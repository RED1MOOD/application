package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.data.model.QariBio
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSectionView(viewModel: QuranAudioViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var activeQariBio by remember { mutableStateOf<QariBio?>(null) }
    var activeOptionsTrack by remember { mutableStateOf<AudioFile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "Smart Search",
            color = PureWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fuzzy matching with phonetic Arabic support",
            color = LightGrayText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Large Premium Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search Qari, Suras, or keywords (e.g., دوسري)...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = QuranPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = QuranPrimary,
                unfocusedBorderColor = QuranPrimary.copy(alpha = 0.3f),
                focusedContainerColor = LightGlassBg,
                unfocusedContainerColor = LightGlassBg,
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Search helpful tip
        if (searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(QuranPrimary.copy(alpha = 0.05f))
                    .border(1.dp, QuranPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tip",
                        tint = QuranPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Try typing: 'دوسري' (finds Yasser), 'باسط' (finds Abdul Basit), 'رحيم' (finds Ar-Rahman / Ar-Raheem) are supported.",
                        color = LightGrayText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search results
        if (searchQuery.isNotEmpty()) {
            Text(
                text = "${searchResults.size} Matching Results Found",
                color = QuranSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local matches found. Try other keywords.", color = LightGrayText, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { track ->
                        QuranTrackRow(
                            track = track,
                            onPlay = { viewModel.playTrack(track, searchResults) },
                            onFav = { viewModel.toggleFavorite(track) },
                            onOptionsClick = { activeOptionsTrack = track },
                            onArtistClick = { activeQariBio = it }
                        )
                    }
                }
            }
        } else {
            // Suggest search categories
            val categories = listOf("Quran Recitations", "Sermons & Talks", "Chants", "Music Recalls")
            Text("Suggested Search Folders", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            categories.forEach { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.onSearchQueryChanged(folder.split(" ").first()) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                    border = BorderStroke(1.dp, BorderWhiteAlpha)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(folder, color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGrayText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Dialog elements trigger inside SearchSectionView
        activeQariBio?.let { bio ->
            QariDetailsDialog(qariBio = bio, onDismiss = { activeQariBio = null })
        }
        activeOptionsTrack?.let { track ->
            AudioFileOptionsDialog(
                track = track,
                onDismiss = { activeOptionsTrack = null },
                onPlay = { viewModel.playTrack(track, searchResults) },
                onFav = { viewModel.toggleFavorite(track) },
                onViewQari = { activeQariBio = it }
            )
        }
    }
}

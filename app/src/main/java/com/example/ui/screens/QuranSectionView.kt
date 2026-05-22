package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.data.model.QariBio
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@Composable
fun QuranSectionView(viewModel: QuranAudioViewModel) {
    val quranFiles by viewModel.quranFiles.collectAsState()
    val qarisList by viewModel.qarisList.collectAsState()

    var filterMode by remember { mutableStateOf("qari") } // qari, surah, completeness, quality
    var selectedQariByGroup by remember { mutableStateOf<String?>(null) }

    var activeQariBio by remember { mutableStateOf<QariBio?>(null) }
    var activeOptionsTrack by remember { mutableStateOf<AudioFile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Core Category Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(QuranPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = "Quran Logo", tint = QuranPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Holy Quran Library",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${quranFiles.size} Classified Audio Tracks Offline",
                    color = LightGrayText,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sorting / Grouping chips
        ScrollableTabRow(
            selectedTabIndex = when (filterMode) {
                "qari" -> 0
                "surah" -> 1
                "completeness" -> 2
                "quality" -> 3
                else -> 0
            },
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[0]), // simplified
                    color = QuranPrimary
                )
            }
        ) {
            Tab(selected = filterMode == "qari", onClick = { filterMode = "qari" }) {
                Text("Reciters (القراء)", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (filterMode == "qari") QuranPrimary else LightGrayText)
            }
            Tab(selected = filterMode == "surah", onClick = { filterMode = "surah" }) {
                Text("Surahs", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (filterMode == "surah") QuranPrimary else LightGrayText)
            }
            Tab(selected = filterMode == "completeness", onClick = { filterMode = "completeness" }) {
                Text("Structure", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (filterMode == "completeness") QuranPrimary else LightGrayText)
            }
            Tab(selected = filterMode == "quality", onClick = { filterMode = "quality" }) {
                Text("Audio Quality", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (filterMode == "quality") QuranPrimary else LightGrayText)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // UI rendering based on Category Groups
        AnimatedContent(targetState = filterMode, label = "filter_content") { targetState ->
            when (targetState) {
                "qari" -> {
                    if (selectedQariByGroup == null) {
                        // Show all detected Qari cards
                        if (qarisList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No reciters detected yet. Please scan or generate.", color = LightGrayText, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(qarisList) { qariName ->
                                    val count = quranFiles.count { it.qariName == qariName }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedQariByGroup = qariName },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                                        border = BorderStroke(1.dp, BorderWhiteAlpha)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(QuranPrimary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = "Qari", tint = DynamicGold)
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = qariName,
                                                    color = PureWhite,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "$count recitation files offline",
                                                    color = LightGrayText,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = LightGrayText)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // View Tracks of Selected Qari
                        val tracksForQari = quranFiles.filter { it.qariName == selectedQariByGroup }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedQariByGroup = null }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = QuranPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Back to Reciters", color = QuranPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = selectedQariByGroup!!,
                                color = PureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tracksForQari) { track ->
                                    QuranTrackRow(
                                        track = track,
                                        onPlay = { viewModel.playTrack(track, tracksForQari) },
                                        onFav = { viewModel.toggleFavorite(track) },
                                        onOptionsClick = { activeOptionsTrack = track },
                                        onArtistClick = { activeQariBio = it }
                                    )
                                }
                            }
                        }
                    }
                }
                "surah" -> {
                    // Listed sorted Surahs
                    val surahsList = quranFiles.filter { it.surahName != null }
                    if (surahsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No identified Surahs yet.", color = LightGrayText)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(surahsList) { track ->
                                QuranTrackRow(
                                    track = track,
                                    onPlay = { viewModel.playTrack(track, surahsList) },
                                    onFav = { viewModel.toggleFavorite(track) },
                                    onOptionsClick = { activeOptionsTrack = track },
                                    onArtistClick = { activeQariBio = it }
                                )
                            }
                        }
                    }
                }
                "completeness" -> {
                    // Filter between completed Surahs and portion clips
                    val completeSurahs = quranFiles.filter { it.isComplete }
                    val partialClips = quranFiles.filter { !it.isComplete }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Complete Surahs (${completeSurahs.size})", color = DynamicGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (completeSurahs.isEmpty()) {
                            Text("No complete surahs.", color = LightGrayText, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(completeSurahs) { track ->
                                        QuranTrackRow(
                                            track = track,
                                            onPlay = { viewModel.playTrack(track, completeSurahs) },
                                            onFav = { viewModel.toggleFavorite(track) },
                                            onOptionsClick = { activeOptionsTrack = track },
                                            onArtistClick = { activeQariBio = it }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Clips & Portions (${partialClips.size})", color = QuranSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (partialClips.isEmpty()) {
                            Text("No clips detected.", color = LightGrayText, fontSize = 12.sp)
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(partialClips) { track ->
                                        QuranTrackRow(
                                            track = track,
                                            onPlay = { viewModel.playTrack(track, partialClips) },
                                            onFav = { viewModel.toggleFavorite(track) },
                                            onOptionsClick = { activeOptionsTrack = track },
                                            onArtistClick = { activeQariBio = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "quality" -> {
                    // Sorting by Quality bands
                    val highQuality = quranFiles.filter { it.qualityKbps >= 256 }
                    val mediumQuality = quranFiles.filter { it.qualityKbps < 256 }

                    Column {
                        Text("High Fidelity (256-320 kbps)", color = QuranPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(highQuality) { track ->
                                    QuranTrackRow(
                                        track = track,
                                        onPlay = { viewModel.playTrack(track, highQuality) },
                                        onFav = { viewModel.toggleFavorite(track) },
                                        onOptionsClick = { activeOptionsTrack = track },
                                        onArtistClick = { activeQariBio = it }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Standard Fidelity (< 256 kbps)", color = LightGrayText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(mediumQuality) { track ->
                                    QuranTrackRow(
                                        track = track,
                                        onPlay = { viewModel.playTrack(track, mediumQuality) },
                                        onFav = { viewModel.toggleFavorite(track) },
                                        onOptionsClick = { activeOptionsTrack = track },
                                        onArtistClick = { activeQariBio = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog view triggers inside QuranSectionView
        activeQariBio?.let { bio ->
            QariDetailsDialog(qariBio = bio, onDismiss = { activeQariBio = null })
        }
        activeOptionsTrack?.let { track ->
            AudioFileOptionsDialog(
                track = track,
                onDismiss = { activeOptionsTrack = null },
                onPlay = { viewModel.playTrack(track, quranFiles) },
                onFav = { viewModel.toggleFavorite(track) },
                onViewQari = { activeQariBio = it }
            )
        }
    }
}

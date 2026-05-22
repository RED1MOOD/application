package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.data.model.QariBio
import com.example.util.Language
import com.example.util.LocalizationManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@Composable
fun HomeScreenView(
    viewModel: QuranAudioViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val allFiles by viewModel.allFiles.collectAsState()
    val quranFiles by viewModel.quranFiles.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val isDemoGenerating by viewModel.isDemoGenerating.collectAsState()
    val demoProgress by viewModel.demoProgress.collectAsState()

    val langState by LocalizationManager.currentLanguage.collectAsState()

    var activeQariBio by remember { mutableStateOf<QariBio?>(null) }
    var activeOptionsTrack by remember { mutableStateOf<AudioFile?>(null) }

    val greeting = remember(langState) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val key = when (hour) {
            in 0..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
        }
        LocalizationManager.get(key)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Beautiful elegant Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    color = LightGrayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = LocalizationManager.get("peace_be_upon_you"),
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Decorative crescent emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(QuranPrimary.copy(alpha = 0.15f))
                    .border(1.dp, QuranPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Emblem",
                    tint = DynamicGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scanner Engine wizard if empty library
        if (allFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(QuranSurface, QuranSurface.copy(alpha = 0.6f))
                        )
                    )
                    .border(1.dp, QuranPrimary.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Scanned Data Wizard",
                        tint = QuranPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = LocalizationManager.get("empty_list"),
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = LocalizationManager.get("about_app_desc"),
                        color = LightGrayText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isDemoGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${LocalizationManager.get("populate")} ($demoProgress%) ...",
                                color = QuranPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = demoProgress / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = QuranPrimary,
                                trackColor = QuranDarkBg
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { viewModel.startStorageScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Scan")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.get("trigger_scan"), color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.generateSampleQuranLibrary() },
                                border = BorderStroke(1.dp, QuranPrimary),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = QuranPrimary)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = "Simulate")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.get("populate"))
                            }
                        }
                    }
                }
            }
        } else {
            // Quick Simulation Info Banner (if they only have demo tracks)
            val hasReal = allFiles.any { !it.filePath.startsWith("/demo_storage") }
            if (!hasReal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DynamicGold.copy(alpha = 0.08f))
                        .border(1.dp, DynamicGold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Tips", tint = DynamicGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LocalizationManager.get("sandbox_banner"),
                            color = DynamicGold,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick Play Shuffle Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (quranFiles.isNotEmpty()) {
                            viewModel.playTrack(quranFiles.random(), quranFiles)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LocalizationManager.get("quran_shuffle"), color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigateToTab("search") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = QuranSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, QuranPrimary.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = QuranPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LocalizationManager.get("smart_search"), color = PureWhite)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recently Played Section
            if (recentlyPlayed.isNotEmpty()) {
                Text(
                    text = LocalizationManager.get("recent"),
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentlyPlayed) { track ->
                        CompactReciterCard(track) {
                            viewModel.playTrack(track, recentlyPlayed)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Smart Recommendations Block
            Text(
                text = LocalizationManager.get("featured_offline"),
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val featuredList = quranFiles.take(4)
            if (featuredList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderWhiteAlpha)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        featuredList.forEach { track ->
                            QuranTrackRow(
                                track = track,
                                onPlay = { viewModel.playTrack(track, quranFiles) },
                                onFav = { viewModel.toggleFavorite(track) },
                                onOptionsClick = { activeOptionsTrack = track },
                                onArtistClick = { activeQariBio = it }
                            )
                            if (track != featuredList.last()) {
                                Divider(color = Color(0x14FFFFFF), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            } else {
                Text(text = LocalizationManager.get("empty_list"), color = LightGrayText, fontSize = 13.sp)
            }
        }

        // Dialog Triggers for Bios and File options
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

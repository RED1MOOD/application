package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.player.AudioPlayerController
import com.example.util.Language
import com.example.util.LocalizationManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@Composable
fun HomeScreenView(
    viewModel: QuranAudioViewModel,
    onNavigateToTab: (String) -> Unit,
    onExpandPlayer: () -> Unit
) {
    val allFiles by viewModel.allFiles.collectAsState()
    val quranFiles by viewModel.quranFiles.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val isDemoGenerating by viewModel.isDemoGenerating.collectAsState()
    val demoProgress by viewModel.demoProgress.collectAsState()

    val currentTrack by AudioPlayerController.currentTrack.collectAsState()
    val isPlaying by AudioPlayerController.isPlaying.collectAsState()
    val isBuffering by AudioPlayerController.isBuffering.collectAsState()
    val playbackPosition by AudioPlayerController.playbackPosition.collectAsState()

    val langState by LocalizationManager.currentLanguage.collectAsState()

    val greeting = remember(langState) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val key = when (hour) {
            in 0..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
        }
        LocalizationManager.get(key)
    }

    // Determine target track for the Hero play card
    val heroTrack = currentTrack ?: recentlyPlayed.firstOrNull() ?: quranFiles.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Welcoming spiritual row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    color = LightGrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = LocalizationManager.get("peace_be_upon_you"),
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Celestial Gold badge icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(QuranPrimary.copy(alpha = 0.12f))
                    .border(1.dp, QuranPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = DynamicGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HERO PLAYER CARD
        if (heroTrack != null) {
            HeroPlayerComponent(
                track = heroTrack,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                playbackPosition = playbackPosition,
                onTogglePlay = {
                    if (currentTrack?.filePath == heroTrack.filePath) {
                        AudioPlayerController.togglePlayPause()
                    } else {
                        viewModel.playTrack(heroTrack, quranFiles)
                    }
                },
                onExpand = onExpandPlayer
            )
        } else {
            // Elegant placeholder card when completely empty
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                border = BorderStroke(1.dp, BorderWhiteAlpha)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = QuranPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (langState == Language.AR) "أثير التلاوة للقلوب" else "Quranic Spiritual Recitations",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (langState == Language.AR) "قم بتهيئة الملفات لتتمكن من تشغيل سور الذكر الحكيم" else "Populate the library database to start streaming offline files",
                        color = LightGrayText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // THE THREE QUICK ACCESS CARDS
        QuickAccessRow(
            langState = langState,
            onNavigate = onNavigateToTab
        )

        Spacer(modifier = Modifier.height(6.dp))

        // DYNAMIC CONTENT LIST
        if (allFiles.isEmpty()) {
            // Setup / Scanner wizard screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = QuranSurface),
                border = BorderStroke(1.dp, Color(0x1F10B981))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = QuranPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = LocalizationManager.get("empty_list"),
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalizationManager.get("about_app_desc"),
                        color = LightGrayText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isDemoGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${LocalizationManager.get("populate")} ($demoProgress%) ...",
                                color = QuranPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = demoProgress / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
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
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.get("trigger_scan"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.generateSampleQuranLibrary() },
                                border = BorderStroke(1.dp, QuranPrimary),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = QuranPrimary)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.get("populate"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Clean sandbox / offline notice cards
            val hasReal = allFiles.any { !it.filePath.startsWith("/demo_storage") }
            if (!hasReal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DynamicGold.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, DynamicGold.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DynamicGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocalizationManager.get("sandbox_banner"),
                            color = DynamicGold,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Clean Quiet Title of Content View
            Text(
                text = if (langState == Language.AR) "التلاوات المتاحة حالياً" else "Spiritual Audio Directory",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Non-nested clean Lazy list
            val itemsToShow = quranFiles.take(8)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(LightGlassBg)
                    .border(1.dp, BorderWhiteAlpha)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsToShow.forEach { track ->
                    val isTrackActive = currentTrack?.filePath == track.filePath
                    SimpleTrackRow(
                        track = track,
                        isActive = isTrackActive,
                        isPlaying = isTrackActive && isPlaying,
                        onPlay = {
                            viewModel.playTrack(track, quranFiles)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HeroPlayerComponent(
    track: AudioFile,
    isPlaying: Boolean,
    isBuffering: Boolean,
    playbackPosition: Long,
    onTogglePlay: () -> Unit,
    onExpand: () -> Unit
) {
    val progressPercent = if (track.durationMs > 0) playbackPosition.toFloat() / track.durationMs.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onExpand),
        colors = CardDefaults.cardColors(containerColor = Color(0x1A10B981)), // subtle spiritual emerald hue
        border = BorderStroke(1.dp, QuranPrimary.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x2E10B981), Color(0x0405070A))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Now Playing or Last Played Indicator
                Text(
                    text = if (isPlaying) {
                        LocalizationManager.get("now_playing").uppercase()
                    } else {
                        if (LocalizationManager.currentLanguage.value == Language.AR) "تشغيل سريع" else "QUICK PLAY"
                    },
                    color = QuranPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Track title & singer/reciter
                Text(
                    text = track.title,
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    color = LightGrayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Substantial, beautiful central action button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = QuranPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(QuranPrimary.copy(alpha = 0.15f))
                        )
                    }

                    IconButton(
                        onClick = {
                            onTogglePlay()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(QuranPrimary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Control",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Visual progress details
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = progressPercent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = QuranPrimary,
                        trackColor = Color(0x26FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val elapsedSec = (playbackPosition / 1000) % 60
                        val elapsedMin = (playbackPosition / (1000 * 60)) % 60
                        val totalSec = (track.durationMs / 1000) % 60
                        val totalMin = (track.durationMs / (1000 * 60)) % 60

                        Text(
                            text = String.format("%02d:%02d", elapsedMin, elapsedSec),
                            color = LightGrayText,
                            fontSize = 10.sp
                        )
                        Text(
                            text = String.format("%02d:%02d", totalMin, totalSec),
                            color = LightGrayText,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessRow(
    langState: Language,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val menuList = listOf(
            Triple(
                "quran",
                if (langState == Language.AR) "القرآن الكريم" else "Noble Quran",
                Icons.Default.MenuBook
            ),
            Triple(
                "online",
                if (langState == Language.AR) "البثث المباشرة" else "Live Streams",
                Icons.Default.CellTower
            ),
            Triple(
                "library",
                if (langState == Language.AR) "المكتبة والـمفضلة" else "Library & Favs",
                Icons.Default.Favorite
            )
        )

        menuList.forEach { (tabId, titleLabel, vectorIcon) ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigate(tabId) },
                colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                border = BorderStroke(1.dp, BorderWhiteAlpha)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(QuranPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = titleLabel,
                            tint = QuranPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = titleLabel,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleTrackRow(
    track: AudioFile,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color(0x1A10B981) else Color.Transparent)
            .clickable(onClick = onPlay)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) QuranPrimary else Color(0x0CFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = if (isActive) Color.Black else QuranPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isActive) QuranPrimary else PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = LightGrayText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (isActive) QuranPrimary else LightGrayText.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

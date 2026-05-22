package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.LocalizationManager
import com.example.util.Language
import com.example.data.model.AudioFile
import com.example.player.AudioPlayerController
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerView(
    track: AudioFile,
    isPlaying: Boolean,
    viewModel: QuranAudioViewModel,
    onDismiss: () -> Unit
) {
    val progress by AudioPlayerController.playbackPosition.collectAsState()
    val speed by AudioPlayerController.playbackSpeed.collectAsState()
    val pitch by AudioPlayerController.playbackPitch.collectAsState()
    val bassStrength by AudioPlayerController.bassStrength.collectAsState()
    val reverbPreset by AudioPlayerController.reverbPreset.collectAsState()
    val isEnhanceActive by AudioPlayerController.isEnhanceModeActive.collectAsState()
    val sleepTimerValue by AudioPlayerController.sleepTimeRemaining.collectAsState()

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("controls") }
    var isFavorite = track.isFavorite

    val langState by LocalizationManager.currentLanguage.collectAsState()
    var ayahsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingAyahs by remember { mutableStateOf(false) }
    var lyricsEnabled by remember { mutableStateOf(true) }

    val parsedSurahIndex = remember(track.filePath) {
        try {
            val fileName = track.filePath.substringAfterLast("/").substringBefore(".mp3")
            fileName.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(track.filePath, parsedSurahIndex) {
        if (track.category == "QURAN" && parsedSurahIndex != null) {
            isLoadingAyahs = true
            val onlineList = fetchSurahAyahs(parsedSurahIndex)
            ayahsList = if (onlineList.isNotEmpty()) {
                onlineList
            } else {
                getOfflineSurahBackup(parsedSurahIndex)
            }
            isLoadingAyahs = false
        } else {
            ayahsList = emptyList()
        }
    }

    // Show custom Playlist Selection popup
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    // Waveform Animation cycles simple model
    val infiniteTransition = rememberInfiniteTransition(label = "player_waveform")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_offset"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QuranDarkBg)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0E121E), Color(0xFF05070A), Color(0xFF05070A))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Elegant Background crescent ornament layout
            Canvas(modifier = Modifier.fillMaxWidth().height(260.dp).align(Alignment.TopCenter)) {
                drawCircle(
                    color = QuranPrimary.copy(alpha = 0.05f),
                    radius = 320f,
                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, -80f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Player Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = PureWhite, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = if (track.category == "QURAN") "Holy Quran Player" else "Audio Studio Player",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showAddToPlaylistDialog = true }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to playlist", tint = QuranPrimary, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Immersive Glassmorphic Card or Circle for Art layout
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(LightGlassBg, QuranPrimary.copy(alpha = 0.2f), LightGlassBg)
                            )
                        )
                        .border(1.dp, BorderWhiteAlpha, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Spiritual Icon Core
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(QuranPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (track.category == "QURAN") Icons.Default.MenuBook else Icons.Default.MusicNote,
                                contentDescription = "Art Core",
                                tint = if (isEnhanceActive) QuranPrimary else DynamicGold,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        // Animated live audio visualizer line inside card
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(30.dp)
                        ) {
                            val bars = 8
                            for (i in 0 until bars) {
                                // Compute dynamic wave height derived from sin & player status
                                val amplitudeMultiplier = if (isPlaying) 1f else 0.1f
                                val scaleY = sin(waveOffset + i * 0.5f) * 12f * amplitudeMultiplier + 14f
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(scaleY.coerceAtLeast(4f).dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(if (isEnhanceActive) QuranPrimary else QuranSecondary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Metadata details
                Text(
                    text = track.title,
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (track.category == "QURAN") {
                        Icon(Icons.Default.Verified, contentDescription = "Original reciter", tint = DynamicGold, modifier = Modifier.size(14.dp).padding(end = 2.dp))
                    }
                    Text(
                        text = track.artist,
                        color = LightGrayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Workspace selection tab row (Controls, DSP, Config)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGlassBg)
                        .border(1.dp, BorderWhiteAlpha, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    val tabs = remember(track.category, langState) {
                        val base = mutableListOf(
                            "controls" to if (langState == com.example.util.Language.AR) "التحكم" else "Overview"
                        )
                        if (track.category == "QURAN") {
                            base.add("lyrics" to LocalizationManager.get("lyrics_tab"))
                        }
                        base.add("dsp" to if (langState == com.example.util.Language.AR) "المحسن" else "AI Enhancer")
                        base.add("filters" to if (langState == com.example.util.Language.AR) "الموازن" else "Equalizer")
                        base
                    }
                    tabs.forEach { (tabId, tabName) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activeTab == tabId) QuranPrimary else Color.Transparent)
                                .clickable { activeTab = tabId }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabName,
                                color = if (activeTab == tabId) Color.Black else LightGrayText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Render dynamic inner tabs
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (activeTab) {
                            "lyrics" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Title row with a Switch component to Enable/Disable
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x0CFFFFFF))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = LocalizationManager.get("lyrics_tab"),
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (lyricsEnabled) LocalizationManager.get("enable_lyrics") else LocalizationManager.get("disable_lyrics"),
                                                color = if (lyricsEnabled) QuranPrimary else LightGrayText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Switch(
                                                checked = lyricsEnabled,
                                                onCheckedChange = { lyricsEnabled = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = QuranPrimary,
                                                    checkedTrackColor = QuranPrimary.copy(alpha = 0.5f),
                                                    uncheckedThumbColor = LightGrayText,
                                                    uncheckedTrackColor = Color(0x1FFFFFFF)
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Verses Content Area
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(QuranSurface)
                                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!lyricsEnabled) {
                                            Text(
                                                text = LocalizationManager.get("lyrics_disabled"),
                                                color = LightGrayText,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        } else if (isLoadingAyahs) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(
                                                    color = QuranPrimary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    text = LocalizationManager.get("lyrics_loading"),
                                                    color = LightGrayText,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            // Render list of Ayahs beautifully in scrollable list
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                itemsIndexed(ayahsList) { index, ayah ->
                                                    val isBismillah = ayah.contains("بِسْمِ اللَّهِ") && index == 0 && parsedSurahIndex != 1
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isBismillah) Color(0x1410B981) else Color(0x05FFFFFF)
                                                        ),
                                                        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(12.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text(
                                                                text = ayah,
                                                                color = PureWhite,
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                textAlign = TextAlign.Center,
                                                                lineHeight = 26.sp,
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "﴿${index + 1}﴾",
                                                                color = QuranPrimary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "controls" -> {
                                // Standard view with seek bar, playback speed controls & sleep timer countdown
                                val durationMs = if (track.durationMs > 0) track.durationMs else 100000L // Default placeholder duration if dynamic stream is preparing
                                val elapsedSec = (progress / 1000) % 60
                                val elapsedMin = (progress / (1000 * 60)) % 60
                                val remainingMs = (durationMs - progress).coerceAtLeast(0L)
                                val remainsSec = (remainingMs / 1000) % 60
                                val remainsMin = (remainingMs / (1000 * 60)) % 60

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(String.format("%02d:%02d", elapsedMin, elapsedSec), color = LightGrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (sleepTimerValue > 0) {
                                        Text("Sleep timer: ${(sleepTimerValue / 60000) + 1} min remain", color = DynamicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(String.format("-%02d:%02d", remainsMin, remainsSec), color = LightGrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                val sliderMax = durationMs.toFloat()
                                val sliderValue = progress.toFloat().coerceIn(0f, sliderMax)
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { AudioPlayerController.seekTo(it.toLong()) },
                                    valueRange = 0f..sliderMax,
                                    colors = SliderDefaults.colors(
                                        thumbColor = QuranPrimary,
                                        activeTrackColor = QuranPrimary,
                                        inactiveTrackColor = QuranSurface
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Main Music controller line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        AudioPlayerController.isShuffle = !AudioPlayerController.isShuffle
                                        Toast.makeText(context, if (AudioPlayerController.isShuffle) "Shuffle On" else "Shuffle Off", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (AudioPlayerController.isShuffle) QuranPrimary else LightGrayText)
                                    }

                                    IconButton(onClick = { AudioPlayerController.prev() }) {
                                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = PureWhite, modifier = Modifier.size(36.dp))
                                    }

                                    IconButton(
                                        onClick = { AudioPlayerController.togglePlayPause() },
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(QuranPrimary)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play/pause",
                                            tint = Color.Black,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    IconButton(onClick = { AudioPlayerController.next() }) {
                                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = PureWhite, modifier = Modifier.size(36.dp))
                                    }

                                    IconButton(onClick = {
                                        AudioPlayerController.isRepeat = !AudioPlayerController.isRepeat
                                        Toast.makeText(context, if (AudioPlayerController.isRepeat) "Repeat On" else "Repeat Off", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = if (AudioPlayerController.isRepeat) QuranPrimary else LightGrayText)
                                    }
                                }
                            }
                            "dsp" -> {
                                // Smart Quran speech enhancer controls
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isEnhanceActive) QuranPrimary.copy(alpha = 0.08f) else QuranSurface)
                                        .border(1.dp, if (isEnhanceActive) QuranPrimary else QuranPrimary.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                                        .padding(18.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Quran Enhance Mode", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("Offline speech DSP. Boosts vocals, cleans statics, and maps custom hollow echo.", color = LightGrayText, fontSize = 11.sp)
                                            }
                                            Switch(
                                                checked = isEnhanceActive,
                                                onCheckedChange = { AudioPlayerController.toggleQuranEnhanceMode() },
                                                colors = SwitchDefaults.colors(checkedThumbColor = QuranPrimary, checkedTrackColor = QuranPrimary.copy(alpha = 0.5f))
                                            )
                                        }

                                        if (isEnhanceActive) {
                                            Divider(color = QuranPrimary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = QuranPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Vocal mid range (1kHz - 4kHz) boosted: Active", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = QuranPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Cathedra echo mapping simulation active", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sleep timer selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sleep Timer Setup", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(0, 15, 30, 45).forEach { mins ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if ((mins == 0 && sleepTimerValue == 0L) || (mins > 0 && sleepTimerValue > 0 && (sleepTimerValue / 60000).toInt() in (mins - 2)..(mins + 2))) QuranPrimary else QuranSurface)
                                                    .clickable { AudioPlayerController.setSleepTimer(mins) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (mins == 0) "Off" else "${mins}m",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if ((mins == 0 && sleepTimerValue == 0L) || (mins > 0 && sleepTimerValue > 0 && (sleepTimerValue / 60000).toInt() in (mins - 2)..(mins + 2))) Color.Black else PureWhite
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "filters" -> {
                                // Equalizer professional sliders, speed & pitch adjusting
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(QuranSurface)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        // Speed slider
                                        Text("Playback Speed: ${"%.2f".format(speed)}x", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = speed,
                                            onValueChange = { AudioPlayerController.setSpeed(it) },
                                            valueRange = 0.5f..2.0f,
                                            colors = SliderDefaults.colors(thumbColor = QuranPrimary, activeTrackColor = QuranPrimary)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Pitch slider
                                        Text("Voice Pitch Level: ${"%.2f".format(pitch)}x", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = pitch,
                                            onValueChange = { AudioPlayerController.setPitch(it) },
                                            valueRange = 0.5f..1.5f,
                                            colors = SliderDefaults.colors(thumbColor = DynamicGold, activeTrackColor = DynamicGold)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Bass slider
                                        Text("Bass Boost Power: ${(bassStrength / 10)}%", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = bassStrength.toFloat(),
                                            onValueChange = { AudioPlayerController.setBassStrength(it.toInt().toShort()) },
                                            valueRange = 0f..1000f,
                                            colors = SliderDefaults.colors(thumbColor = QuranSecondary, activeTrackColor = QuranSecondary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom heart toggler
                IconButton(
                    onClick = { AudioPlayerController.toggleFavoriteCurrentTrack() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(QuranSurface)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (track.isFavorite) QuranPrimary else LightGrayText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // ADD TO PLAYLIST SELECTION POPUP DIALOG
    if (showAddToPlaylistDialog) {
        val playlistsList by viewModel.playlists.collectAsState()
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            containerColor = QuranSurface,
            title = { Text("Add Track to Playlist", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(160.dp).verticalScroll(rememberScrollState())) {
                    if (playlistsList.isEmpty()) {
                        Text("No playlists created yet. Create one in the Library tab first.", color = LightGrayText, fontSize = 13.sp)
                    } else {
                        playlistsList.forEach { pl ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.addTrackToPlaylist(pl.id, track.filePath)
                                        Toast.makeText(context, "Added track to ${pl.name}", Toast.LENGTH_SHORT).show()
                                        showAddToPlaylistDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = QuranDarkBg)
                            ) {
                                Text(pl.name, color = PureWhite, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("Close", color = LightGrayText)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Helper networks loaders for verses / words of current Surah
suspend fun fetchSurahAyahs(surahIndex: Int): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.alquran.cloud/v1/surah/$surahIndex")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.connect()
            
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                if (json.getInt("code") == 200) {
                    val data = json.getJSONObject("data")
                    val ayahsArray = data.getJSONArray("ayahs")
                    val list = mutableListOf<String>()
                    for (i in 0 until ayahsArray.length()) {
                        val ayahObj = ayahsArray.getJSONObject(i)
                        val text = ayahObj.getString("text")
                        list.add(text)
                    }
                    return@withContext list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}

fun getOfflineSurahBackup(surahIndex: Int): List<String> {
    return when (surahIndex) {
        1 -> listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            "الرَّحْمَٰنِ الرَّحِيمِ",
            "مَالِكِ يَوْمِ الدِّينِ",
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"
        )
        108 -> listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
            "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
            "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ"
        )
        112 -> listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "قُلْ هُوَ اللَّهُ أَحَدٌ",
            "اللَّهُ الصَّمَدُ",
            "لَمْ يَلِدْ وَلَمْ يُولَدْ",
            "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ"
        )
        else -> listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
            "مِن شَرِّ مَا خَلَقَ",
            "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
            "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
            "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"
        )
    }
}

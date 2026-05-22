package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.AudioFile
import com.example.data.model.Playlist
import com.example.editor.AudioEditorUtility
import com.example.player.AudioPlayerController
import com.example.ui.theme.*
import com.example.util.LocalizationManager
import com.example.ui.viewmodel.QuranAudioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: QuranAudioViewModel) {
    val langState by LocalizationManager.currentLanguage.collectAsState()
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("home") }
    var activeFullPlayer by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentTrack by AudioPlayerController.currentTrack.collectAsState()
    val isPlaying by AudioPlayerController.isPlaying.collectAsState()

    // Trigger Splash delay
    LaunchedEffect(Unit) {
        delay(2200)
        showSplash = false
    }

    if (showSplash) {
        SplashScreenPresenter()
    } else {
        Scaffold(
            bottomBar = {
                Column {
                    // Persistent Mini Player above the bottom bar
                    if (currentTrack != null) {
                        MiniPlayerComponent(
                            track = currentTrack!!,
                            isPlaying = isPlaying,
                            onPlayPauseToggle = { AudioPlayerController.togglePlayPause() },
                            onPrev = { AudioPlayerController.prev() },
                            onNext = { AudioPlayerController.next() },
                            onExpand = { activeFullPlayer = true }
                        )
                    }

                    // Main Navigation Bar with Glassmorphic gradient
                    NavigationBar(
                        containerColor = Color(0xFF0A0C12),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .drawBehind {
                                drawLine(
                                    color = Color(0x14FFFFFF), // white with 8% alpha
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                    ) {
                        val tabs = listOf(
                            Triple("home", LocalizationManager.get("home"), Icons.Default.Home),
                            Triple("quran", LocalizationManager.get("quran"), Icons.Default.MenuBook),
                            Triple("online", LocalizationManager.get("online"), Icons.Default.Cloud),
                            Triple("search", LocalizationManager.get("search"), Icons.Default.Search),
                            Triple("library", LocalizationManager.get("library"), Icons.Default.LibraryMusic),
                            Triple("settings", LocalizationManager.get("settings"), Icons.Default.Settings)
                        )

                        tabs.forEach { (tabId, label, icon) ->
                            NavigationBarItem(
                                selected = currentTab == tabId,
                                onClick = { currentTab = tabId },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = QuranPrimary,
                                    selectedTextColor = QuranPrimary,
                                    unselectedIconColor = LightGrayText,
                                    unselectedTextColor = LightGrayText,
                                    indicatorColor = Color(0xFF0A0C12)
                                )
                            )
                        }
                    }
                }
            },
            containerColor = QuranDarkBg
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(QuranDarkBg)
                    .pointerInput(currentTrack) {
                        try {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                if (java.lang.Math.abs(dragAmount) > 15f && currentTrack != null) {
                                    activeFullPlayer = true
                                }
                            }
                        } catch (e: Exception) {}
                    }
                    .drawBehind {
                        // Ambient top right Emerald orb matching html
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x3010B981), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * -0.05f),
                                radius = size.width * 0.8f
                            ),
                            radius = size.width * 0.8f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * -0.05f)
                        )
                        // Ambient bottom left Blue orb matching html
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x243B82F6), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width * -0.1f, size.height * 1.05f),
                                radius = size.width * 0.7f
                            ),
                            radius = size.width * 0.7f,
                            center = androidx.compose.ui.geometry.Offset(size.width * -0.1f, size.height * 1.05f)
                        )
                    }
            ) {
                // Crossfade animation between screens
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, easing = LinearEasing)) togetherWith
                        fadeOut(animationSpec = tween(220, easing = LinearEasing))
                    },
                    label = "tab_crossfade"
                ) { targetState ->
                    when (targetState) {
                        "home" -> HomeScreenView(viewModel, onNavigateToTab = { currentTab = it })
                        "quran" -> QuranSectionView(viewModel)
                        "online" -> OnlineSectionView(viewModel)
                        "search" -> SearchSectionView(viewModel)
                        "library" -> LibrarySectionView(viewModel)
                        "settings" -> SettingsSectionView(viewModel)
                    }
                }
            }
        }

        // Expanded full immersive sliding player
        if (activeFullPlayer && currentTrack != null) {
            FullPlayerView(
                track = currentTrack!!,
                isPlaying = isPlaying,
                viewModel = viewModel,
                onDismiss = { activeFullPlayer = false }
            )
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreenPresenter() {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash_scale"
    )
    val alpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QuranDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Geometric islamic background overlay
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
            val width = size.width
            val height = size.height
            val step = 120f
            for (x in 0..width.toInt() step step.toInt()) {
                drawLine(
                    color = QuranPrimary,
                    start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                    end = androidx.compose.ui.geometry.Offset(width - x.toFloat(), height),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(QuranPrimary, QuranSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Quran Audio AI Logo",
                    tint = PureWhite,
                    modifier = Modifier.size(65.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quran Audio AI",
                color = PureWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Local Storage Smart AI Manager",
                color = LightGrayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(60.dp))

            CircularProgressIndicator(
                color = QuranPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "100% Offline | Smart DSP Audio",
                color = QuranPrimary.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// 2. MINI PLAYER
// ==========================================
@Composable
fun MiniPlayerComponent(
    track: AudioFile,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit
) {
    val progress by AudioPlayerController.playbackPosition.collectAsState()
    val progressPercent = if (track.durationMs > 0) progress.toFloat() / track.durationMs.toFloat() else 0f
    val isEnhanceActive by AudioPlayerController.isEnhanceModeActive.collectAsState()

    val elapsedSec = (progress / 1000) % 60
    val elapsedMin = (progress / (1000 * 60)) % 60
    val totalSec = (track.durationMs / 1000) % 60
    val totalMin = (track.durationMs / (1000 * 60)) % 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x1AFFFFFF)) // elegant translucent overlay matching bg-white/10
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(24.dp)) // border-white/20
            .clickable(onClick = onExpand)
    ) {
        Column {
            LinearProgressIndicator(
                progress = progressPercent,
                modifier = Modifier.fillMaxWidth().height(2.5.dp),
                color = QuranPrimary,
                trackColor = Color.Transparent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Floating icon box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (track.category == "QURAN") QuranPrimary else QuranSecondary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (track.category == "QURAN") Icons.Default.MenuBook else Icons.Default.MusicNote,
                        contentDescription = "Track Art",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isEnhanceActive) "QURAN ENHANCE ACTIVE" else "QURAN PLAYER CORE",
                        color = QuranPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.title} - ${track.artist}",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format("%02d:%02d / %02d:%02d", elapsedMin, elapsedSec, totalMin, totalSec),
                        color = LightGrayText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Controls
                IconButton(onClick = onPrev) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = PureWhite.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onPlayPauseToggle()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PureWhite)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = PureWhite.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

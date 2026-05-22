package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AudioFile
import com.example.data.model.QariBio
import com.example.util.Language
import com.example.util.LocalizationManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QariDetailsDialog(
    qariBio: QariBio,
    onDismiss: () -> Unit
) {
    val langState by LocalizationManager.currentLanguage.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = QuranDarkBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F241F), QuranDarkBg, QuranDarkBg)
                        )
                    )
            ) {
                // Background artistic overlay representation
                Canvas(modifier = Modifier.fillMaxSize().matchParentSize().align(Alignment.Center)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1F10B981), Color.Transparent),
                            radius = size.width * 0.5f
                        ),
                        radius = size.width * 0.5f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                        }

                        // Verified Chip Label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1510B981))
                                .border(1.dp, QuranPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = DynamicGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LocalizationManager.get("ai_certified"),
                                    color = QuranPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Scenic Qari Badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(QuranPrimary, QuranSecondary)
                                )
                            )
                            .border(2.dp, PureWhite.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = qariBio.name.split(" ").lastOrNull()?.take(2) ?: "قاري",
                            color = PureWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Qari Name & Title
                    Text(
                        text = qariBio.fullName,
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = qariBio.title,
                        color = DynamicGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Geographical & Timeline Board
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Location
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                            border = BorderStroke(1.dp, BorderWhiteAlpha)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = QuranPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocalizationManager.get("qari_location"),
                                    color = LightGrayText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(qariBio.location, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }

                        // Card 2: Active Timeline
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                            border = BorderStroke(1.dp, BorderWhiteAlpha)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Timeline, contentDescription = null, tint = QuranSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocalizationManager.get("qari_influence"),
                                    color = LightGrayText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(qariBio.activeYears, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Style & Recitation Highlights
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1F10B981))
                            .border(1.dp, QuranPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DynamicGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LocalizationManager.get("qari_school"),
                                color = QuranPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = qariBio.style,
                            color = PureWhite.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Biography Details Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGlassBg)
                            .border(1.dp, BorderWhiteAlpha, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = QuranSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LocalizationManager.get("qari_bio_title"),
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = qariBio.bio,
                            color = LightGrayText,
                            fontSize = 12.sp,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Done/Close Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = LocalizationManager.get("done_reading"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioFileOptionsDialog(
    track: AudioFile,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onFav: () -> Unit,
    onViewQari: (QariBio) -> Unit
) {
    val langState by LocalizationManager.currentLanguage.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copiedBytesMsg by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BorderWhiteAlpha, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = QuranDarkBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with File metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (track.category == "QURAN") QuranPrimary.copy(alpha = 0.15f)
                                else QuranSecondary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.category == "QURAN") Icons.Default.MenuBook else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (track.category == "QURAN") QuranPrimary else QuranSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = LightGrayText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LightGrayText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0x14FFFFFF), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Track Actions
                Text(
                    text = LocalizationManager.get("quick_options"),
                    color = LightGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Play
                    IconButtonOption(
                        icon = Icons.Default.PlayArrow,
                        text = LocalizationManager.get("play_now"),
                        color = QuranPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onPlay()
                        }
                    )

                    // Action 2: Favorite toggle
                    IconButtonOption(
                        icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        text = if (track.isFavorite) LocalizationManager.get("infav") else LocalizationManager.get("fav"),
                        color = if (track.isFavorite) Color.Red else PureWhite,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFav()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Physical File details
                Text(
                    text = LocalizationManager.get("path"),
                    color = LightGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LightGlassBg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${LocalizationManager.get("path")}:",
                            color = LightGrayText,
                            fontSize = 11.sp
                        )
                        Text(
                            text = LocalizationManager.get("copy_path"),
                            color = QuranPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(track.filePath))
                                copiedBytesMsg = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.filePath,
                        color = PureWhite,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    
                    if (copiedBytesMsg) {
                        Text(
                            text = LocalizationManager.get("copied"),
                            color = DynamicGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0x0FFFFFFF), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(LocalizationManager.get("file_size"), color = LightGrayText, fontSize = 10.sp)
                            val sizeMb = String.format("%.2f MB", track.sizeBytes.toFloat() / (1024f * 1024f))
                            Text(sizeMb, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(LocalizationManager.get("quality"), color = LightGrayText, fontSize = 10.sp)
                            Text("${track.qualityKbps} Kbps MP3", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(LocalizationManager.get("recitation_type"), color = LightGrayText, fontSize = 10.sp)
                            Text(
                                if (track.category == "QURAN") LocalizationManager.get("assem") else LocalizationManager.get("mixed_sound"),
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Qari Bio Trigger Section (if available)
                val qariBio = remember(track.artist, track.qariName) {
                    QariBio.findByName(track.artist) ?: QariBio.findByName(track.qariName)
                }

                if (qariBio != null) {
                    Text(LocalizationManager.get("verified"), color = LightGrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1F10B981))
                            .border(1.dp, QuranPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                onViewQari(qariBio)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(QuranPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(qariBio.fullName, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(LocalizationManager.get("qari_bio_title"), color = DynamicGold, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = QuranPrimary, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Show a beautiful option to search Google about target singer/artist
                    Text(LocalizationManager.get("bio_not_found"), color = LightGrayText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(LocalizationManager.get("bio_not_found_desc"), track.artist),
                        color = LightGrayText,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

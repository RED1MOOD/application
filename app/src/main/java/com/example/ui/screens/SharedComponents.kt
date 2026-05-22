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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.data.model.QariBio
import com.example.ui.theme.*

@Composable
fun CompactReciterCard(
    track: AudioFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderWhiteAlpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(QuranPrimary, QuranSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = track.artist.split(" ").lastOrNull()?.take(2) ?: "ق",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = track.title,
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                color = LightGrayText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuranTrackRow(
    track: AudioFile,
    onPlay: () -> Unit,
    onFav: () -> Unit,
    onOptionsClick: () -> Unit,
    onArtistClick: (QariBio) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onPlay)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(QuranPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (track.category == "QURAN") Icons.Default.MenuBook else Icons.Default.MusicNote,
                contentDescription = null,
                tint = QuranPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Titles and artist details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val artistName = track.artist
            Text(
                text = artistName,
                color = LightGrayText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    QariBio.findByName(artistName)?.let { onArtistClick(it) }
                }
            )
        }

        // Quality badge label
        if (track.qualityKbps > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1F10B981))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${track.qualityKbps}K",
                    color = QuranPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Play/Pause Action Row Buttons
        IconButton(
            onClick = onFav,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) Color.Red else LightGrayText,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Configure File",
                tint = LightGrayText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun IconButtonOption(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(44.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import com.example.util.LocalizationManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.data.model.Playlist
import com.example.data.model.QariBio
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySectionView(viewModel: QuranAudioViewModel) {
    val langState by LocalizationManager.currentLanguage.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val musicFiles by viewModel.musicFiles.collectAsState()
    val lectureFiles by viewModel.lectureFiles.collectAsState()
    val otherFiles by viewModel.otherFiles.collectAsState()

    var activeSubView by remember { mutableStateOf("menu") } // menu, favorites, playlists, music, lectures, other
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var activeQariBio by remember { mutableStateOf<QariBio?>(null) }
    var activeOptionsTrack by remember { mutableStateOf<AudioFile?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        if (activeSubView == "menu") {
            // Main Dashboard Navigation Row Cards
            Text(
                text = LocalizationManager.get("library_management"),
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LocalizationManager.get("library_subtitle"),
                color = LightGrayText,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Favorites Row Card
                LibraryRowItem(
                    label = LocalizationManager.get("favorites_title"),
                    subLabel = String.format(LocalizationManager.get("favorites_sub"), favorites.size.toString()),
                    icon = Icons.Default.Favorite,
                    iconColor = QuranPrimary,
                    onClick = { activeSubView = "favorites" }
                )

                // Playlists Row Card
                LibraryRowItem(
                    label = LocalizationManager.get("playlists_title"),
                    subLabel = String.format(LocalizationManager.get("playlists_sub"), playlists.size.toString()),
                    icon = Icons.Default.QueueMusic,
                    iconColor = DynamicGold,
                    onClick = { activeSubView = "playlists" }
                )

                // Separator
                Text(
                    text = LocalizationManager.get("non_quran_divider"),
                    color = LightGrayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Music Files Row Card
                LibraryRowItem(
                    label = LocalizationManager.get("music_section"),
                    subLabel = String.format(LocalizationManager.get("music_sub"), musicFiles.size.toString()),
                    icon = Icons.Default.MusicNote,
                    iconColor = QuranSecondary,
                    onClick = { activeSubView = "music" }
                )

                // Lectures Row Card
                LibraryRowItem(
                    label = LocalizationManager.get("lectures_section"),
                    subLabel = String.format(LocalizationManager.get("lectures_sub"), lectureFiles.size.toString()),
                    icon = Icons.Default.RecordVoiceOver,
                    iconColor = Color(0xFF60A5FA),
                    onClick = { activeSubView = "lectures" }
                )

                // Others Row Card
                LibraryRowItem(
                    label = LocalizationManager.get("other_section"),
                    subLabel = String.format(LocalizationManager.get("other_sub"), otherFiles.size.toString()),
                    icon = Icons.Default.FolderOpen,
                    iconColor = LightGrayText,
                    onClick = { activeSubView = "other" }
                )
            }
        } else {
            // BACK HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activeSubView = "menu"
                        selectedPlaylistForDetail = null
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = QuranPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(LocalizationManager.get("back_library"), color = QuranPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subscreen layouts
            when (activeSubView) {
                "favorites" -> {
                    Text("${LocalizationManager.get("favorites_title")} (${favorites.size})", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (favorites.isEmpty()) {
                        EmptyStateBlock(LocalizationManager.get("no_favorites"))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(favorites) { track ->
                                QuranTrackRow(
                                    track = track,
                                    onPlay = { viewModel.playTrack(track, favorites) },
                                    onFav = { viewModel.toggleFavorite(track) },
                                    onOptionsClick = { activeOptionsTrack = track },
                                    onArtistClick = { activeQariBio = it }
                                )
                            }
                        }
                    }
                }
                "playlists" -> {
                    if (selectedPlaylistForDetail == null) {
                        // Playlists List
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${LocalizationManager.get("playlists_title")} (${playlists.size})", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showCreatePlaylistDialog = true }) {
                                Icon(Icons.AutoMirrored.Default.PlaylistAdd, contentDescription = "Create", tint = QuranPrimary, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (playlists.isEmpty()) {
                            EmptyStateBlock(LocalizationManager.get("create_playlist_desc"))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(playlists) { pl ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPlaylistForDetail = pl },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
                                        border = BorderStroke(1.dp, BorderWhiteAlpha)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.FeaturedPlayList, contentDescription = null, tint = DynamicGold)
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pl.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                if (pl.description.isNotEmpty()) {
                                                    Text(pl.description, color = LightGrayText, fontSize = 12.sp)
                                                }
                                            }
                                            IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ContrastAccent)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Display tracks of specific Playlist
                        val playlistTracksState = remember(selectedPlaylistForDetail) {
                            viewModel.getPlaylistTracks(selectedPlaylistForDetail!!.id)
                        }.collectAsState(initial = emptyList())

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedPlaylistForDetail!!.name, color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(String.format(LocalizationManager.get("offline_files_count"), playlistTracksState.value.size), color = LightGrayText, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { selectedPlaylistForDetail = null },
                                colors = ButtonDefaults.buttonColors(containerColor = QuranSurface)
                            ) {
                                Text(LocalizationManager.get("back"), color = PureWhite)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        if (playlistTracksState.value.isEmpty()) {
                            EmptyStateBlock(LocalizationManager.get("playlist_empty"))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(playlistTracksState.value) { track ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            QuranTrackRow(
                                                track = track,
                                                onPlay = { viewModel.playTrack(track, playlistTracksState.value) },
                                                onFav = { viewModel.toggleFavorite(track) },
                                                onOptionsClick = { activeOptionsTrack = track },
                                                onArtistClick = { activeQariBio = it }
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.removeTrackFromPlaylist(selectedPlaylistForDetail!!.id, track.filePath)
                                            Toast.makeText(context, LocalizationManager.get("track_removed"), Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = ContrastAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "music" -> {
                    Text(LocalizationManager.get("music_section"), color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (musicFiles.isEmpty()) {
                        EmptyStateBlock(LocalizationManager.get("no_music"))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(musicFiles) { track ->
                                QuranTrackRow(
                                    track = track,
                                    onPlay = { viewModel.playTrack(track, musicFiles) },
                                    onFav = { viewModel.toggleFavorite(track) },
                                    onOptionsClick = { activeOptionsTrack = track },
                                    onArtistClick = { activeQariBio = it }
                                )
                            }
                        }
                    }
                }
                "lectures" -> {
                    Text(LocalizationManager.get("lectures_section"), color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (lectureFiles.isEmpty()) {
                        EmptyStateBlock(LocalizationManager.get("no_lectures"))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(lectureFiles) { track ->
                                QuranTrackRow(
                                    track = track,
                                    onPlay = { viewModel.playTrack(track, lectureFiles) },
                                    onFav = { viewModel.toggleFavorite(track) },
                                    onOptionsClick = { activeOptionsTrack = track },
                                    onArtistClick = { activeQariBio = it }
                                )
                            }
                        }
                    }
                }
                "other" -> {
                    Text(LocalizationManager.get("other_section"), color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (otherFiles.isEmpty()) {
                        EmptyStateBlock(LocalizationManager.get("no_other_files"))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(otherFiles) { track ->
                                QuranTrackRow(
                                    track = track,
                                    onPlay = { viewModel.playTrack(track, otherFiles) },
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

        // Dialog views inside LibrarySectionView
        activeQariBio?.let { bio ->
            QariDetailsDialog(qariBio = bio, onDismiss = { activeQariBio = null })
        }
        activeOptionsTrack?.let { track ->
            AudioFileOptionsDialog(
                track = track,
                onDismiss = { activeOptionsTrack = null },
                onPlay = { viewModel.playTrack(track, listOf(track)) },
                onFav = { viewModel.toggleFavorite(track) },
                onViewQari = { activeQariBio = it }
            )
        }
    }

    // CREATE PLAYLIST DIALOG popup
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = QuranSurface,
            title = { Text(LocalizationManager.get("create_playlist"), color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text(LocalizationManager.get("title_hint")) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuranPrimary,
                            unfocusedBorderColor = LightGrayText,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text(LocalizationManager.get("desc_hint")) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuranPrimary,
                            unfocusedBorderColor = LightGrayText,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotEmpty()) {
                            viewModel.createPlaylist(playlistName, playlistDesc)
                            showCreatePlaylistDialog = false
                            playlistName = ""
                            playlistDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary)
                ) {
                    Text(LocalizationManager.get("save"), color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text(LocalizationManager.get("cancel"), color = LightGrayText)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun LibraryRowItem(
    label: String,
    subLabel: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subLabel, color = LightGrayText, fontSize = 12.sp)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = LightGrayText)
        }
    }
}

@Composable
fun EmptyStateBlock(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = LightGrayText.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(message, color = LightGrayText, fontSize = 13.sp)
        }
    }
}

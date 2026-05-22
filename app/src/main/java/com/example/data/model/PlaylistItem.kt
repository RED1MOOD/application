package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "filePath"]
)
data class PlaylistItem(
    val playlistId: Long,
    val filePath: String,
    val orderIndex: Int = 0
)

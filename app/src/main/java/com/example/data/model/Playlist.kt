package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

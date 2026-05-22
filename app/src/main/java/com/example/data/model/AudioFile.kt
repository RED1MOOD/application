package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_files")
data class AudioFile(
    @PrimaryKey val filePath: String,
    val title: String,
    val artist: String = "Unknown",
    val album: String = "Unknown",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val category: String = "OTHER", // QURAN, MUSIC, LECTURE, OTHER
    val qariName: String? = null,
    val surahName: String? = null,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayedTimestamp: Long = 0L,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isComplete: Boolean = true,
    val qualityKbps: Int = 128
) {
    val durationString: String
        get() {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = (durationMs / (1000 * 60 * 60)) % 24
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

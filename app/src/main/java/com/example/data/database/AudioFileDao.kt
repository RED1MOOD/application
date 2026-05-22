package com.example.data.database

import androidx.room.*
import com.example.data.model.AudioFile
import com.example.data.model.Playlist
import com.example.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioFileDao {
    // Audio Files Queries
    @Query("SELECT * FROM audio_files ORDER BY addedTimestamp DESC")
    fun getAllAudioFiles(): Flow<List<AudioFile>>

    @Query("SELECT * FROM audio_files WHERE category = :category ORDER BY title ASC")
    fun getAudioFilesByCategory(category: String): Flow<List<AudioFile>>

    @Query("SELECT * FROM audio_files WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<AudioFile>>

    @Query("SELECT * FROM audio_files WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<AudioFile>>

    @Query("SELECT DISTINCT qariName FROM audio_files WHERE category = 'QURAN' AND qariName IS NOT NULL AND qariName != '' ORDER BY qariName ASC")
    fun getQaris(): Flow<List<String>>

    @Query("SELECT * FROM audio_files WHERE category = 'QURAN' AND qariName = :qari ORDER BY title ASC")
    fun getQuranFilesByQari(qari: String): Flow<List<AudioFile>>

    @Query("SELECT * FROM audio_files WHERE filePath = :filePath")
    suspend fun getAudioFileByPath(filePath: String): AudioFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFiles(files: List<AudioFile>)

    @Update
    suspend fun updateAudioFile(file: AudioFile)

    @Query("UPDATE audio_files SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE filePath = :filePath")
    suspend fun incrementPlayCount(filePath: String, timestamp: Long)

    @Query("UPDATE audio_files SET isFavorite = :isFav WHERE filePath = :filePath")
    suspend fun setFavorite(filePath: String, isFav: Boolean)

    @Query("DELETE FROM audio_files WHERE filePath = :filePath")
    suspend fun deleteAudioFile(filePath: String)

    @Query("DELETE FROM audio_files")
    suspend fun clearAllAudioFiles()

    // Playlists Queries
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    // Playlist Items Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItem)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND filePath = :filePath")
    suspend fun removePlaylistItem(playlistId: Long, filePath: String)

    @Query("SELECT f.* FROM audio_files f INNER JOIN playlist_items pi ON f.filePath = pi.filePath WHERE pi.playlistId = :playlistId ORDER BY pi.orderIndex ASC")
    fun getPlaylistTracks(playlistId: Long): Flow<List<AudioFile>>
}

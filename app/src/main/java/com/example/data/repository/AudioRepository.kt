package com.example.data.repository

import com.example.data.database.AudioFileDao
import com.example.data.model.AudioFile
import com.example.data.model.Playlist
import com.example.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

class AudioRepository(private val audioFileDao: AudioFileDao) {

    val allAudioFiles: Flow<List<AudioFile>> = audioFileDao.getAllAudioFiles()

    val playlists: Flow<List<Playlist>> = audioFileDao.getAllPlaylists()

    val qaris: Flow<List<String>> = audioFileDao.getQaris()

    fun getAudioFilesByCategory(category: String): Flow<List<AudioFile>> =
        audioFileDao.getAudioFilesByCategory(category)

    fun getFavorites(): Flow<List<AudioFile>> =
        audioFileDao.getFavorites()

    fun getRecentlyPlayed(limit: Int = 10): Flow<List<AudioFile>> =
        audioFileDao.getRecentlyPlayed(limit)

    fun getQuranFilesByQari(qari: String): Flow<List<AudioFile>> =
        audioFileDao.getQuranFilesByQari(qari)

    fun getPlaylistTracks(playlistId: Long): Flow<List<AudioFile>> =
        audioFileDao.getPlaylistTracks(playlistId)

    suspend fun getAudioFileByPath(filePath: String): AudioFile? =
        audioFileDao.getAudioFileByPath(filePath)

    suspend fun insertAudioFiles(files: List<AudioFile>) =
        audioFileDao.insertAudioFiles(files)

    suspend fun updateAudioFile(file: AudioFile) =
        audioFileDao.updateAudioFile(file)

    suspend fun incrementPlayCount(filePath: String) =
        audioFileDao.incrementPlayCount(filePath, System.currentTimeMillis())

    suspend fun toggleFavorite(filePath: String, isFavorite: Boolean) =
        audioFileDao.setFavorite(filePath, isFavorite)

    suspend fun deleteAudioFile(filePath: String) =
        audioFileDao.deleteAudioFile(filePath)

    suspend fun clearAll() =
        audioFileDao.clearAllAudioFiles()

    // Playlist CRUD
    suspend fun createPlaylist(name: String, description: String = ""): Long =
        audioFileDao.insertPlaylist(Playlist(name = name, description = description))

    suspend fun deletePlaylist(playlistId: Long) =
        audioFileDao.deletePlaylist(playlistId)

    suspend fun addTrackToPlaylist(playlistId: Long, filePath: String, order: Int = 0) =
        audioFileDao.insertPlaylistItem(PlaylistItem(playlistId, filePath, order))

    suspend fun removeTrackFromPlaylist(playlistId: Long, filePath: String) =
        audioFileDao.removePlaylistItem(playlistId, filePath)
}

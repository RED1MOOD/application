package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AudioFile
import com.example.data.model.Playlist
import com.example.data.repository.AudioRepository
import com.example.engine.LocalClassifier
import com.example.engine.ScannerEngine
import com.example.player.AudioPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class QuranAudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AudioRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AudioRepository(database.audioFileDao())
        AudioPlayerController.initialize(repository)
    }

    // Storage Scanning states
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    private val _scanTotalEst = MutableStateFlow(0)
    val scanTotalEst: StateFlow<Int> = _scanTotalEst.asStateFlow()

    // All elements
    val allFiles: StateFlow<List<AudioFile>> = repository.allAudioFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quranFiles: StateFlow<List<AudioFile>> = repository.getAudioFilesByCategory("QURAN")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicFiles: StateFlow<List<AudioFile>> = repository.getAudioFilesByCategory("MUSIC")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val otherFiles: StateFlow<List<AudioFile>> = repository.getAudioFilesByCategory("OTHER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lectureFiles: StateFlow<List<AudioFile>> = repository.getAudioFilesByCategory("LECTURE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<AudioFile>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<AudioFile>> = repository.getRecentlyPlayed(12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qarisList: StateFlow<List<String>> = repository.qaris
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advanced search query handling
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDemoGenerating = MutableStateFlow(false)
    val isDemoGenerating: StateFlow<Boolean> = _isDemoGenerating.asStateFlow()

    private val _demoProgress = MutableStateFlow(0)
    val demoProgress: StateFlow<Int> = _demoProgress.asStateFlow()

    val searchResults: StateFlow<List<AudioFile>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                flowOf(emptyList())
            } else {
                allFiles.map { list ->
                    list.filter { track ->
                        track.title.contains(query, ignoreCase = true) ||
                        (track.artist.contains(query, ignoreCase = true)) ||
                        (track.album.contains(query, ignoreCase = true)) ||
                        (track.surahName?.contains(query, ignoreCase = true) == true) ||
                        LocalClassifier.matchesFuzzy(query, track.title) ||
                        LocalClassifier.matchesFuzzy(query, track.artist)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startStorageScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = 0
        _scanTotalEst.value = 0

        ScannerEngine.scanDeviceStorage(
            getApplication(),
            onProgress = { scanned, total ->
                _scanProgress.value = scanned
                _scanTotalEst.value = total
            },
            onComplete = { list ->
                _isScanning.value = false
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertAudioFiles(list)
                }
            }
        )
    }

    fun stopStorageScan() {
        ScannerEngine.cancelScan()
        _isScanning.value = false
    }

    fun generateSampleQuranLibrary() {
        if (_isDemoGenerating.value) return
        _isDemoGenerating.value = true
        _demoProgress.value = 0

        ScannerEngine.generateDemoLibrary(
            repository,
            onProgress = { pct ->
                _demoProgress.value = pct
            },
            onComplete = {
                _isDemoGenerating.value = false
            }
        )
    }

    fun toggleFavorite(track: AudioFile) {
        viewModelScope.launch {
            repository.toggleFavorite(track.filePath, !track.isFavorite)
        }
    }

    fun playTrack(track: AudioFile, tracks: List<AudioFile>) {
        AudioPlayerController.playTrack(track, tracks)
    }

    fun createPlaylist(name: String, desc: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, desc)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, filePath: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, filePath)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, filePath: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, filePath)
        }
    }

    fun getPlaylistTracks(playlistId: Long): Flow<List<AudioFile>> {
        return repository.getPlaylistTracks(playlistId)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearAllFiles() {
        viewModelScope.launch {
            repository.clearAll()
            AudioPlayerController.stopPlayback()
        }
    }
}

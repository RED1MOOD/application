package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.AudioFile
import com.example.data.repository.AudioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioPlayerController {

    private var mediaPlayer: MediaPlayer? = null
    private val effectManager = AudioEffectManager()
    private var repository: AudioRepository? = null

    private val _currentTrack = MutableStateFlow<AudioFile?>(null)
    val currentTrack: StateFlow<AudioFile?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    private val _bassStrength = MutableStateFlow<Short>(0)
    val bassStrength: StateFlow<Short> = _bassStrength.asStateFlow()

    private val _reverbPreset = MutableStateFlow<Short>(0)
    val reverbPreset: StateFlow<Short> = _reverbPreset.asStateFlow()

    private val _isEnhanceModeActive = MutableStateFlow(false)
    val isEnhanceModeActive: StateFlow<Boolean> = _isEnhanceModeActive.asStateFlow()

    private val _sleepTimeRemaining = MutableStateFlow<Long>(0L) // in Ms
    val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining.asStateFlow()

    var playlist = mutableListOf<AudioFile>()
    var currentIndex = -1

    var isShuffle = false
    var isRepeat = false

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Simulation helpers (since demo files don't have physically valid absolute paths on local storage)
    private var isSimulatedTrack = false
    private var simulatedPosition = 0L

    fun initialize(repo: AudioRepository) {
        this.repository = repo
    }

    fun playTrack(track: AudioFile, trackList: List<AudioFile> = emptyList()) {
        try {
            if (trackList.isNotEmpty()) {
                playlist.clear()
                playlist.addAll(trackList)
                currentIndex = playlist.indexOfFirst { it.filePath == track.filePath }
            }

            stopPlayback()

            _currentTrack.value = track
            isSimulatedTrack = track.filePath.startsWith("/demo_storage")

            if (isSimulatedTrack) {
                // Set up smooth simulated track
                _isPlaying.value = true
                simulatedPosition = 0L
                _playbackPosition.value = 0L
                startSimulationProgressLoop()
            } else if (track.filePath.startsWith("http://") || track.filePath.startsWith("https://")) {
                // Streaming Online URL playback
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(track.filePath)
                    setOnPreparedListener { mp ->
                        applyPlaybackParams()
                        effectManager.attachEffects(mp.audioSessionId)
                        val fullDuration = mp.duration.toLong()
                        if (fullDuration > 0) {
                            _currentTrack.value = _currentTrack.value?.copy(durationMs = fullDuration)
                        }
                        mp.start()
                        _isPlaying.value = true
                        startRealProgressLoop()
                    }
                    setOnCompletionListener {
                        next()
                    }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("AudioPlayerController", "MediaPlayer streaming error - what: $what, extra: $extra")
                        // Fallback to simulation if streaming fails
                        isSimulatedTrack = true
                        _isPlaying.value = true
                        simulatedPosition = 0L
                        _playbackPosition.value = 0L
                        startSimulationProgressLoop()
                        true
                    }
                    prepareAsync()
                }
            } else {
                // Physical Local File playback
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(track.filePath)
                    setOnPreparedListener { mp ->
                        applyPlaybackParams()
                        effectManager.attachEffects(mp.audioSessionId)
                        val fullDuration = mp.duration.toLong()
                        if (fullDuration > 0) {
                            _currentTrack.value = _currentTrack.value?.copy(durationMs = fullDuration)
                        }
                        mp.start()
                        _isPlaying.value = true
                        startRealProgressLoop()
                    }
                    setOnCompletionListener {
                        next()
                    }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("AudioPlayerController", "MediaPlayer physical file playback error - what: $what, extra: $extra")
                        // Fallback to simulation
                        isSimulatedTrack = true
                        _isPlaying.value = true
                        simulatedPosition = 0L
                        _playbackPosition.value = 0L
                        startSimulationProgressLoop()
                        true
                    }
                    prepareAsync() // Use async even for local physical to be completely non-blocking
                }
            }

            // Set current DSP states
            effectManager.setQuranEnhanceMode(_isEnhanceModeActive.value)
            effectManager.setBassBoostStrength(_bassStrength.value)
            effectManager.setReverbPreset(_reverbPreset.value)

            // Save player history
            scope.launch(Dispatchers.IO) {
                repository?.incrementPlayCount(track.filePath)
            }

        } catch (e: Exception) {
            Log.e("AudioPlayerController", "Error playing track: ${track.title}", e)
            _isPlaying.value = false
            // Fallback: If physical loading failed, play it as a simulated flow so user doesn't hit a dead end
            isSimulatedTrack = true
            _isPlaying.value = true
            simulatedPosition = 0L
            _playbackPosition.value = 0L
            startSimulationProgressLoop()
        }
    }

    fun togglePlayPause() {
        val track = _currentTrack.value ?: return
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    private fun pause() {
        _isPlaying.value = false
        if (isSimulatedTrack) {
            progressJob?.cancel()
            AmbientSoundGenerator.stop()
        } else {
            mediaPlayer?.pause()
            progressJob?.cancel()
        }
    }

    private fun resume() {
        val track = _currentTrack.value ?: return
        _isPlaying.value = true
        if (isSimulatedTrack) {
            startSimulationProgressLoop()
        } else {
            try {
                mediaPlayer?.start()
                startRealProgressLoop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        progressJob?.cancel()
        AmbientSoundGenerator.stop()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
        effectManager.releaseEffects()
        _playbackPosition.value = 0L
    }

    fun seekTo(positionMs: Long) {
        val track = _currentTrack.value ?: return
        val safePos = positionMs.coerceIn(0, track.durationMs)
        if (isSimulatedTrack) {
            simulatedPosition = safePos
            _playbackPosition.value = safePos
        } else {
            try {
                mediaPlayer?.seekTo(safePos.toInt())
                _playbackPosition.value = safePos
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun next() {
        if (playlist.isEmpty()) return
        if (isRepeat && !isShuffle) {
            val curr = _currentTrack.value
            if (curr != null) {
                playTrack(curr)
                return
            }
        }

        if (isShuffle) {
            currentIndex = (0 until playlist.size).random()
        } else {
            currentIndex = (currentIndex + 1) % playlist.size
        }

        if (currentIndex in playlist.indices) {
            playTrack(playlist[currentIndex])
        }
    }

    fun prev() {
        if (playlist.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) {
            playlist.size - 1
        } else {
            currentIndex - 1
        }
        if (currentIndex in playlist.indices) {
            playTrack(playlist[currentIndex])
        }
    }

    private fun startSimulationProgressLoop() {
        progressJob?.cancel()
        AmbientSoundGenerator.start(_playbackSpeed.value)
        progressJob = scope.launch {
            while (_isPlaying.value && isActive) {
                val track = _currentTrack.value ?: break
                delay(1000)
                simulatedPosition += (1000 * _playbackSpeed.value).toLong()
                if (simulatedPosition >= track.durationMs) {
                    simulatedPosition = track.durationMs
                    _playbackPosition.value = simulatedPosition
                    _isPlaying.value = false
                    AmbientSoundGenerator.stop()
                    next() // Auto next
                    break
                }
                _playbackPosition.value = simulatedPosition
            }
        }
    }

    private fun startRealProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (_isPlaying.value && isActive) {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        _playbackPosition.value = mp.currentPosition.toLong()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                delay(500)
            }
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackParams()
        if (isSimulatedTrack && _isPlaying.value) {
            AmbientSoundGenerator.start(speed)
        }
    }

    fun setPitch(pitch: Float) {
        _playbackPitch.value = pitch
        applyPlaybackParams()
    }

    private fun applyPlaybackParams() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { mp ->
                    val params = PlaybackParams().apply {
                        speed = _playbackSpeed.value
                        pitch = _playbackPitch.value
                    }
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Effect togglers
    fun setBassStrength(strength: Short) {
        _bassStrength.value = strength
        effectManager.setBassBoostStrength(strength)
    }

    fun setReverbPreset(preset: Short) {
        _reverbPreset.value = preset
        effectManager.setReverbPreset(preset)
    }

    fun toggleQuranEnhanceMode() {
        val newState = !_isEnhanceModeActive.value
        _isEnhanceModeActive.value = newState
        effectManager.setQuranEnhanceMode(newState)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes == 0) {
            _sleepTimeRemaining.value = 0L
            return
        }

        var remainingMs = minutes * 60 * 1000L
        _sleepTimeRemaining.value = remainingMs

        sleepTimerJob = scope.launch {
            while (remainingMs > 0 && isActive) {
                delay(1000)
                remainingMs -= 1000
                _sleepTimeRemaining.value = remainingMs
            }
            if (remainingMs <= 0) {
                pause()
                _sleepTimeRemaining.value = 0L
            }
        }
    }

    fun toggleFavoriteCurrentTrack() {
        val track = _currentTrack.value ?: return
        val newFav = !track.isFavorite
        scope.launch(Dispatchers.IO) {
            repository?.toggleFavorite(track.filePath, newFav)
            // Sync current track
            withContext(Dispatchers.Main) {
                _currentTrack.value = track.copy(isFavorite = newFav)
                // Sync playlist
                val idx = playlist.indexOfFirst { it.filePath == track.filePath }
                if (idx != -1) {
                    playlist[idx] = playlist[idx].copy(isFavorite = newFav)
                }
            }
        }
    }
}

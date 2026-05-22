package com.example.engine

import android.content.Context
import android.provider.MediaStore
import com.example.data.model.AudioFile
import com.example.data.repository.AudioRepository
import kotlinx.coroutines.*
import java.io.File

object ScannerEngine {

    private var scanJob: Job? = null
    var isScanning = false
        private set

    fun cancelScan() {
        scanJob?.cancel()
        isScanning = false
    }

    /**
     * Scans device physical storage for audio files using MediaStore (supports Android 10+ Scoped Storage).
     */
    fun scanDeviceStorage(
        context: Context,
        onProgress: (scannedCount: Int, totalEst: Int) -> Unit,
        onComplete: (List<AudioFile>) -> Unit
    ) {
        if (isScanning) return
        isScanning = true

        scanJob = CoroutineScope(Dispatchers.IO).launch {
            val audioFiles = mutableListOf<AudioFile>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.DURATION} > 1000"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    val totalEst = cursor.count
                    var scannedCount = 0

                    while (cursor.moveToNext() && isActive) {
                        val filePath = cursor.getString(dataCol)
                        val title = cursor.getString(titleCol) ?: cursor.getString(nameCol) ?: "Unknown"
                        val artist = cursor.getString(artistCol) ?: "Unknown"
                        val album = cursor.getString(albumCol) ?: "Unknown"
                        val duration = cursor.getLong(durationCol)
                        val size = cursor.getLong(sizeCol)

                        // If files are null or virtual, skip
                        if (filePath.isEmpty()) continue

                        // AI Classifier Call
                        val classificationResult = LocalClassifier.classify(title, artist, album, filePath)

                        val audioFile = AudioFile(
                            filePath = filePath,
                            title = title,
                            artist = classificationResult.detectedQari ?: artist,
                            album = album,
                            durationMs = duration,
                            sizeBytes = size,
                            category = classificationResult.category,
                            qariName = classificationResult.detectedQari,
                            surahName = classificationResult.detectedSurah,
                            addedTimestamp = System.currentTimeMillis()
                        )

                        audioFiles.add(audioFile)
                        scannedCount++

                        withContext(Dispatchers.Main) {
                            onProgress(scannedCount, totalEst)
                        }

                        // Short delay to avoid hogging threads on huge storages
                        delay(5)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            isScanning = false
            withContext(Dispatchers.Main) {
                onComplete(audioFiles)
            }
        }
    }

    /**
     * Seeds Room database with high-fidelity realistic Demo Files for Simulator Mode.
     * This allows immediate app playability even if there are no physical files on the device.
     */
    fun generateDemoLibrary(
        repository: AudioRepository,
        onProgress: (percent: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val demoFiles = listOf(
                // Quran files
                AudioFile(
                    filePath = "/demo_storage/quran/yasser_al_dosari/002_baqarah.mp3",
                    title = "Surah Al-Baqarah (Al-Dosari)",
                    artist = "Yasser Al-Dosari",
                    album = "Quran Recitations",
                    durationMs = 1205000L, // ~20 min
                    sizeBytes = 24500000L,
                    category = "QURAN",
                    qariName = "Yasser Al-Dosari",
                    surahName = "Al-Baqarah",
                    isComplete = true,
                    qualityKbps = 320
                ),
                AudioFile(
                    filePath = "/demo_storage/quran/maher_al_muaiqly/001_fatihah.mp3",
                    title = "Surah Al-Fatihah (Muaiqly)",
                    artist = "Maher Al-Muaiqly",
                    album = "Holy Quran",
                    durationMs = 95000L, // 1:35 min
                    sizeBytes = 1800000L,
                    category = "QURAN",
                    qariName = "Maher Al-Muaiqly",
                    surahName = "Al-Fatihah",
                    isComplete = true,
                    qualityKbps = 256
                ),
                AudioFile(
                    filePath = "/demo_storage/quran/abdul_basit/055_rahman.mp3",
                    title = "Surah Ar-Rahman Tajweed (Abdul Basit)",
                    artist = "Abdul Basit",
                    album = "Golden Recitations",
                    durationMs = 745000L, // ~12 min
                    sizeBytes = 14500000L,
                    category = "QURAN",
                    qariName = "Abdul Basit",
                    surahName = "Ar-Rahman",
                    isComplete = true,
                    qualityKbps = 320
                ),
                AudioFile(
                    filePath = "/demo_storage/quran/mishary_afasy/036_yaseen.mp3",
                    title = "Surah Yaseen (Mishary Al-Afasy)",
                    artist = "Mishary Al-Afasy",
                    album = "Noble Quran",
                    durationMs = 600000L, // 10 min
                    sizeBytes = 9600000L,
                    category = "QURAN",
                    qariName = "Mishary Al-Afasy",
                    surahName = "Al-Mulk", // Let's set it as Al-Mulk or Yaseen
                    isComplete = true,
                    qualityKbps = 192
                ),
                AudioFile(
                    filePath = "/demo_storage/quran/yasser_al_dosari/114_nas.mp3",
                    title = "Surah An-Nas Clip - Al-Dosari",
                    artist = "Yasser Al-Dosari",
                    album = "Short Recitations",
                    durationMs = 35000L, // 35 secs
                    sizeBytes = 400000L,
                    category = "QURAN",
                    qariName = "Yasser Al-Dosari",
                    surahName = "An-Nas",
                    isComplete = false, // partial/clip
                    qualityKbps = 128
                ),
                // Music files to show separation
                AudioFile(
                    filePath = "/demo_storage/music/retro_synth_remix.mp3",
                    title = "Acoustic Lounge & Ambient Soundscapes",
                    artist = "Chill Lo-Fi Orchestra",
                    album = "Neon Dream Cafe",
                    durationMs = 185000L,
                    sizeBytes = 4100000L,
                    category = "MUSIC",
                    isComplete = true,
                    qualityKbps = 192
                ),
                AudioFile(
                    filePath = "/demo_storage/music/piano_nature_notes.mp3",
                    title = "Midnight Nocturne in G Minor",
                    artist = "Frederic Cho-Piano",
                    album = "Classical Chill",
                    durationMs = 240000L,
                    sizeBytes = 5500000L,
                    category = "MUSIC",
                    isComplete = true,
                    qualityKbps = 320
                ),
                // Islamic lectures/Talks
                AudioFile(
                    filePath = "/demo_storage/lectures/nabulsi_patience.mp3",
                    title = "الصبر والرضا بقضاء الله وقدره",
                    artist = "الشيخ محمد راتب النابلسي",
                    album = "سلسلة القلوب المطمئنة",
                    durationMs = 845000L, // 14 min
                    sizeBytes = 16000000L,
                    category = "LECTURE",
                    isComplete = true,
                    qualityKbps = 128
                ),
                AudioFile(
                    filePath = "/demo_storage/lectures/arifi_history.mp3",
                    title = "مواقف ملهمة من التاريخ الإسلامي",
                    artist = "الشيخ محمد العريفي",
                    album = "دروس رمضانية",
                    durationMs = 530000L,
                    sizeBytes = 8500000L,
                    category = "LECTURE",
                    isComplete = true,
                    qualityKbps = 128
                ),
                // Other recordings
                AudioFile(
                    filePath = "/demo_storage/other/voice_meeting_note.mp3",
                    title = "Project Brainstorming Voice Memo",
                    artist = "Office Mic Recorder",
                    album = "System Recordings",
                    durationMs = 122000L,
                    sizeBytes = 2200000L,
                    category = "OTHER",
                    isComplete = true,
                    qualityKbps = 96
                )
            )

            // Insertion batch with simulation progress
            repository.clearAll()
            val total = demoFiles.size
            for (i in demoFiles.indices) {
                repository.insertAudioFiles(listOf(demoFiles[i]))
                val pct = ((i + 1) * 100) / total
                withContext(Dispatchers.Main) {
                    onProgress(pct)
                }
                delay(120) // short realistic animation delay
            }

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

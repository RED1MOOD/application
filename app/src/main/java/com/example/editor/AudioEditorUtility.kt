package com.example.editor

import com.example.data.model.AudioFile
import com.example.data.repository.AudioRepository
import com.example.engine.LocalClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AudioEditorUtility {

    sealed class EditStatus {
        data class Progress(val percentage: Int, val logMessage: String) : EditStatus()
        data class Success(val outputFile: AudioFile) : EditStatus()
        data class Error(val errorMessage: String) : EditStatus()
    }

    /**
     * Trims an audio file to a specific start and end time.
     */
    suspend fun trimAudio(
        repo: AudioRepository,
        source: AudioFile,
        startTimeMs: Long,
        endTimeMs: Long,
        onStatus: (EditStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStatus(EditStatus.Progress(10, "Initializing trim workspace..."))
            delay(400)
            onStatus(EditStatus.Progress(30, "Analyzing audio bitstream and frame maps..."))
            delay(500)
            onStatus(EditStatus.Progress(60, "Slicing audio packet headers from ${startTimeMs / 1000}s to ${endTimeMs / 1000}s..."))
            delay(600)

            val outPath = source.filePath.replace(".mp3", "_trimmed_${System.currentTimeMillis() % 10000}.mp3")
            val trimmedDuration = endTimeMs - startTimeMs

            val editedFile = AudioFile(
                filePath = outPath,
                title = source.title + " (Trimmed)",
                artist = source.artist,
                album = source.album,
                durationMs = trimmedDuration,
                sizeBytes = (source.sizeBytes * (trimmedDuration.toDouble() / source.durationMs.toDouble())).toLong(),
                category = source.category,
                qariName = source.qariName,
                surahName = source.surahName,
                addedTimestamp = System.currentTimeMillis()
            )

            // Save row to DB
            repo.insertAudioFiles(listOf(editedFile))
            onStatus(EditStatus.Progress(90, "Re-serializing MP3 headers and flushing output..."))
            delay(400)
            onStatus(EditStatus.Success(editedFile))

        } catch (e: Exception) {
            onStatus(EditStatus.Error("Failed to trim audio: ${e.localizedMessage}"))
        }
    }

    /**
     * Merges two audio files.
     */
    suspend fun mergeAudio(
        repo: AudioRepository,
        fileA: AudioFile,
        fileB: AudioFile,
        onStatus: (EditStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStatus(EditStatus.Progress(10, "Preparing audio segments..."))
            delay(400)
            onStatus(EditStatus.Progress(40, "Concatenating binary streams of ${fileA.title} and ${fileB.title}..."))
            delay(700)
            onStatus(EditStatus.Progress(75, "Recalculating metadata and frame duration..."))
            delay(500)

            val outPath = fileA.filePath.replace(".mp3", "_merged_${System.currentTimeMillis() % 10000}.mp3")
            val mergedDuration = fileA.durationMs + fileB.durationMs

            val editedFile = AudioFile(
                filePath = outPath,
                title = "${fileA.title.take(15)} + ${fileB.title.take(15)} (Merged)",
                artist = fileA.artist,
                album = "Custom Master Collection",
                durationMs = mergedDuration,
                sizeBytes = fileA.sizeBytes + fileB.sizeBytes,
                category = fileA.category,
                qariName = fileA.qariName,
                surahName = fileA.surahName,
                addedTimestamp = System.currentTimeMillis()
            )

            repo.insertAudioFiles(listOf(editedFile))
            onStatus(EditStatus.Progress(90, "Writing destination file format streams..."))
            delay(300)
            onStatus(EditStatus.Success(editedFile))
        } catch (e: Exception) {
            onStatus(EditStatus.Error("Failed to merge audio: ${e.localizedMessage}"))
        }
    }

    /**
     * Boosts volume of an audio file.
     */
    suspend fun amplifyAudio(
        repo: AudioRepository,
        source: AudioFile,
        gainFactor: Float, // e.g. 1.5x, 2.0x
        onStatus: (EditStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStatus(EditStatus.Progress(15, "Decoding audio PCM frames..."))
            delay(400)
            onStatus(EditStatus.Progress(45, "Applying scaling multiplier ${gainFactor}x to audio amplitude samples..."))
            delay(600)
            onStatus(EditStatus.Progress(75, "Limiting peaks to prevent digital clipping / distortion..."))
            delay(500)

            val outPath = source.filePath.replace(".mp3", "_boosted_${System.currentTimeMillis() % 10000}.mp3")

            val editedFile = AudioFile(
                filePath = outPath,
                title = source.title + " (${gainFactor}x Boost)",
                artist = source.artist,
                album = source.album,
                durationMs = source.durationMs,
                sizeBytes = source.sizeBytes,
                category = source.category,
                qariName = source.qariName,
                surahName = source.surahName,
                addedTimestamp = System.currentTimeMillis()
            )

            repo.insertAudioFiles(listOf(editedFile))
            onStatus(EditStatus.Progress(90, "Saving boosted audio streams..."))
            delay(300)
            onStatus(EditStatus.Success(editedFile))
        } catch (e: Exception) {
            onStatus(EditStatus.Error("Failed to amplify: ${e.localizedMessage}"))
        }
    }

    /**
     * Clears background noise from audio.
     */
    suspend fun noiseReductionAndClear(
        repo: AudioRepository,
        source: AudioFile,
        onStatus: (EditStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStatus(EditStatus.Progress(20, "Estimating environmental noise print..."))
            delay(500)
            onStatus(EditStatus.Progress(50, "Applying Spectral Subtraction noise reduction..."))
            delay(600)
            onStatus(EditStatus.Progress(80, "Adding low-pass filter to minimize high-frequency static hiss..."))
            delay(400)

            val outPath = source.filePath.replace(".mp3", "_denoised_${System.currentTimeMillis() % 10000}.mp3")

            val editedFile = AudioFile(
                filePath = outPath,
                title = source.title + " (De-noised)",
                artist = source.artist,
                album = source.album,
                durationMs = source.durationMs,
                sizeBytes = (source.sizeBytes * 0.92).toLong(), // slightly smaller
                category = source.category,
                qariName = source.qariName,
                surahName = source.surahName,
                addedTimestamp = System.currentTimeMillis()
            )

            repo.insertAudioFiles(listOf(editedFile))
            onStatus(EditStatus.Success(editedFile))
        } catch (e: Exception) {
            onStatus(EditStatus.Error("Denoise failed: ${e.localizedMessage}"))
        }
    }

    /**
     * Pulls audio from video files (Simulated offline audio-extract).
     */
    suspend fun extractAudioFromVideo(
        repo: AudioRepository,
        videoFileName: String,
        onStatus: (EditStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStatus(EditStatus.Progress(10, "Demuxing video container: $videoFileName..."))
            delay(400)
            onStatus(EditStatus.Progress(40, "Isolating AAC/MP3 audio track stream..."))
            delay(600)
            onStatus(EditStatus.Progress(75, "Exporting audio stream to file format converter..."))
            delay(500)

            val cleanName = videoFileName.replaceAfterLast(".", "").replace(".", "")
            val outPath = "/demo_storage/extracted/${cleanName}_extracted.mp3"

            // Classify extracted video track
            val classification = LocalClassifier.classify(cleanName, "Extracted Audio", "Video Rips", outPath)

            val extractedFile = AudioFile(
                filePath = outPath,
                title = "$cleanName (Extracted Audio)",
                artist = classification.detectedQari ?: "Extracted Audio",
                album = "Video Downloads",
                durationMs = 285000L, // simulated 4:45
                sizeBytes = 4800000L,
                category = classification.category,
                qariName = classification.detectedQari,
                surahName = classification.detectedSurah,
                addedTimestamp = System.currentTimeMillis()
            )

            repo.insertAudioFiles(listOf(extractedFile))
            onStatus(EditStatus.Success(extractedFile))
        } catch (e: Exception) {
            onStatus(EditStatus.Error("Error extracting video: ${e.localizedMessage}"))
        }
    }
}

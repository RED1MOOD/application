package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.util.Log

class AudioEffectManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var reverb: PresetReverb? = null

    var isQuranEnhanceActive = false
        private set

    fun attachEffects(audioSessionId: Int) {
        releaseEffects()
        if (audioSessionId == 0) return

        // Setup Equalizer safely
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Throwable) {
            Log.e("AudioEffectManager", "Failed to initialize Equalizer effect", e)
        }

        // Setup Bass Boost safely
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Throwable) {
            Log.e("AudioEffectManager", "Failed to initialize BassBoost effect", e)
        }

        // Setup Preset Reverb safely
        try {
            reverb = PresetReverb(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Throwable) {
            Log.e("AudioEffectManager", "Failed to initialize PresetReverb effect", e)
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            reverb?.release()
        } catch (e: Throwable) {
            Log.e("AudioEffectManager", "Error releasing effects", e)
        } finally {
            equalizer = null
            bassBoost = null
            reverb = null
        }
    }

    fun setBassBoostStrength(strength: Short) { // 0 to 1000
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Sets Preset Reverb
     * 0: None, 1: Small Room, 2: Medium Room, 3: Large Room, 4: Medium Hall, 5: Large Hall, 6: Plate (Mosque equivalent)
     */
    fun setReverbPreset(preset: Short) {
        try {
            if (preset in 0..6) {
                reverb?.preset = preset
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandValue(band: Short, value: Short) {
        try {
            equalizer?.setBandLevel(band, value)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getEqualizerBandsCount(): Short = equalizer?.numberOfBands ?: 5

    /**
     * Quran Enhance Mode:
     * - Enhances speech: Boosts mid frequencies (1kHz - 4kHz) for beautiful Quranic clarity.
     * - Attenuates rumbles (bass level reduce).
     * - Adds majestic cathedral/hall echo for beautiful spiritual mosque depth.
     */
    fun setQuranEnhanceMode(active: Boolean) {
        isQuranEnhanceActive = active
        val eq = equalizer ?: return
        try {
            if (active) {
                // Boost vocal clarity bands, reduce bass rumble
                val bands = eq.numberOfBands
                if (bands >= 5) {
                    eq.setBandLevel(0, -300) // Lower lower rumbles
                    eq.setBandLevel(1, -100)
                    eq.setBandLevel(2, 600)  // Boost center vocals (mid)
                    eq.setBandLevel(3, 800)  // Boost high vocals
                    eq.setBandLevel(4, 300)  // Smooth air
                }
                // Set deep spacious reverb (Large Hall = 5 or Medium Hall = 4 or Plate = 6)
                setReverbPreset(5) // High fidelity spiritual echo
                // Low bass boost to prevent speech distortion
                setBassBoostStrength(0)
            } else {
                // Reset to flat
                val bands = eq.numberOfBands
                for (b in 0 until bands) {
                    eq.setBandLevel(b.toShort(), 0)
                }
                setReverbPreset(0) // No Reverb
                setBassBoostStrength(0)
            }
        } catch (e: Throwable) {
            Log.e("AudioEffectManager", "Fail setting Quran Enhance Mode", e)
        }
    }
}

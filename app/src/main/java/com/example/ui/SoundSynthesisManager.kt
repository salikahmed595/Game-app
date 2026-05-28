package com.example.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundSynthesisManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled = true

    fun playLaser() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 150
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    // Glide pitch downwards from 1500Hz to 400Hz
                    val currentFreq = 1500.0 - (1100.0 * progress)
                    val t = i.toDouble() / sampleRate
                    val angle = 2.0 * Math.PI * currentFreq * t
                    val sample = sin(angle) * Short.MAX_VALUE * (1.0 - progress) // fade out
                    samples[i] = sample.toInt().toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playExplosion() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 11025
                val durationMs = 350
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                // Low-pass filtered noise with decreasing amplitude
                var prevSample = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val rawNoise = (Math.random() * 2.0 - 1.0) * Short.MAX_VALUE
                    // Simple low-pass filter to make it bassy
                    val currentSample = (0.2 * rawNoise) + (0.8 * prevSample)
                    prevSample = currentSample
                    
                    val volumeEnvelope = (1.0 - progress) * (1.0 - progress)
                    samples[i] = (currentSample * volumeEnvelope).toInt().toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playChime() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 300
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    // High-pitched bright double chime (e.g., 880Hz and 1320Hz overlay)
                    val angle1 = 2.0 * Math.PI * 880.0 * t
                    val angle2 = 2.0 * Math.PI * 1320.0 * t
                    val sample = (sin(angle1) + 0.5 * sin(angle2)) / 1.5 * Short.MAX_VALUE * (1.0 - progress)
                    samples[i] = sample.toInt().toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playHit() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 60
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val t = i.toDouble() / sampleRate
                    // Metallic pitch bend down from 300Hz to 100Hz
                    val currentFreq = 300.0 - (200.0 * progress)
                    val angle = 2.0 * Math.PI * currentFreq * t
                    val sample = sin(angle) * Short.MAX_VALUE * 0.7 * (1.0 - progress)
                    samples[i] = sample.toInt().toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTick() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 11025
                val durationMs = 20
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    samples[i] = (sin(2.0 * Math.PI * 2500.0 * t) * Short.MAX_VALUE * 0.2).toInt().toShort()
                }
                playBuffer(samples, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playBuffer(samples: ShortArray, sampleRate: Int) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            samples.size * 2,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        // Release track when done
        scope.launch {
            val wait = (samples.size * 1000L) / sampleRate
            kotlinx.coroutines.delay(wait + 200)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (ignored: Exception) {}
        }
    }
}

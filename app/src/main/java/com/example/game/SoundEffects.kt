package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SoundEffects {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val sampleRate = 22050

  fun playPickup() = playToneAsync(freqStart = 420f, freqEnd = 620f, durationMs = 45, volume = 0.22f)

  fun playPlace() = playToneAsync(freqStart = 210f, freqEnd = 135f, durationMs = 60, volume = 0.28f)

  fun playClear(combo: Int) {
    scope.launch {
      val base = 440f + (combo.coerceAtMost(6) - 1) * 65f
      playToneSync(base, base * 1.25f, 110, 0.26f)
      playToneSync(base * 1.5f, base * 1.8f, 130, 0.22f)
    }
  }

  fun playStageClear() {
    scope.launch {
      val notes = listOf(523.25f, 659.25f, 783.99f, 1046.5f)
      for (n in notes) {
        playToneSync(n, n * 1.02f, 95, 0.24f)
      }
    }
  }

  fun playGameOver() {
    scope.launch {
      val notes = listOf(392f, 349.23f, 293.66f, 220f)
      for (n in notes) {
        playToneSync(n, n * 0.96f, 120, 0.22f)
      }
    }
  }

  private fun playToneAsync(freqStart: Float, freqEnd: Float, durationMs: Int, volume: Float) {
    scope.launch { playToneSync(freqStart, freqEnd, durationMs, volume) }
  }

  private fun playToneSync(freqStart: Float, freqEnd: Float, durationMs: Int, volume: Float) {
    val numSamples = (sampleRate * durationMs) / 1000
    if (numSamples <= 0) return
    val pcm = ShortArray(numSamples)
    var phase = 0.0
    for (i in 0 until numSamples) {
      val t = i.toFloat() / numSamples
      val freq = freqStart + (freqEnd - freqStart) * t
      val env = exp(-4.5f * t) * (1f - t * 0.2f)
      phase += 2.0 * PI * freq / sampleRate
      val sample = sin(phase) * env * volume * Short.MAX_VALUE
      pcm[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

    runCatching {
      val bufferSize = maxOf(
        numSamples * 2,
        AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
        ),
      )
      val track =
        AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufferSize)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()
      track.write(pcm, 0, pcm.size)
      track.play()
      Thread.sleep(durationMs.toLong() + 15L)
      track.stop()
      track.release()
    }
  }
}

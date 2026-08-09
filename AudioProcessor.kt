package com.example.potenciadoraudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder

class AudioProcessor {
    private var isProcessing = false
    private var gainFactor = 2.0f // Factor de amplificación inicial

    fun startAudioEngine() {
        isProcessing = true
        
        // Hilo secundario para no congelar la pantalla del celular
        Thread {
            val sampleRate = 44100
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            // Inicializar Micrófono
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            // Inicializar Salida de Auriculares
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            val buffer = ShortArray(bufferSize)
            recorder.startRecording()
            track.play()

            while (isProcessing) {
                val readSize = recorder.read(buffer, 0, buffer.size)
                
                // 1. Aquí se aplica la Ganancia (Potenciar)
                for (i in 0 until readSize) {
                    val amplified = (buffer[i] * gainFactor).toInt()
                    // Limitar para evitar distorsión severa (Clipping)
                    buffer[i] = amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                // 2. TODO: Pasar buffer por el modelo RNNoise / IA de cancelación de ruido

                track.write(buffer, 0, readSize)
            }

            recorder.stop()
            recorder.release()
            track.stop()
            track.release()
        }.start()
    }

    fun stopAudioEngine() {
        isProcessing = false
    }

    fun setGain(newGain: Float) {
        gainFactor = newGain
    }
}

package com.example.potenciadoraudio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val audioProcessor = AudioProcessor()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Permiso denegado por el usuario
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        setContent {
            var isEngineActive by remember { mutableStateOf(false) }
            var gainValue by remember { mutableStateOf(2.0f) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Potenciador de Voz e IA", style = MaterialTheme.typography.headlineMedium)
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // BOTÓN PRINCIPAL
                    Button(
                        onClick = {
                            isEngineActive = !isEngineActive
                            if (isEngineActive) {
                                audioProcessor.startAudioEngine()
                            } else {
                                audioProcessor.stopAudioEngine()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEngineActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(180.dp, 60.dp)
                    ) {
                        Text(if (isEngineActive) "DETENER" else "POTENCIAR")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // CONTROL DE GANANCIA
                    Text(text = "Nivel de Amplificación: ${String.format("%.1f", gainValue)}x")
                    Slider(
                        value = gainValue,
                        onValueChange = { 
                            gainValue = it
                            audioProcessor.setGain(it)
                        },
                        valueRange = 1.0f..5.0f,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

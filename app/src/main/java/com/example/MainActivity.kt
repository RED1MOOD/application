package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuranAudioViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: QuranAudioViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "Storage access granted! Scanners functional.", Toast.LENGTH_SHORT).show()
            viewModel.startStorageScan()
        } else {
            Toast.makeText(this, "Simulated playback workspace loaded. Permissions are optional.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[QuranAudioViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.startStorageScan()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }
}

package com.guardianai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.guardianai.service.FallDetectionService
import com.guardianai.ui.screens.HomeScreen
import com.guardianai.ui.screens.PermissionsScreen
import com.guardianai.ui.screens.SplashScreen
import com.guardianai.ui.screens.ContactsScreen
import com.guardianai.ui.theme.GuardianAITheme

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                startFallDetectionService()
            } else {
                Toast.makeText(this, "Permissions are required for the app to function.", Toast.LENGTH_LONG).show()
            }
        }

    // --- UPDATED PERMISSIONS LIST ---
    // CALL_PHONE permission is removed from this list
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SEND_SMS
        )
    } else {
        arrayOf(
            Manifest.permission.SEND_SMS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
        setContent {
            var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }

            GuardianAITheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val firstLaunch = prefs.getBoolean("first_launch_done", false)
                    var currentScreen by remember { mutableStateOf(if (!firstLaunch) Screen.Permissions else Screen.Home) }

                    when (currentScreen) {
                        Screen.Splash -> SplashScreen(onContinue = { currentScreen = Screen.Permissions })
                        Screen.Permissions -> PermissionsScreen(
                            onPermissionsGranted = {
                                requestPermissions()
                                prefs.edit().putBoolean("first_launch_done", true).apply()
                                currentScreen = Screen.Home
                            },
                            onBack = { currentScreen = Screen.Home }
                        )
                        Screen.Home -> HomeScreen(
                            onOpenPermissions = { currentScreen = Screen.Permissions },
                            onOpenContacts = { currentScreen = Screen.Contacts },
                            darkMode = darkMode,
                            onToggleDarkMode = { enabled ->
                                darkMode = enabled
                                prefs.edit().putBoolean("dark_mode", enabled).apply()
                            }
                        )
                        Screen.Contacts -> ContactsScreen(
                            onDone = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val allPermissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            startFallDetectionService()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun startFallDetectionService() {
        try {
            val serviceIntent = Intent(this, FallDetectionService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (t: Throwable) {
            Toast.makeText(this, "Could not start background service: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

enum class Screen { Splash, Permissions, Home, Contacts }
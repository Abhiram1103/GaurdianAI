package com.guardianai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import com.guardianai.ui.screens.HomeScreen
import com.guardianai.ui.screens.PermissionsScreen
import com.guardianai.ui.screens.SplashScreen
import com.guardianai.ui.screens.ContactsScreen
import com.guardianai.ui.theme.GuardianAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)
        setContent {
            var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }

            GuardianAITheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val firstLaunch = prefs.getBoolean("first_launch_done", false)

                    // simple navigation state
                    var currentScreen by remember { mutableStateOf(if (!firstLaunch) Screen.Permissions else Screen.Home) }

                    when (currentScreen) {
                        Screen.Splash -> SplashScreen(onContinue = { currentScreen = Screen.Permissions })
                        Screen.Permissions -> PermissionsScreen(
                            onPermissionsGranted = {
                                // ensure required runtime permissions are present before starting service
                                val postNotificationsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                } else true

                                if (!postNotificationsOk) {
                                    android.widget.Toast.makeText(this, "Please enable notifications to allow background monitoring.", android.widget.Toast.LENGTH_LONG).show()
                                    // stay on the Permissions screen so the user can enable it
                                    currentScreen = Screen.Permissions
                                } else {
                                    prefs.edit().putBoolean("first_launch_done", true).apply()
                                    // start background (foreground) service scaffold
                                    try {
                                        // use ContextCompat to start foreground service for compatibility
                                        androidx.core.content.ContextCompat.startForegroundService(
                                            this,
                                            Intent(this, com.guardianai.service.FallDetectionService::class.java)
                                        )
                                    } catch (t: Throwable) {
                                        // prevent the app from crashing if starting the service fails
                                        android.widget.Toast.makeText(this, "Could not start background service: ${t.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    currentScreen = Screen.Home
                                }
                            },
                            onBack = {
                                // allow the user to back out to Home without finishing permissions
                                currentScreen = Screen.Home
                            }
                        )
                        Screen.Home -> HomeScreen(
                            onOpenPermissions = {
                                android.util.Log.d("GuardianAI", "Navigation: Home -> Permissions")
                                currentScreen = Screen.Permissions
                            },
                            onOpenContacts = {
                                android.util.Log.d("GuardianAI", "Navigation: Home -> Contacts")
                                currentScreen = Screen.Contacts
                            },
                            darkMode = darkMode,
                            onToggleDarkMode = { enabled ->
                                darkMode = enabled
                                prefs.edit().putBoolean("dark_mode", enabled).apply()
                            }
                        )
                        Screen.Contacts -> ContactsScreen(
                            onDone = { 
                                android.util.Log.d("GuardianAI", "Navigation: Contacts -> Home")
                                currentScreen = Screen.Home 
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class Screen { Splash, Permissions, Home, Contacts }

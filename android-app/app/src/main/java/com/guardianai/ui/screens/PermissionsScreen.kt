package com.guardianai.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("guardian_prefs", Context.MODE_PRIVATE)

    // initialize switches from current permission state so we reflect actual granted permissions
    var notificationsGranted by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    ) }
    var smsGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) }
    var callGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) }
    // dark mode toggle moved to HomeScreen

    val requestNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> notificationsGranted = granted }
    )

    val requestSmsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> smsGranted = granted }
    )

    val requestCallPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> callGranted = granted }
    )

    // handle system back to provide a path back to Home
    BackHandler {
        onBack()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        Text("Permissions", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("To run background fall detection, the app needs the following permissions. If you disable any permission required by the model, detection may not work correctly.")
        Spacer(modifier = Modifier.height(12.dp))

        // Notification toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Notifications")
                Text("Allow the app to show alerts when a fall is detected", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = notificationsGranted, onCheckedChange = { checked ->
                if (checked) {
                    // request permission if Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsGranted = true
                    }
                } else {
                    // open app settings to disable
                    openAppSettings(context as Activity)
                }
            })
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SMS toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Send SMS (optional)")
                Text("Send SMS to emergency contact when not cancelled", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = smsGranted, onCheckedChange = { checked ->
                if (checked) requestSmsPermission.launch(Manifest.permission.SEND_SMS) else openAppSettings(context as Activity)
            })
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Call toggle
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Make Call (optional)")
                Text("Place a call to emergency contact (requires permission)", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = callGranted, onCheckedChange = { checked ->
                if (checked) requestCallPermission.launch(Manifest.permission.CALL_PHONE) else openAppSettings(context as Activity)
            })
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // Persist minimal permission state and continue
            prefs.edit().putBoolean("notifications_granted", notificationsGranted).apply()
            prefs.edit().putBoolean("sms_granted", smsGranted).apply()
            prefs.edit().putBoolean("call_granted", callGranted).apply()

            onPermissionsGranted()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Enable and Continue")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Allow users to go back to Home without completing permissions (explicit path)
        OutlinedButton(onClick = { onBack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Important: Accelerometer access is provided by device sensors and does not require runtime permission. Do not disable sensors or background processing if you want fall detection to work.")
    }
}

private fun openAppSettings(activity: Activity) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.parse("package:" + activity.packageName)
    intent.data = uri
    activity.startActivity(intent)
}

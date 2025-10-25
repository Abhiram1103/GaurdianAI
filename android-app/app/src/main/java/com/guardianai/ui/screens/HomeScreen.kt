package com.guardianai.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guardianai.service.FallEvent
import com.guardianai.ui.screens.ContactsScreen

@Composable
fun HomeScreen(
    onOpenPermissions: () -> Unit,
    onOpenContacts: () -> Unit,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var events by remember { mutableStateOf(listOf<FallEvent>()) }

    // Load stored events - placeholder: in production use Room or DataStore

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Top row: app title and dark mode toggle on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Guardian AI", style = MaterialTheme.typography.headlineLarge)
                // Dark mode toggle in top-right
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (darkMode) "Dark" else "Light", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = darkMode, onCheckedChange = { onToggleDarkMode(it) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Status: Monitoring in background")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Fall History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (events.isEmpty()) {
                Text("No events yet")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(events) { ev ->
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("Time: ${ev.timestamp}")
                            Text("Confidence: ${ev.probability}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stack the important action buttons vertically to avoid overlap on narrow screens
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onOpenContacts() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Emergency Contacts")
                }

                // Permissions button placed below contacts to avoid horizontal crowding
                OutlinedButton(onClick = { onOpenPermissions() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Permissions")
                }
            }
        }
    }
}

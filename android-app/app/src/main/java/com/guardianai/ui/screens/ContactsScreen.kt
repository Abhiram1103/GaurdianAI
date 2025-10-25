package com.guardianai.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardianai.data.EmergencyContact
import com.guardianai.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onDone: () -> Unit) {
    val vm: AppViewModel = viewModel()
    val contacts by vm.contacts.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Handle system back button
    BackHandler {
        onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
        Text("Add New Contact", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (name.isNotBlank() && phone.isNotBlank()) {
                vm.addContact(name.trim(), phone.trim())
                name = ""
                phone = ""
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved Contacts", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (contacts.isEmpty()) {
            Text("No contacts yet")
        } else {
            LazyColumn {
                items(contacts) { contact: EmergencyContact ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(contact.name)
                                Text(contact.phone, style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = { vm.removeContact(contact) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

package com.example.billease.ui.persons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsListScreen(
    onNavigateToPersonDetail: (Long) -> Unit,
    onNavigateToPersonForm: (Long?) -> Unit,
    viewModel: PersonsViewModel = hiltViewModel()
) {
    val persons by viewModel.persons.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Person?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Persons") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToPersonForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        },
        snackbarHost = {
            snackbarMessage?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                ) { Text(it) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or phone") },
                singleLine = true
            )

            if (persons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No persons found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(persons) { person ->
                        PersonListItem(
                            person = person,
                            onClick = { onNavigateToPersonDetail(person.id) },
                            onDeleteClick = { showDeleteDialog = person }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { person ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Person") },
            text = { Text("Are you sure you want to delete ${person.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePerson(person) { success, message ->
                            snackbarMessage = message
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PersonListItem(
    person: Person,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = person.name, style = MaterialTheme.typography.titleMedium)
                Text(text = person.phone, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Person")
            }
        }
    }
}

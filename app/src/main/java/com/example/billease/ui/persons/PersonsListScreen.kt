package com.example.billease.ui.persons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.Person
import com.example.billease.ui.components.ConfirmDeleteDialog
import com.example.billease.ui.components.DismissableSnackbar
import com.example.billease.ui.components.EmptyState
import com.example.billease.ui.components.ListItemCard
import com.example.billease.ui.components.ScreenHeader

@Composable
fun PersonsListScreen(
    onNavigateToPersonDetail: (Long) -> Unit,
    onNavigateToPersonForm: (Long?) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PersonsViewModel = hiltViewModel(),
) {
    val persons by viewModel.persons.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Person?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToPersonForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        },
        snackbarHost = {
            snackbarMessage?.let {
                DismissableSnackbar(message = it, onDismiss = { snackbarMessage = null })
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            ScreenHeader(
                heading = "Persons",
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onNavigateToSettings = onNavigateToSettings,
            )

            if (persons.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Person,
                    title = "No persons found",
                    subtitle = "Tap + to add a new person.",
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(persons) { person ->
                        PersonListItem(
                            person = person,
                            onClick = { onNavigateToPersonDetail(person.id) },
                            onDeleteClick = { showDeleteDialog = person },
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { person ->
        ConfirmDeleteDialog(
            title = "Delete Person",
            message = "Are you sure you want to delete ${person.name}?",
            onConfirm = {
                viewModel.deletePerson(person) { message ->
                    snackbarMessage = message
                    showDeleteDialog = null
                }
            },
            onDismiss = { showDeleteDialog = null },
        )
    }
}

@Composable
fun PersonListItem(
    person: Person,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    ListItemCard(
        onClick = onClick,
        onDeleteClick = onDeleteClick,
        deleteContentDescription = "Delete Person",
    ) {
        Text(text = person.name, style = MaterialTheme.typography.titleMedium)
        Text(text = person.phone, style = MaterialTheme.typography.bodyMedium)
    }
}

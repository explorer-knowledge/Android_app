package com.example.billease.ui.persons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.R
import com.example.billease.ui.components.DetailTopAppBar
import com.example.billease.ui.components.ProfileIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PersonFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title =
                    if (uiState.name.isBlank()) {
                        stringResource(R.string.add_person)
                    } else {
                        stringResource(R.string.edit_person)
                    },
                onNavigateBack = onNavigateBack,
            ) {
                ProfileIconButton(onClick = onNavigateToSettings)
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::updatePhone,
                label = { Text(stringResource(R.string.phone)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError?.let { { Text(it) } },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = { Text(stringResource(R.string.email_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::updateAddress,
                label = { Text(stringResource(R.string.address_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.gstNumber,
                onValueChange = viewModel::updateGstNumber,
                label = { Text(stringResource(R.string.gst_number_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            uiState.saveError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.savePerson(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

package com.example.billease.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun ProductFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title =
                    if (uiState.name.isBlank()) {
                        stringResource(R.string.add_product)
                    } else {
                        stringResource(R.string.edit_product)
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
                label = { Text(stringResource(R.string.product_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.unitPrice,
                    onValueChange = viewModel::updateUnitPrice,
                    label = { Text(stringResource(R.string.unit_price)) },
                    modifier = Modifier.weight(1f),
                    isError = uiState.unitPriceError != null,
                    supportingText = uiState.unitPriceError?.let { { Text(it) } },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = viewModel::updateUnit,
                    label = { Text(stringResource(R.string.unit_hint)) },
                    modifier = Modifier.weight(1f),
                    isError = uiState.unitError != null,
                    supportingText = uiState.unitError?.let { { Text(it) } },
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = uiState.taxPercent,
                onValueChange = viewModel::updateTaxPercent,
                label = { Text(stringResource(R.string.tax_percentage)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.taxPercentError != null,
                supportingText = uiState.taxPercentError?.let { { Text(it) } },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.description_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            uiState.saveError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.saveProduct(onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

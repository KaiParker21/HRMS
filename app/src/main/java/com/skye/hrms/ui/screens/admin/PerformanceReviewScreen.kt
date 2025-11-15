package com.skye.hrms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.admin.EmployeeReview
import com.skye.hrms.data.viewmodels.admin.PerformanceReviewViewModel
import com.skye.hrms.ui.components.ColorfulRadarChart
import com.skye.hrms.utilities.PerformanceMetrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceReviewScreen(
    onBackClicked: () -> Unit,
    viewModel: PerformanceReviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // State to manage the dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<EmployeeReview?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Reviews") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.employees, key = { it.userId }) { employee ->
                            EmployeeReviewCard(
                                employee = employee,
                                onClick = {
                                    selectedEmployee = employee
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show the Edit Dialog
    if (showDialog && selectedEmployee != null) {
        ReviewEditDialog(
            employee = selectedEmployee!!,
            onDismiss = { showDialog = false },
            onSave = { userId, newScores ->
                viewModel.updateEmployeeReview(userId, newScores)
                showDialog = false
            }
        )
    }
}

@Composable
fun EmployeeReviewCard(employee: EmployeeReview, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = employee.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = employee.designation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

             ColorfulRadarChart(
                 labels = PerformanceMetrics.labels,
                 values = employee.reviewScores.values.toList()
             )

        }
    }
}

@Composable
fun ReviewEditDialog(
    employee: EmployeeReview,
    onDismiss: () -> Unit,
    onSave: (String, Map<String, Float>) -> Unit
) {
    val labels = PerformanceMetrics.labels
    var currentScores by remember { mutableStateOf(employee.reviewScores) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review ${employee.fullName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                labels.forEach { label ->
                    val currentValue = currentScores[label] ?: 50f
                    Text(
                        "${label}: ${currentValue.toInt()}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = currentValue,
                        onValueChange = { newValue ->
                            currentScores = currentScores.toMutableMap().apply {
                                this[label] = newValue
                            }
                        },
                        valueRange = 0f..100f
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(employee.userId, currentScores) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
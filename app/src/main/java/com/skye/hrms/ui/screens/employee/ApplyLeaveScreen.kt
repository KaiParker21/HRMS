package com.skye.hrms.ui.screens.employee

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.employee.LeaveHistoryItem
import com.skye.hrms.data.viewmodels.employee.LeaveSubmissionState
import com.skye.hrms.data.viewmodels.employee.LeaveViewModel
import com.skye.hrms.ui.components.SingleChoiceButtonGroup
import com.skye.hrms.ui.components.leaves
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplyLeaveScreen(
    onBackClicked: () -> Unit,
    leaveViewModel: LeaveViewModel = viewModel()
) {
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var leaveType by remember { mutableStateOf(leaves[0].name) }
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current
    val leaveSubmissionState by leaveViewModel.submissionState.collectAsState()

    // --- NEW ---
    val historyState by leaveViewModel.historyState.collectAsState()

    var selectedLeaveIndex by remember { mutableIntStateOf(0) }
    val openStartDateDialog = remember { mutableStateOf(false) }
    val openEndDateDialog = remember { mutableStateOf(false) }

    LaunchedEffect(leaveSubmissionState) {
        when (val state = leaveSubmissionState) {
            is LeaveSubmissionState.Success -> {
                Toast.makeText(context, "Leave request submitted successfully!", Toast.LENGTH_LONG).show()
                leaveViewModel.resetSubmissionState()
                // Clear the form
                startDate = null
                endDate = null
                reason = ""
                // We don't need onBackClicked() if we show the history list
            }
            is LeaveSubmissionState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                leaveViewModel.resetSubmissionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for Leave", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- CHANGED to LazyColumn ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // --- ITEM 1: The Form ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Fill in your leave details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DateInputField(
                            label = "Start Date",
                            selectedDate = startDate,
                            onClick = { openStartDateDialog.value = true },
                            modifier = Modifier.weight(1f)
                        )
                        DateInputField(
                            label = "End Date",
                            selectedDate = endDate,
                            onClick = { openEndDateDialog.value = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SingleChoiceButtonGroup(
                        selectedIndex = selectedLeaveIndex,
                        onSelectionChanged = { newIndex ->
                            selectedLeaveIndex = newIndex
                            leaveType = leaves[newIndex].name
                        }
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Please provide a reason for your leave...") },
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = {
                            if (startDate != null && endDate != null && leaveType.isNotBlank() && reason.isNotBlank()) {
                                leaveViewModel.submitLeaveRequest(startDate!!, endDate!!, leaveType, reason)
                            } else {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        enabled = leaveSubmissionState !is LeaveSubmissionState.Loading,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (leaveSubmissionState is LeaveSubmissionState.Loading) {
                            LoadingIndicator()
                        } else {
                            Text("Submit Request", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // --- ITEM 2: History Section Title ---
            item {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
                    Text(
                        text = "Your Leave History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // --- ITEM 3: History List ---
            when {
                historyState.isLoading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator()
                        }
                    }
                }
                historyState.errorMessage != null -> {
                    Log.e("LeaveHistoryScreen", "Error loading history: ${historyState.errorMessage}")
                    item {
                        Text(
                            text = historyState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                historyState.history.isEmpty() -> {
                    item {
                        Text(
                            text = "No past leave requests found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    items(historyState.history, key = { it.id }) { request ->
                        LeaveHistoryItemCard(request = request)
                    }
                }
            }
        }
    }

    if (openStartDateDialog.value) {
        CustomDatePickerDialog(
            onDateSelected = { startDate = it },
            onDismiss = { openStartDateDialog.value = false }
        )
    }
    if (openEndDateDialog.value) {
        CustomDatePickerDialog(
            onDateSelected = { endDate = it },
            onDismiss = { openEndDateDialog.value = false }
        )
    }
}

// --- NEW COMPOSABLE ---
@Composable
fun LeaveHistoryItemCard(request: LeaveHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.leaveType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(request.status)
                )
            }
            Text(
                text = request.dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (request.reason.isNotBlank()) {
                Text(
                    text = request.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// --- NEW HELPER ---
@Composable
private fun getStatusColor(status: String): Color {
    return when (status) {
        "Approved" -> MaterialTheme.colorScheme.primary
        "Rejected" -> MaterialTheme.colorScheme.error
        "Pending" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}


@Composable
fun DateInputField(label: String, selectedDate: Date?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // ... (This composable remains the same)
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    Box(modifier = modifier.clickable { onClick() }) {
        OutlinedTextField(
            value = selectedDate?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(onDateSelected: (Date) -> Unit, onDismiss: () -> Unit) {
    // ... (This composable remains the same)
    val datePickerState = rememberDatePickerState()
    val selectedDateInMillis = datePickerState.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                selectedDateInMillis?.let {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/KKolkata"))
                    calendar.timeInMillis = it
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onDateSelected(calendar.time)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
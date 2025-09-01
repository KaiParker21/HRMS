package com.skye.hrms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.skye.hrms.data.viewmodels.LeaveSubmissionState // <-- Import the new state class
import com.skye.hrms.data.viewmodels.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveScreen(
    navController: NavController,
    leaveViewModel: LeaveViewModel = viewModel()
) {
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var leaveType by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val leaveTypes = listOf("Casual", "Sick", "Earned", "Unpaid")
    val context = LocalContext.current

    val leaveSubmissionState by leaveViewModel.submissionState.collectAsState()

    val openStartDateDialog = remember { mutableStateOf(false) }
    val openEndDateDialog = remember { mutableStateOf(false) }

    LaunchedEffect(leaveSubmissionState) {
        // Use the new LeaveSubmissionState type here
        when (val state = leaveSubmissionState) {
            is LeaveSubmissionState.Success -> {
                Toast.makeText(context, "Leave request submitted successfully!", Toast.LENGTH_LONG).show()
                leaveViewModel.resetSubmissionState()
                navController.popBackStack()
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
                title = { Text("Apply for Leave") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            // Leave Type Dropdown
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = leaveType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Leave Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    leaveTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                leaveType = type
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Reason TextField
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Please provide a reason for your leave...") }
            )

            // Submit Button
            Button(
                onClick = {
                    if (startDate != null && endDate != null && leaveType.isNotBlank() && reason.isNotBlank()) {
                        leaveViewModel.submitLeaveRequest(startDate!!, endDate!!, leaveType, reason)
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                // Use the new LeaveSubmissionState type here
                enabled = leaveSubmissionState !is LeaveSubmissionState.Loading
            ) {
                // Use the new LeaveSubmissionState type here
                if (leaveSubmissionState is LeaveSubmissionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request", fontWeight = FontWeight.Bold)
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

// Helper composables (DateInputField, CustomDatePickerDialog) remain the same as the previous version.
// ... (You can copy them from the previous response if needed)
@Composable
fun DateInputField(label: String, selectedDate: Date?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    Box(modifier = modifier.clickable { onClick() }) {
        OutlinedTextField(
            value = selectedDate?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(onDateSelected: (Date) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    val selectedDateInMillis = datePickerState.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                selectedDateInMillis?.let {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = it
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
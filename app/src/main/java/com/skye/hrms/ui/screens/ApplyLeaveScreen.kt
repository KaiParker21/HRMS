package com.skye.hrms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.LeaveSubmissionState
import com.skye.hrms.data.viewmodels.LeaveViewModel
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
    var selectedLeaveIndex by remember { mutableIntStateOf(0) }
    val openStartDateDialog = remember { mutableStateOf(false) }
    val openEndDateDialog = remember { mutableStateOf(false) }

    LaunchedEffect(leaveSubmissionState) {
        when (val state = leaveSubmissionState) {
            is LeaveSubmissionState.Success -> {
                Toast.makeText(context, "Leave request submitted successfully!", Toast.LENGTH_LONG).show()
                leaveViewModel.resetSubmissionState()
                onBackClicked()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

//            ExposedDropdownMenuBox(
//                expanded = isDropdownExpanded,
//                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
//            ) {
//                OutlinedTextField(
//                    value = leaveType,
//                    onValueChange = {},
//                    readOnly = true,
//                    label = { Text("Leave Type") },
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .menuAnchor(),
//                    shape = RoundedCornerShape(16.dp)
//                )
//                ExposedDropdownMenu(
//                    expanded = isDropdownExpanded,
//                    onDismissRequest = { isDropdownExpanded = false }
//                ) {
//                    leaveTypes.forEach { type ->
//                        DropdownMenuItem(
//                            text = { Text(type) },
//                            onClick = {
//                                leaveType = type
//                                isDropdownExpanded = false
//                            }
//                        )
//                    }
//                }
//            }

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
//                    CircularWavyProgressIndicator()
                } else {
                    Text("Submit Request", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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

@Composable
fun DateInputField(label: String, selectedDate: Date?, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
    val datePickerState = rememberDatePickerState()
    val selectedDateInMillis = datePickerState.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                selectedDateInMillis?.let {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
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
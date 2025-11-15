package com.skye.hrms.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.common.SubmissionState
import com.skye.hrms.data.viewmodels.admin.HolidayItem
import com.skye.hrms.data.viewmodels.admin.HolidayViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HolidayScreen(
    onBackClicked: () -> Unit,
    viewModel: HolidayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val context = LocalContext.current

    // State for the "Add Holiday" form
    var holidayName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var openDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(submissionState) {
        when (val state = submissionState) {
            is SubmissionState.Success -> {
                Toast.makeText(context, "Holiday added!", Toast.LENGTH_SHORT).show()
                viewModel.resetSubmissionState()
                // Clear the form
                holidayName = ""
                selectedDate = null
            }
            is SubmissionState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSubmissionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Holidays") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
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
        ) {
            // 1. "Add Holiday" Form
            AddHolidayForm(
                holidayName = holidayName,
                selectedDate = selectedDate,
                onNameChange = { holidayName = it },
                onDateClick = { openDateDialog = true },
                isSubmitting = submissionState is SubmissionState.Loading,
                onSubmit = {
                    if (holidayName.isNotBlank() && selectedDate != null) {
                        viewModel.addHoliday(holidayName, selectedDate!!)
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 2. "Upcoming Holidays" List
            Text(
                text = "Upcoming Holidays",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.errorMessage != null -> {
                        Text(
                            text = uiState.errorMessage ?: "An error occurred.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    uiState.holidays.isEmpty() -> {
                        Text(
                            text = "No holidays added yet.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.holidays) { holiday ->
                                HolidayListItem(
                                    holiday = holiday,
                                    onDelete = { viewModel.deleteHoliday(holiday.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (openDateDialog) {
        CustomDatePickerDialog(
            onDateSelected = { selectedDate = it },
            onDismiss = { openDateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddHolidayForm(
    holidayName: String,
    selectedDate: Date?,
    onNameChange: (String) -> Unit,
    onDateClick: () -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = holidayName,
            onValueChange = onNameChange,
            label = { Text("Holiday Name (e.g., Diwali)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        DateInputField(
            label = "Date",
            selectedDate = selectedDate,
            onClick = onDateClick,
            modifier = Modifier.fillMaxWidth()
        )
        FilledTonalButton(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSubmitting) {
                LoadingIndicator()
            } else {
                Text("Add Holiday", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HolidayListItem(holiday: HolidayItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = holiday.date, // "01 Nov"
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = holiday.dayOfWeek, // "Saturday"
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Holiday Name
            Text(
                text = holiday.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete Holiday",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- Helper Composables (Copied from ApplyLeaveScreen) ---
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
package com.skye.hrms.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.common.SubmissionState
import com.skye.hrms.data.viewmodels.admin.EmployeeListItem
import com.skye.hrms.data.viewmodels.admin.PayslipUploadViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PayslipUploadScreen(
    onBackClicked: () -> Unit,
    viewModel: PayslipUploadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val context = LocalContext.current

    // Form State
    var selectedEmployee by remember { mutableStateOf<EmployeeListItem?>(null) }
    var selectedMonth by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("No file selected") }

    var isEmployeeDropdownExpanded by remember { mutableStateOf(false) }
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.let { context.getFileName(it) } ?: "No file selected"
    }

    LaunchedEffect(submissionState) {
        when (val state = submissionState) {
            is SubmissionState.Success -> {
                Toast.makeText(context, "Payslip uploaded successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetSubmissionState()
                // Clear the form
                selectedEmployee = null
                selectedMonth = ""
                selectedYear = ""
                selectedFileUri = null
                selectedFileName = "No file selected"
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
                title = { Text("Upload Payslip") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoadingEmployees) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
                Text("Loading employees...", modifier = Modifier.padding(top = 80.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Employee Dropdown
                ExposedDropdownMenuBox(
                    expanded = isEmployeeDropdownExpanded,
                    onExpandedChange = { isEmployeeDropdownExpanded = !isEmployeeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEmployee?.fullName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Employee") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEmployeeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isEmployeeDropdownExpanded,
                        onDismissRequest = { isEmployeeDropdownExpanded = false }
                    ) {
                        uiState.employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.fullName) },
                                onClick = {
                                    selectedEmployee = employee
                                    isEmployeeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Month/Year
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Month Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isMonthDropdownExpanded,
                        onExpandedChange = { isMonthDropdownExpanded = !isMonthDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMonthDropdownExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isMonthDropdownExpanded,
                            onDismissRequest = { isMonthDropdownExpanded = false }
                        ) {
                            months.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = month
                                        isMonthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Field
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = { selectedYear = it },
                        label = { Text("Year") },
                        placeholder = { Text(Calendar.getInstance().get(Calendar.YEAR).toString()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                // File Picker
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedFileName, maxLines = 1)
                }

                // Submit Button
                // Submit Button
                FilledTonalButton(
                    onClick = {
                        val yearLong = selectedYear.toLongOrNull()
                        if (selectedEmployee == null || selectedMonth.isBlank() || yearLong == null || selectedFileUri == null) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.uploadPayslip(
                                userId = selectedEmployee!!.userId,
                                uri = selectedFileUri!!,
                                context = context,
                                month = selectedMonth,
                                year = yearLong
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = submissionState != SubmissionState.Loading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (submissionState is SubmissionState.Loading) {
                        LoadingIndicator()
                    } else {
                        Text("Upload Payslip", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper function to get the real file name
private fun Context.getFileName(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}
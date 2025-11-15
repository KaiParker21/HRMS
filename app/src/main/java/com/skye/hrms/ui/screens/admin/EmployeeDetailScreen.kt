package com.skye.hrms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.common.EducationItem
import com.skye.hrms.data.viewmodels.common.OnboardingFormData
import com.skye.hrms.data.viewmodels.admin.EmployeeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmployeeDetailScreen(
    userId: String,
    onBackClicked: () -> Unit,
    viewModel: EmployeeDetailViewModel = viewModel()
) {
    // Trigger the data load when the screen appears
    LaunchedEffect(userId) {
        viewModel.loadEmployeeDetails(userId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "An error occurred.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.employee != null -> {
                    EmployeeDataContent(employee = uiState.employee!!)
                }
            }
        }
    }
}

@Composable
fun EmployeeDataContent(employee: OnboardingFormData) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Job Details Card
        item {
            DetailCard(title = "Job Details", icon = Icons.Outlined.Badge) {
                DetailRow(label = "Full Name", value = employee.fullName)
                DetailRow(label = "Designation", value = employee.designation)
                DetailRow(label = "Department", value = employee.department)
                DetailRow(label = "Employee ID", value = employee.employeeId)
                DetailRow(label = "Date of Joining", value = employee.dateOfJoining)
            }
        }

        // Personal Details Card
        item {
            DetailCard(title = "Personal Details", icon = Icons.Outlined.Person) {
                DetailRow(label = "Date of Birth", value = employee.dateOfBirth)
                DetailRow(label = "Gender", value = employee.gender)
                DetailRow(label = "Contact Number", value = employee.contactNumber)
            }
        }

        // Address Card
        item {
            DetailCard(title = "Address", icon = Icons.Outlined.Home) {
                DetailRow(label = "Current Address", value = employee.currentAddress)
                DetailRow(label = "Permanent Address", value = employee.permanentAddress)
            }
        }

        // Emergency Contact
        item {
            DetailCard(title = "Emergency Contact", icon = Icons.Outlined.Emergency) {
                DetailRow(label = "Contact Name", value = employee.emergencyContactName)
                DetailRow(label = "Contact Number", value = employee.emergencyContactNumber)
            }
        }

        // Education History
        if (employee.educationalHistory.isNotEmpty()) {
            item {
                Text(
                    "Education History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(employee.educationalHistory) { education ->
                EducationItemCard(education = education)
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EducationItemCard(education: EducationItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = education.degree,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = education.university,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Specialisation: ${education.specialisation} (${education.year})",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
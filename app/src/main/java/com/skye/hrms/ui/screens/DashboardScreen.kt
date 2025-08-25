package com.skye.hrms.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.data.viewmodels.DashboardState
import com.skye.hrms.data.viewmodels.DashboardViewModel
import com.skye.hrms.ui.themes.HRMSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi, ${uiState.employeeName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.employeeDesignation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle search */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: Handle notifications */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                    Image(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClick = {
                                    authViewModel.signOut()
                                    onSignOut()
                                }
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage ?: "An unknown error occurred.")
            }
        } else {
            DashboardContent(paddingValues)
        }
    }
}

@Composable
private fun DashboardContent(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { QuickActionsSection() }
        item { AnnouncementSection() }
        item { LeaveBalanceSection() }
    }
}

// Section for quick action buttons
@Composable
private fun QuickActionsSection() {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(icon = Icons.Outlined.EditCalendar, label = "Apply Leave")
            QuickActionButton(icon = Icons.Outlined.Payments, label = "Payslip")
            QuickActionButton(icon = Icons.Outlined.CalendarMonth, label = "Attendance")
            QuickActionButton(icon = Icons.Outlined.Work, label = "Regularize")
        }
    }
}

// A single action button used in the QuickActionsSection
@Composable
private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        contentPadding = PaddingValues(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}


// Section for company announcements
@Composable
private fun AnnouncementSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📢 Announcements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "The annual performance review cycle will begin next month. Please ensure all your goals are updated in the portal by the end of this week.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Section displaying leave balances
@Composable
private fun LeaveBalanceSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Leave Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LeaveBalanceItem("🌴 Casual Leave", 8.5f, 12f)
            Spacer(modifier = Modifier.height(12.dp))
            LeaveBalanceItem("🤒 Sick Leave", 3f, 6f)
        }
    }
}

// A single leave item with a progress bar
@Composable
private fun LeaveBalanceItem(type: String, used: Float, total: Float) {
    val progress = used / total
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = type, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$used / $total Days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    HRMSTheme {
        // A preview that doesn't rely on the ViewModel for easy visualization
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Hi, Alex",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Software Engineer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Outlined.Search, "Search") }
                        IconButton(onClick = {}) { Icon(Icons.Outlined.Notifications, "Notifications") }
                        Image(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        ) { padding ->
            DashboardContent(padding)
        }
    }
}
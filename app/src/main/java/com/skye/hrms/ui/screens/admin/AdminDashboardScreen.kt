package com.skye.hrms.ui.screens.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.skye.hrms.data.viewmodels.common.AuthViewModel
import com.skye.hrms.data.viewmodels.admin.AdminDashboardViewModel
import com.skye.hrms.ui.components.ColorfulRadarChart
import com.skye.hrms.ui.helpers.Screens
import com.skye.hrms.utilities.PerformanceMetrics

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: AdminDashboardViewModel = viewModel(),
    onNavigateToLeaveApprovals: () -> Unit,
    onNavigateToEmployeeList: () -> Unit,
    onNavigateToPayslips: () -> Unit,
    onNavigateToHolidays: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToPerformanceReview: () -> Unit

) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        // Check if this screen is the current destination
        if (navBackStackEntry?.destination?.route == Screens.AdminDashboardScreen.route) {
            viewModel.loadAdminData() // 4. Tell the ViewModel to refresh
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HR Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.signOut()
                            onSignOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeroApprovalCard(
                        pendingCount = uiState.pendingApprovals,
                        onClick = onNavigateToLeaveApprovals
                    )
                }
                item {
                    QuickActionsAdmin(
                        onManageEmployees = onNavigateToEmployeeList,
                        onUploadPayslips = onNavigateToPayslips,
                        onManageHolidays = onNavigateToHolidays,
                        onPerformanceReview = onNavigateToPerformanceReview
                    )
                }
                if (uiState.averageReviewScores.isNotEmpty()) {
                    item {
                        CompanyPerformanceCard(
                            reviewScores = uiState.averageReviewScores
                        )
                    }
                }
                item {
                    TeamStatsCard(
                        totalEmployees = uiState.totalEmployees,
                        onLeaveToday = uiState.onLeaveToday
                    )
                }

                item {
                    HolidayCard(
                        holidayName = uiState.nextHolidayName,
                        holidayDate = uiState.nextHolidayDate
                    )
                }
            }
        }
    }
}

@Composable
fun CompanyPerformanceCard(reviewScores: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Company-Wide Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- UNCOMMENT YOUR CHART COMPOSABLE HERE ---
             ColorfulRadarChart(
                 labels = PerformanceMetrics.labels,
                 values = reviewScores.values.toList()
             )
        }
    }
}

// 1. A hero card for the most urgent task
@Composable
fun HeroApprovalCard(pendingCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pending Approvals",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = pendingCount.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "New leave requests to review",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// 2. A card for high-level stats
@Composable
fun TeamStatsCard(totalEmployees: Int, onLeaveToday: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                label = "Total Employees",
                count = totalEmployees.toString(),
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "On Leave Today",
                count = onLeaveToday.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(label: String, count: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// 3. Quick action buttons for other modules
@Composable
fun QuickActionsAdmin(
    onManageEmployees: () -> Unit,
    onUploadPayslips: () -> Unit,
    onManageHolidays: () -> Unit,
    onPerformanceReview: () -> Unit
) {
    Column {
        Text(
            text = "HR Toolkit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AdminActionCard(
                    title = "Manage Employees",
                    icon = Icons.Outlined.Groups,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = onManageEmployees
                )
            }
            item {
                AdminActionCard(
                    title = "Upload Payslips",
                    icon = Icons.Outlined.CloudUpload,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onUploadPayslips
                )
            }
            item {
                AdminActionCard(
                    title = "Set Holidays",
                    icon = Icons.Outlined.EditCalendar,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = onManageHolidays
                )
            }
            item {
                AdminActionCard(
                    title = "Performance Review",
                    icon = Icons.Outlined.BarChart,
                    color = MaterialTheme.colorScheme.errorContainer,
                    onClick = onPerformanceReview
                )
            }
        }
    }
}

@Composable
fun AdminActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(130.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(32.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// 4. Re-usable Holiday Card
@Composable
fun HolidayCard(holidayName: String, holidayDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Celebration,
                contentDescription = "Holiday",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Next Holiday",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    holidayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                holidayDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
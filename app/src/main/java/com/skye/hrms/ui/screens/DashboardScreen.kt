package com.skye.hrms.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.data.viewmodels.DashboardViewModel
import com.skye.hrms.data.viewmodels.LeaveInfo
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit,
    onApplyLeaveClicked: () -> Unit,
    onAttendanceClicked: () -> Unit,
    onPayslipClicked: () -> Unit,
    onMyDocumentsClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GreetingHeader(
                name = uiState.employeeName,
                onSignOutClicked = {
                    authViewModel.signOut()
                    onSignOut()
                }
            ) }
            item { AttendanceCard(
                isClockedIn = uiState.isClockedIn,
                clockInTime = uiState.clockInTime,
                onClockInToggle = { viewModel.toggleClockIn() }
            ) }
            item { QuickActionsSection(
                onApplyLeaveClicked,
                onAttendanceClicked,
                onPayslipClicked,
                onMyDocumentsClicked
            ) }
            item { TimeOffInfoSection(
                leaveBalances = uiState.leaveBalances,
                holidayName = uiState.nextHoliday,
                holidayDate = uiState.nextHolidayDate
            ) }
            item { AnnouncementsSection(announcements = uiState.announcements) }
        }
    }
}

@Composable
fun GreetingHeader(
    name: String,
    onSignOutClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Text(
                text = "Good morning,",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onSignOutClicked
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AttendanceCard(
    isClockedIn: Boolean,
    clockInTime: LocalTime?,
    onClockInToggle: () -> Unit
) {
    var workDuration by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(key1 = isClockedIn, key2 = clockInTime) {
        while (isClockedIn && clockInTime != null) {
            val duration = Duration.between(clockInTime, LocalTime.now())
            val hours = duration.toHours().toString().padStart(2, '0')
            val minutes = duration.toMinutesPart().toString().padStart(2, '0')
            val seconds = duration.toSecondsPart().toString().padStart(2, '0')
            workDuration = "$hours:$minutes:$seconds"
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isClockedIn) "You are Clocked In" else "You are Clocked Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isClockedIn) {
                    Text(
                        text = "Working for: $workDuration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Last updated: ${LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FilledTonalButton(onClick = onClockInToggle) {
                Icon(
                    imageVector = if (isClockedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                    contentDescription = "Clock In/Out",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isClockedIn) "Clock Out" else "Clock In")
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onApplyLeaveClicked: () -> Unit,
    onAttendanceClicked: () -> Unit,
    onPayslipClicked: () -> Unit,
    onMyDocumentsClicked: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ActionCard("Apply Leave", Icons.AutoMirrored.Outlined.EventNote, MaterialTheme.colorScheme.tertiaryContainer, onCardClicked = onApplyLeaveClicked) }
            item { ActionCard("View Payslip", Icons.Outlined.Payments, MaterialTheme.colorScheme.primaryContainer, onCardClicked = onPayslipClicked) }
            item { ActionCard("Attendance", Icons.Outlined.CoPresent, MaterialTheme.colorScheme.secondaryContainer, onCardClicked = onAttendanceClicked) }
            item { ActionCard("My Documents", Icons.AutoMirrored.Outlined.Article, MaterialTheme.colorScheme.errorContainer, onCardClicked = onMyDocumentsClicked) }
        }
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, backgroundColor: Color, onCardClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable(
                onClick = onCardClicked
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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


@Composable
fun TimeOffInfoSection(leaveBalances: List<LeaveInfo>, holidayName: String, holidayDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Time Off", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                leaveBalances.forEach { leave ->
                    LeaveBalanceIndicator(leave = leave)
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Next Holiday",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Next Holiday", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(holidayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(holidayDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun LeaveBalanceIndicator(leave: LeaveInfo) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { leave.balance / leave.total },
            modifier = Modifier.size(80.dp),
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${leave.balance}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(leave.type, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun AnnouncementsSection(announcements: List<String>) {
    if(announcements.isNotEmpty()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Announcements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = announcements.first(),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
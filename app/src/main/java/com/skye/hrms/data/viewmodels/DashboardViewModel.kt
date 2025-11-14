package com.skye.hrms.data.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

data class LeaveInfo(val type: String, val balance: Float, val total: Float)
data class TeamMember(val name: String, val avatarUrl: String)

data class DashboardUiState(
    val employeeName: String = "",
    val isClockedIn: Boolean = false,
    val clockInTime: LocalTime? = null,
    val leaveBalances: List<LeaveInfo> = emptyList(),
    val nextHoliday: String = "Independence Day",
    val nextHolidayDate: String = "15 August",
    val announcements: List<String> = emptyList(),
    val team: List<TeamMember> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
class DashboardViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private fun getTodayDateId(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun getTodayStartTimestamp(): Timestamp {
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        return Timestamp(Date.from(startOfToday.toInstant()))
    }

    init {
        loadDashboardData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true) }
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            try {
                val employeeDoc = db.collection("employees").document(userId).get().await()
                val employeeName = employeeDoc.getString("fullName") ?: "Employee"
                val isClockedIn = employeeDoc.getBoolean("isClockedIn") ?: false
                val clockInTimestamp = employeeDoc.getTimestamp("lastClockInTime")
                val clockInTime = clockInTimestamp?.toLocalTime()

                val leaveBalancesList = mutableListOf<LeaveInfo>()
                val leavesData = employeeDoc.get("leaveBalances") as? List<HashMap<String, Any>> ?: emptyList()
                leavesData.forEach { leaveMap ->
                    leaveBalancesList.add(
                        LeaveInfo(
                            type = leaveMap["type"] as? String ?: "",
                            balance = (leaveMap["balance"] as? Double)?.toFloat() ?: 0f,
                            total = (leaveMap["total"] as? Double)?.toFloat() ?: 0f
                        )
                    )
                }

                val announcementsList = mutableListOf<String>()
                val announcementSnapshot = db.collection("announcements")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
                if (!announcementSnapshot.isEmpty) {
                    announcementsList.add(
                        announcementSnapshot.documents.first().getString("message") ?: ""
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employeeName = employeeName,
                        isClockedIn = isClockedIn,
                        clockInTime = clockInTime,
                        leaveBalances = leaveBalancesList,
                        announcements = announcementsList
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleClockIn() {
        val userId = auth.currentUser?.uid ?: return
        val currentUiState = _uiState.value
        val newClockInStatus = !currentUiState.isClockedIn
        val todayDocId = getTodayDateId()

        val employeeRef = db.collection("employees").document(userId)
        val attendanceDocRef = employeeRef.collection("attendance").document(todayDocId)

        viewModelScope.launch {
            try {
                if (newClockInStatus) {
                    val now = FieldValue.serverTimestamp()

                    val attendanceData = hashMapOf(
                        "date" to getTodayStartTimestamp(),
                        "clockInTime" to now,
                        "clockOutTime" to null,
                        "status" to "Pending",
                        "totalHours" to 0.0
                    )
                    attendanceDocRef.set(attendanceData, SetOptions.merge()).await()

                    employeeRef.update(
                        "isClockedIn", true,
                        "lastClockInTime", now
                    ).await()

                    _uiState.update {
                        it.copy(isClockedIn = true, clockInTime = LocalTime.now())
                    }
                } else {
                    val clockOutTimestamp = FieldValue.serverTimestamp()

                    val employeeDoc = employeeRef.get().await()
                    val clockInTimestamp = employeeDoc.getTimestamp("lastClockInTime")

                    var status = "Present"
                    var totalHours = 0.0

                    if (clockInTimestamp != null) {
                        val clockInTime = clockInTimestamp.toDate().time
                        val clockOutTime = Date().time
                        val diffMillis = clockOutTime - clockInTime
                        totalHours = (diffMillis.toDouble() / (1000 * 60 * 60))

                        status = when {
                            totalHours >= 8 -> "Present"
                            totalHours >= 4 -> "Half-day"
                            else -> "Absent"
                        }
                    }

                    attendanceDocRef.update(
                        "clockOutTime", clockOutTimestamp,
                        "status", status,
                        "totalHours", totalHours
                    ).await()

                    employeeRef.update("isClockedIn", false).await()

                    _uiState.update { it.copy(isClockedIn = false, clockInTime = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update status: ${e.message}") }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Timestamp.toLocalTime(): LocalTime {
        return Instant.ofEpochSecond(this.seconds)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
    }
}
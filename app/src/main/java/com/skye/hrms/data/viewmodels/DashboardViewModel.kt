package com.skye.hrms.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

data class LeaveInfo(val type: String, val balance: Float, val total: Float)
data class TeamMember(val name: String, val avatarUrl: String)

// The UI State now includes loading and error handling
data class DashboardUiState(
    val employeeName: String = "",
    val isClockedIn: Boolean = false,
    val clockInTime: LocalTime? = null,
    val leaveBalances: List<LeaveInfo> = emptyList(),
    val nextHoliday: String = "Independence Day", // Can be moved to Firestore later
    val nextHolidayDate: String = "15 August", // Can be moved to Firestore later
    val announcements: List<String> = emptyList(),
    val team: List<TeamMember> = emptyList(), // Team data can be fetched in a similar way
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true) }
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            try {
                // --- Fetch Employee-Specific Data ---
                val employeeDoc = db.collection("employees").document(userId).get().await()
                val employeeName = employeeDoc.getString("fullName") ?: "Employee"
                val isClockedIn = employeeDoc.getBoolean("isClockedIn") ?: false
                val clockInTimestamp = employeeDoc.getTimestamp("lastClockInTime")
                val clockInTime = clockInTimestamp?.toLocalTime()

                // Fetch leave balances (stored as an array of maps in Firestore)
                val leaveBalancesList = mutableListOf<LeaveInfo>()
                val leavesData = employeeDoc.get("leaveBalances") as? List<HashMap<String, Any>>
                leavesData?.forEach { leaveMap ->
                    leaveBalancesList.add(
                        LeaveInfo(
                            type = leaveMap["type"] as? String ?: "",
                            balance = (leaveMap["balance"] as? Double)?.toFloat() ?: 0f,
                            total = (leaveMap["total"] as? Double)?.toFloat() ?: 0f
                        )
                    )
                }

                // --- Fetch Global Announcements ---
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

                // --- Update State on Success ---
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

        viewModelScope.launch {
            try {
                // Update the document in Firestore
                val employeeRef = db.collection("employees").document(userId)
                if (newClockInStatus) {
                    // Clocking IN: set status to true and update the timestamp
                    employeeRef.update(
                        "isClockedIn", true,
                        "lastClockInTime", FieldValue.serverTimestamp()
                    ).await()
                    // Update local state immediately for snappy UI
                    _uiState.update {
                        it.copy(isClockedIn = true, clockInTime = LocalTime.now())
                    }
                } else {
                    // Clocking OUT: set status to false
                    employeeRef.update("isClockedIn", false).await()
                    _uiState.update { it.copy(isClockedIn = false, clockInTime = null) }
                }
            } catch (e: Exception) {
                // If the update fails, you could show an error to the user
                _uiState.update { it.copy(errorMessage = "Failed to update status: ${e.message}") }
            }
        }
    }

    // Helper function to convert Firebase Timestamp to LocalTime
    private fun Timestamp.toLocalTime(): LocalTime {
        return Instant.ofEpochSecond(this.seconds)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
    }
}
package com.skye.hrms.data.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// UI state for the admin dashboard
data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val pendingApprovals: Int = 0,
    val totalEmployees: Int = 0,
    val onLeaveToday: Int = 0, // We'll mock this for now
    val nextHolidayName: String = "No upcoming holidays",
    val nextHolidayDate: String = "",
    val errorMessage: String? = null
)

class AdminDashboardViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // We run these queries concurrently for faster loading
                val pendingApprovalsQuery = async {
                    db.collection("leave_requests")
                        .whereEqualTo("status", "Pending")
                        .get()
                        .await()
                }

                val employeeStatsQuery = async {
                    db.collection("employees").get().await()
                }

                val holidayQuery = async {
                    db.collection("holidays")
                        .whereGreaterThan("date", Timestamp.now())
                        .orderBy("date", Query.Direction.ASCENDING)
                        .limit(1)
                        .get()
                        .await()
                }

                // Await all results
                val pendingApprovals = pendingApprovalsQuery.await().size()
                val totalEmployees = employeeStatsQuery.await().size()
                val holidays = holidayQuery.await()

                var holidayName = "No upcoming holidays"
                var holidayDate = ""
                if (!holidays.isEmpty) {
                    val nextHolidayDoc = holidays.documents.first()
                    holidayName = nextHolidayDoc.getString("name") ?: "Holiday"
                    holidayDate = nextHolidayDoc.getTimestamp("date")?.toDate()?.let {
                        dateFormatter.format(it)
                    } ?: ""
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingApprovals = pendingApprovals,
                        totalEmployees = totalEmployees,
                        nextHolidayName = holidayName,
                        nextHolidayDate = holidayDate
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
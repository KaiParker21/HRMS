package com.skye.hrms.data.viewmodels.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skye.hrms.utilities.PerformanceMetrics
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

// --- 1. UPDATED THE UI STATE ---
data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val pendingApprovals: Int = 0,
    val totalEmployees: Int = 0,
    val onLeaveToday: Int = 0, // <-- This will now be populated
    val nextHolidayName: String = "No upcoming holidays",
    val nextHolidayDate: String = "",
    val averageReviewScores: Map<String, Float> = emptyMap(), // <-- ADDED
    val errorMessage: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
class AdminDashboardViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAdminData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Get the timestamp for the start of today
                val todayTimestamp = Timestamp(Date.from(
                    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                ))

                // Query for pending approvals
                val pendingApprovalsQuery = async {
                    db.collection("leave_requests")
                        .whereEqualTo("status", "Pending")
                        .get()
                        .await()
                }

                // Query for employee stats (we'll use this for 2 things)
                val employeeStatsQuery = async {
                    db.collection("employees").get().await()
                }

                // Query for next holiday
                val holidayQuery = async {
                    db.collection("holidays")
                        .whereGreaterThan("date", Timestamp.now())
                        .orderBy("date", Query.Direction.ASCENDING)
                        .limit(1)
                        .get()
                        .await()
                }

                // --- 2. NEW QUERY for "On Leave Today" ---
                val onLeaveTodayQuery = async {
                    db.collection("leave_requests")
                        .whereEqualTo("status", "Approved")
                        .whereLessThanOrEqualTo("startDate", todayTimestamp)
                        .get()
                        .await()
                }

                // Await all results
                val pendingApprovals = pendingApprovalsQuery.await().size()
                val employeeSnapshot = employeeStatsQuery.await()
                val holidays = holidayQuery.await()
                val onLeaveSnapshot = onLeaveTodayQuery.await()

                // --- 3. CALCULATE "On Leave Today" ---
                var onLeaveCount = 0
                for (doc in onLeaveSnapshot.documents) {
                    val endDate = doc.getTimestamp("endDate")
                    // Check if the leave ends today or in the future
                    if (endDate != null && endDate.toDate().time >= todayTimestamp.toDate().time) {
                        onLeaveCount++
                    }
                }

                // --- 4. CALCULATE Average Performance ---
                val totalEmployees = employeeSnapshot.size()
                val totalsMap = PerformanceMetrics.labels.associateWith { 0.0f }.toMutableMap()
                var validReviewCount = 0

                for (doc in employeeSnapshot.documents) {
                    val rawReviewMap = doc.get("performanceReview") as? Map<String, Number>
                    val reviewMap = rawReviewMap?.mapValues { it.value.toFloat() }

                    if (reviewMap != null) {
                        validReviewCount++
                        for (label in PerformanceMetrics.labels) {
                            totalsMap[label] = (totalsMap[label] ?: 0f) + (reviewMap[label] ?: 0f)
                        }
                    }
                }

                val averagesMap = if (validReviewCount > 0) {
                    totalsMap.mapValues { it.value / validReviewCount }
                } else {
                    emptyMap() // No reviews found
                }

                // (Holiday logic remains the same)
                var holidayName = "No upcoming holidays"
                var holidayDate = ""
                if (!holidays.isEmpty) {
                    val nextHolidayDoc = holidays.documents.first()
                    holidayName = nextHolidayDoc.getString("name") ?: "Holiday"
                    holidayDate = nextHolidayDoc.getTimestamp("date")?.toDate()?.let {
                        dateFormatter.format(it)
                    } ?: ""
                }

                // --- 5. UPDATE THE STATE ---
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingApprovals = pendingApprovals,
                        totalEmployees = totalEmployees,
                        onLeaveToday = onLeaveCount, // <-- Updated
                        nextHolidayName = holidayName,
                        nextHolidayDate = holidayDate,
                        averageReviewScores = averagesMap // <-- Added
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
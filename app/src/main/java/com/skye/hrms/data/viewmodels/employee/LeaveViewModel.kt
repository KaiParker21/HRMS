package com.skye.hrms.data.viewmodels.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Leave State
sealed class LeaveSubmissionState {
    object Idle : LeaveSubmissionState()
    object Loading : LeaveSubmissionState()
    object Success : LeaveSubmissionState()
    data class Error(val message: String) : LeaveSubmissionState()
}

// --- NEW ---
// Data class to hold a single history item
data class LeaveHistoryItem(
    val id: String,
    val dateRange: String,
    val leaveType: String,
    val status: String,
    val reason: String
)

// --- NEW ---
// UI state for the history list
data class LeaveHistoryState(
    val isLoading: Boolean = true,
    val history: List<LeaveHistoryItem> = emptyList(),
    val errorMessage: String? = null
)

class LeaveViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    private val _leaveSubmissionState = MutableStateFlow<LeaveSubmissionState>(LeaveSubmissionState.Idle)
    val submissionState = _leaveSubmissionState.asStateFlow()

    private val _historyState = MutableStateFlow(LeaveHistoryState())
    val historyState = _historyState.asStateFlow()

    init {
        // --- NEW ---
        fetchLeaveHistory()
    }

    private fun fetchLeaveHistory() {
        _historyState.value = LeaveHistoryState(isLoading = true)
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _historyState.value = LeaveHistoryState(isLoading = false, errorMessage = "User not logged in.")
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("leave_requests")
                    .whereEqualTo("userId", userId)
                    .orderBy("requestedAt", Query.Direction.DESCENDING)
                    .limit(20) // Get the last 20 requests
                    .get()
                    .await()

                val historyList = snapshot.documents.map { doc ->
                    val startDate = doc.getTimestamp("startDate")?.toDate()?.let { dateFormatter.format(it) } ?: ""
                    val endDate = doc.getTimestamp("endDate")?.toDate()?.let { dateFormatter.format(it) } ?: ""
                    LeaveHistoryItem(
                        id = doc.id,
                        dateRange = "$startDate - $endDate",
                        leaveType = doc.getString("leaveType") ?: "N/A",
                        status = doc.getString("status") ?: "N/A",
                        reason = doc.getString("reason") ?: "No reason"
                    )
                }
                _historyState.value = LeaveHistoryState(isLoading = false, history = historyList)

            } catch (e: Exception) {
                _historyState.value = LeaveHistoryState(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun submitLeaveRequest(
        startDate: Date,
        endDate: Date,
        leaveType: String,
        reason: String
    ) {
        _leaveSubmissionState.value = LeaveSubmissionState.Loading
        val user = auth.currentUser

        if (user == null) {
            _leaveSubmissionState.value = LeaveSubmissionState.Error("You must be logged in.")
            return
        }

        viewModelScope.launch {
            try {
                val employeeDoc = db.collection("employees").document(user.uid).get().await()
                val fullName = employeeDoc.getString("fullName") ?: "Unknown User"

                val leaveRequest = hashMapOf(
                    "userId" to user.uid,
                    "fullName" to fullName,
                    "startDate" to Timestamp(startDate),
                    "endDate" to Timestamp(endDate),
                    "leaveType" to leaveType,
                    "reason" to reason,
                    "status" to "Pending",
                    "requestedAt" to FieldValue.serverTimestamp()
                )

                db.collection("leave_requests").add(leaveRequest).await()
                fetchLeaveHistory()
                _leaveSubmissionState.value = LeaveSubmissionState.Success

            } catch (e: Exception) {
                _leaveSubmissionState.value = LeaveSubmissionState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // Funcition to reset submission state
    fun resetSubmissionState() {
        _leaveSubmissionState.value = LeaveSubmissionState.Idle
    }
}
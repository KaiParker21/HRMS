package com.skye.hrms.data.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Data class to represent a single leave request
data class LeaveRequest(
    val id: String,
    val employeeName: String,
    val leaveType: String,
    val reason: String,
    val dateRange: String,
    val status: String
)

// UI state for the approval screen
data class LeaveApprovalUiState(
    val isLoading: Boolean = true,
    val pendingRequests: List<LeaveRequest> = emptyList(),
    val errorMessage: String? = null
)

class LeaveApprovalViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    private val _uiState = MutableStateFlow(LeaveApprovalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchPendingRequests()
    }

    // Fetches all requests with "Pending" status
    fun fetchPendingRequests() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("leave_requests")
                    .whereEqualTo("status", "Pending")
                    .orderBy("requestedAt", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val requests = snapshot.documents.map { doc ->
                    val startDate = doc.getTimestamp("startDate")?.toDate()?.let { dateFormatter.format(it) } ?: ""
                    val endDate = doc.getTimestamp("endDate")?.toDate()?.let { dateFormatter.format(it) } ?: ""

                    LeaveRequest(
                        id = doc.id,
                        employeeName = doc.getString("fullName") ?: "N/A",
                        leaveType = doc.getString("leaveType") ?: "N/A",
                        reason = doc.getString("reason") ?: "No reason provided.",
                        dateRange = "$startDate - $endDate",
                        status = doc.getString("status") ?: "Pending"
                    )
                }

                _uiState.update { it.copy(isLoading = false, pendingRequests = requests) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // Updates the status of a specific request
    fun updateRequestStatus(requestId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                db.collection("leave_requests").document(requestId)
                    .update("status", newStatus)
                    .await()

                // Refresh the list after an update
                fetchPendingRequests()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update status: ${e.message}") }
            }
        }
    }
}
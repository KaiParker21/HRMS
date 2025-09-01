package com.skye.hrms.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

sealed class LeaveSubmissionState {
    object Idle : LeaveSubmissionState()
    object Loading : LeaveSubmissionState()
    object Success : LeaveSubmissionState()
    data class Error(val message: String) : LeaveSubmissionState()
}

class LeaveViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _leaveSubmissionState = MutableStateFlow<LeaveSubmissionState>(LeaveSubmissionState.Idle)
    val submissionState = _leaveSubmissionState.asStateFlow()

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
                // First, fetch the user's full name from their employee profile
                val employeeDoc = db.collection("employees").document(user.uid).get().await()
                val fullName = employeeDoc.getString("fullName") ?: "Unknown User"

                // Create the leave request map
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

                // Add the new document to the 'leave_requests' collection
                db.collection("leave_requests").add(leaveRequest).await()
                _leaveSubmissionState.value = LeaveSubmissionState.Success

            } catch (e: Exception) {
                _leaveSubmissionState.value = LeaveSubmissionState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // Function to reset the state, e.g., after the UI has shown a success message
    fun resetSubmissionState() {
        _leaveSubmissionState.value = LeaveSubmissionState.Idle
    }
}
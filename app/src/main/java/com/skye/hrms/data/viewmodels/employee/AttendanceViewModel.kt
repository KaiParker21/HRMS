package com.skye.hrms.data.viewmodels.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Record data class
data class AttendanceRecord(
    val id: String,
    val date: String,
    val clockInTime: String,
    val clockOutTime: String,
    val status: String,
    val totalHours: Double
)

// UI state
data class AttendanceUiState(
    val isLoading: Boolean = true,
    val records: List<AttendanceRecord> = emptyList(),
    val errorMessage: String? = null
)

class AttendanceViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    init {
        fetchAttendanceHistory()
    }

    // Get attendance history
    private fun fetchAttendanceHistory() {
        _uiState.update { it.copy(isLoading = true) }
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not found.") }
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("employees").document(userId)
                    .collection("attendance")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .await()

                val records = snapshot.documents.mapNotNull { doc ->
                    val clockInTs = doc.getTimestamp("clockInTime")
                    val clockOutTs = doc.getTimestamp("clockOutTime")
                    val dateTs = doc.getTimestamp("date")

                    AttendanceRecord(
                        id = doc.id,
                        date = dateTs?.toDate()?.let { dateFormatter.format(it) } ?: "No Date",
                        clockInTime = clockInTs?.toDate()?.let { timeFormatter.format(it) } ?: "---",
                        clockOutTime = clockOutTs?.toDate()?.let { timeFormatter.format(it) } ?: "---",
                        status = doc.getString("status") ?: "N/A",
                        totalHours = doc.getDouble("totalHours") ?: 0.0
                    )
                }

                _uiState.update { it.copy(isLoading = false, records = records) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
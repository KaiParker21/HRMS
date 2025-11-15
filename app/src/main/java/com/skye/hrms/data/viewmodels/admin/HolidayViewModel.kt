package com.skye.hrms.data.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skye.hrms.data.viewmodels.SubmissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for displaying a holiday
data class HolidayItem(
    val id: String,
    val name: String,
    val date: String,
    val dayOfWeek: String
)

// UI state for the list of holidays
data class HolidayUiState(
    val isLoading: Boolean = true,
    val holidays: List<HolidayItem> = emptyList(),
    val errorMessage: String? = null
)

class HolidayViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())

    private val _uiState = MutableStateFlow(HolidayUiState())
    val uiState = _uiState.asStateFlow()

    // Separate state for the "Add" button
    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    init {
        fetchHolidays()
    }

    fun fetchHolidays() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("holidays")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val holidayList = snapshot.documents.map { doc ->
                    val date = doc.getTimestamp("date")?.toDate() ?: Date()
                    HolidayItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "N/A",
                        date = dateFormatter.format(date),
                        dayOfWeek = dayFormatter.format(date)
                    )
                }
                _uiState.update { it.copy(isLoading = false, holidays = holidayList) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addHoliday(name: String, date: Date) {
        _submissionState.value = SubmissionState.Loading
        viewModelScope.launch {
            try {
                val holidayData = hashMapOf(
                    "name" to name,
                    "date" to Timestamp(date)
                )
                db.collection("holidays").add(holidayData).await()
                _submissionState.value = SubmissionState.Success
                fetchHolidays() // Refresh the list
            } catch (e: Exception) {
                _submissionState.value = SubmissionState.Error(e.message ?: "Failed to add holiday")
            }
        }
    }

    fun deleteHoliday(holidayId: String) {
        viewModelScope.launch {
            try {
                db.collection("holidays").document(holidayId).delete().await()
                fetchHolidays() // Refresh the list
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete") }
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }
}
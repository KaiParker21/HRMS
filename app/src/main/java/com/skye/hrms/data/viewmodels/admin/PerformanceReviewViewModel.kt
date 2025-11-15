package com.skye.hrms.data.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skye.hrms.utilities.PerformanceMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class to hold an employee and their review for the list
data class EmployeeReview(
    val userId: String,
    val fullName: String,
    val designation: String,
    val reviewScores: Map<String, Float>
)

data class PerformanceReviewUiState(
    val isLoading: Boolean = true,
    val employees: List<EmployeeReview> = emptyList(),
    val errorMessage: String? = null
)

class PerformanceReviewViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PerformanceReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchEmployeeReviews()
    }

    fun fetchEmployeeReviews() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val snapshot = db.collection("employees")
                    .orderBy("fullName", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val employeeList = snapshot.documents.map { doc ->
                    // Get the review map, or use default values if it's missing
                    val reviewMap = doc.get("performanceReview") as? Map<String, Float>
                        ?: PerformanceMetrics.getDefaultMap()

                    EmployeeReview(
                        userId = doc.id,
                        fullName = doc.getString("fullName") ?: "No Name",
                        designation = doc.getString("designation") ?: "N/A",
                        reviewScores = reviewMap
                    )
                }
                _uiState.update { it.copy(isLoading = false, employees = employeeList) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // Updates the scores in Firestore
    fun updateEmployeeReview(userId: String, newScores: Map<String, Float>) {
        viewModelScope.launch {
            try {
                db.collection("employees").document(userId)
                    .update("performanceReview", newScores)
                    .await()
                // Refresh the list after the update is successful
                fetchEmployeeReviews()
            } catch (e: Exception) {
                // Handle error (e.g., show a toast)
            }
        }
    }
}
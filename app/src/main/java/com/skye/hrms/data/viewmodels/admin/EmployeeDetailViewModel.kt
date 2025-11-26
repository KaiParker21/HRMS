package com.skye.hrms.data.viewmodels.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.hrms.data.viewmodels.common.OnboardingFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// UI state for the detail screen
data class EmployeeDetailUiState(
    val isLoading: Boolean = true,
    val employee: OnboardingFormData? = null, // We'll reuse the onboarding data model
    val errorMessage: String? = null
)

class EmployeeDetailViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(EmployeeDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadEmployeeDetails(userId: String) {
        if (userId.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User ID is missing.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Fetch the document and convert it directly to our OnboardingFormData class
                val employeeData = db.collection("employees").document(userId)
                    .get()
                    .await()
                    .toObject(OnboardingFormData::class.java)

                if (employeeData == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Employee not found.") }
                } else {
                    _uiState.update { it.copy(isLoading = false, employee = employeeData) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
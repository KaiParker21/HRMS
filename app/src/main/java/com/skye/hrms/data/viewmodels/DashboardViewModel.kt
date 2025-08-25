package com.skye.hrms.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DashboardState(
    val employeeName: String = "",
    val employeeDesignation: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchEmployeeData()
    }

    private fun fetchEmployeeData() {
        _uiState.update { it.copy(isLoading = true) }
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            try {
                val document = db.collection("employees").document(userId).get().await()
                if (document.exists()) {
                    val name = document.getString("fullName") ?: "Employee"
                    val designation = document.getString("designation") ?: "N/A"
                    _uiState.update {
                        it.copy(
                            employeeName = name,
                            employeeDesignation = designation,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Profile not found.") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An error occurred."
                    )
                }
            }
        }
    }
}
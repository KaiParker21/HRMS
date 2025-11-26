package com.skye.hrms.data.viewmodels.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// A simple data class for the list
data class EmployeeListItem(
    val userId: String,
    val fullName: String,
    val designation: String,
    val department: String,
    val isClockedIn: Boolean // <-- ADD THIS
)

// UI state for the employee list screen
data class EmployeeListUiState(
    val isLoading: Boolean = true,
    val employees: List<EmployeeListItem> = emptyList(),
    val errorMessage: String? = null
)

class EmployeeListViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(EmployeeListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchEmployees()
    }

    fun fetchEmployees() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("employees")
                    .orderBy("fullName", Query.Direction.ASCENDING) // List alphabetically
                    .get()
                    .await()

                val employeeList = snapshot.documents.map { doc ->
                    EmployeeListItem(
                        userId = doc.id,
                        fullName = doc.getString("fullName") ?: "No Name",
                        designation = doc.getString("designation") ?: "N/A",
                        department = doc.getString("department") ?: "N/A",
                        isClockedIn = doc.getBoolean("isClockedIn") ?: false // <-- ADD THIS
                    )
                }

                _uiState.update {
                    it.copy(isLoading = false, employees = employeeList)
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }
}
package com.skye.hrms.data.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp // <-- Import Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.hrms.utilities.PerformanceMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Submission State
sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    object Success : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

// Data class for education item
data class EducationItem(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val university: String = "",
    val year: String = "",
    val specialisation: String = ""
)

// Data class for onboarding data
data class OnboardingFormData(
    val role: String = "EMPLOYEE",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val currentAddress: String = "",
    val permanentAddress: String = "",
    val isPermanentAddressSameAsCurrent: Boolean = false,
    val contactNumber: String = "",
    val employeeId: String = "",
    val dateOfJoining: String = "",
    val department: String = "",
    val designation: String = "",
    val educationalHistory: List<EducationItem> = listOf(EducationItem()),
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",

    val isClockedIn: Boolean = false,
    val lastClockInTime: Timestamp? = null,
    val leaveBalances: List<Map<String, Any>> = listOf(
        mapOf("type" to "Casual", "balance" to 6.0, "total" to 6.0),
        mapOf("type" to "Sick", "balance" to 6.0, "total" to 6.0),
        mapOf("type" to "Unpaid", "balance" to 6.0, "total" to 6.0)
    ),

    val performanceReview: Map<String, Float> = PerformanceMetrics.getDefaultMap()
)

class OnboardingViewModel: ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(OnboardingFormData())
    val formData = _formData.asStateFlow()

    // Function to submit the form
    fun submitForm() {
        _submissionState.value = SubmissionState.Loading
        val userID = auth.currentUser?.uid

        if (userID == null) {
            _submissionState.value = SubmissionState.Error("User is not logged in")
            return
        }

        viewModelScope.launch {
            try {
                db.collection("employees").document(userID).set(_formData.value).await()
                _submissionState.value = SubmissionState.Success
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred."
                _submissionState.value = SubmissionState.Error(errorMessage)
            }
        }
    }

    fun onNextStep() {
        if(_currentStep.value < 5) {
            _currentStep.value++
        }
    }

    fun onPreviousStep() {
        if(_currentStep.value > 1) {
            _currentStep.value--
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }

    fun updateFullName(name: String) = _formData.update { it.copy(fullName = name) }
    fun updateDateOfBirth(date: String) = _formData.update { it.copy(dateOfBirth = date) }
    fun updateGender(gender: String) = _formData.update { it.copy(gender = gender) }

    fun updateCurrentAddress(address: String) = _formData.update { it.copy(currentAddress = address) }
    fun updatePermanentAddress(address: String) = _formData.update { it.copy(permanentAddress = address) }
    fun togglePermanentAddressSameAsCurrent(isSame: Boolean) {
        _formData.update {
            val newPermanentAddress = if(isSame) it.currentAddress else ""
            it.copy(
                isPermanentAddressSameAsCurrent = isSame,
                permanentAddress = newPermanentAddress
            )
        }
    }
    fun updateContactNumber(number: String) = _formData.update { it.copy(contactNumber = number) }

    fun updateEmployeeId(id: String) = _formData.update { it.copy(employeeId = id) }
    fun updateDateOfJoining(date: String) = _formData.update { it.copy(dateOfJoining = date) }
    fun updateDepartment(department: String) = _formData.update { it.copy(department = department) }
    fun updateDesignation(designation: String) = _formData.update { it.copy(designation = designation) }

    fun updateEducationItem(item: EducationItem) {
        _formData.update { currentState ->
            val newList = currentState.educationalHistory.map { oldItem->
                if (oldItem.id == item.id) item else oldItem
            }
            currentState.copy(educationalHistory = newList)
        }
    }

    fun addEducationItem() {
        _formData.update { it.copy(educationalHistory = it.educationalHistory + EducationItem()) }
    }

    fun removeEducationItem(item: EducationItem) {
        _formData.update {
            if (it.educationalHistory.size > 1) {
                it.copy(educationalHistory = it.educationalHistory.filterNot { it.id == item.id })
            } else {
                it
            }
        }
    }

    fun updateEmergencyContactName(name: String) = _formData.update { it.copy(emergencyContactName = name) }
    fun updateEmergencyContactNumber(number: String) = _formData.update { it.copy(emergencyContactNumber = number) }
}
package com.skye.hrms.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp // <-- Import Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class SubmissionState {
    object Idle : LeaveSubmissionState()
    object Loading : LeaveSubmissionState()
    object Success : LeaveSubmissionState()
    data class Error(val message: String) : LeaveSubmissionState()
}

data class EducationItem(
    val id: String = UUID.randomUUID().toString(),
    val degree: String = "",
    val university: String = "",
    val year: String = "",
    val specialisation: String = ""
)

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

    // --- CHANGES START HERE ---
    // Added fields to ensure every new user document has the required data for the dashboard.

    val isClockedIn: Boolean = false,
    val lastClockInTime: Timestamp? = null,
    val leaveBalances: List<Map<String, Any>> = listOf(
        mapOf("type" to "Casual", "balance" to 12.0, "total" to 12.0),
        mapOf("type" to "Sick", "balance" to 6.0, "total" to 6.0)
    )
    // --- CHANGES END HERE ---
)

class OnboardingViewModel: ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _submissionState = MutableStateFlow<LeaveSubmissionState>(LeaveSubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(OnboardingFormData())
    val formData = _formData.asStateFlow()

    fun submitForm() {
        _submissionState.value = LeaveSubmissionState.Loading
        val userID = auth.currentUser?.uid

        if (userID == null) {
            _submissionState.value = LeaveSubmissionState.Error("User is not logged in")
            return
        }

        viewModelScope.launch {
            try {
                // No changes needed here. It automatically saves the entire updated formData object.
                db.collection("employees").document(userID).set(_formData.value).await()
                _submissionState.value = LeaveSubmissionState.Success
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred."
                _submissionState.value = LeaveSubmissionState.Error(errorMessage)
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
        _submissionState.value = LeaveSubmissionState.Idle
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
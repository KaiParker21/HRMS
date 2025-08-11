package com.skye.hrms.data.viewmodels

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    object Success : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

data class EducationItem(
    val id: Int = System.identityHashCode(Any()),
    val degree: String = "",
    val university: String = "",
    val year: String = "",
    val specialisation: String = ""
)

data class OnboardingFormData(
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
)

class OnboardingViewModel: ViewModel() {

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    private val _formData = MutableStateFlow(OnboardingFormData())
    val formData = _formData.asStateFlow()

    fun submitForm() {
        _submissionState.value = SubmissionState.Loading
        val userID = Firebase.auth.currentUser?.uid

        if (userID == null) {
            _submissionState.value = SubmissionState.Error("User is not logged in")
            return
        }

        val finalFormData = _formData.value

        val db = Firebase.firestore
        val employeeDocument = db.collection("employees").document(userID)

        employeeDocument.set(finalFormData)
            .addOnSuccessListener {
                _submissionState.value = SubmissionState.Success
                println("SUCCESS: Onboarding data saved to Firestore.")
            }
            .addOnFailureListener { e ->
                _submissionState.value = SubmissionState.Error(e.message ?: "An unknown error occurred.")
                println("ERROR: Failed to save data. Reason: ${e.message}")
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
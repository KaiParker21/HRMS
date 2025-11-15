package com.skye.hrms.data.viewmodels.admin

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.skye.hrms.data.viewmodels.SubmissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// State for this screen
data class PayslipUploadUiState(
    val isLoadingEmployees: Boolean = true,
    val employees: List<EmployeeListItem> = emptyList() // Reusing EmployeeListItem
)

class PayslipUploadViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow(PayslipUploadUiState())
    val uiState = _uiState.asStateFlow()

    // Separate state for the submission
    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submissionState = _submissionState.asStateFlow()

    init {
        fetchAllEmployees()
    }

    // Fetches all employees for the dropdown
    private fun fetchAllEmployees() {
        _uiState.update { it.copy(isLoadingEmployees = true) }
        viewModelScope.launch {
            try {
                val snapshot = db.collection("employees")
                    .orderBy("fullName", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val employeeList = snapshot.documents.map { doc ->
                    EmployeeListItem(
                        userId = doc.id,
                        fullName = doc.getString("fullName") ?: "No Name",
                        designation = doc.getString("designation") ?: "N/A",
                        department = doc.getString("department") ?: "N/A",
                        isClockedIn = doc.getBoolean("isClockedIn") ?: false
                    )
                }
                _uiState.update { it.copy(isLoadingEmployees = false, employees = employeeList) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingEmployees = false) }
                // Handle error
            }
        }
    }

    fun uploadPayslip(
        userId: String,
        uri: Uri,
        context: Context,
        month: String,
        year: Long
    ) {
        _submissionState.value = SubmissionState.Loading

        val fileName = context.getFileName(uri) ?: "payslip_${month}_${year}.pdf"
        val storageRef = storage.reference.child("payslips/$userId/$fileName")

        viewModelScope.launch {
            try {
                // 1. Upload file to Storage
                val uploadTask = storageRef.putFile(uri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                // 2. Create metadata in Firestore
                val payslipMetadata = hashMapOf(
                    "month" to month,
                    "year" to year,
                    "issueDate" to FieldValue.serverTimestamp(),
                    "downloadUrl" to downloadUrl,
                    "storagePath" to storageRef.path
                )

                db.collection("employees").document(userId)
                    .collection("payslips")
                    .add(payslipMetadata)
                    .await()

                _submissionState.value = SubmissionState.Success

            } catch (e: Exception) {
                _submissionState.value = SubmissionState.Error(e.message ?: "File upload failed")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionState.Idle
    }
}

private fun Context.getFileName(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}

package com.skye.hrms.data.viewmodels

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

// Data class to hold a single payslip record
// (This might already exist from your admin modules, you can reuse it)
data class PayslipInfo(
    val id: String,
    val month: String,
    val year: Long,
    val issueDate: String,
    val downloadUrl: String
)

// UI state for the payslip screen
data class PayslipUiState(
    val isLoading: Boolean = true,
    val payslips: List<PayslipInfo> = emptyList(),
    val errorMessage: String? = null
)

class ViewPayslipViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    init {
        fetchPayslipHistory()
    }

    private fun fetchPayslipHistory() {
        _uiState.update { it.copy(isLoading = true) }
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not found.") }
            return
        }

        viewModelScope.launch {
            try {
                // Fetch payslip records, ordered by date
                val snapshot = db.collection("employees").document(userId)
                    .collection("payslips")
                    .orderBy("issueDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val records = snapshot.documents.mapNotNull { doc ->
                    val issueDateTs = doc.getTimestamp("issueDate")
                    PayslipInfo(
                        id = doc.id,
                        month = doc.getString("month") ?: "N/A",
                        year = doc.getLong("year") ?: 2025L,
                        issueDate = issueDateTs?.toDate()?.let { dateFormatter.format(it) } ?: "N/A",
                        downloadUrl = doc.getString("downloadUrl") ?: ""
                    )
                }.filter { it.downloadUrl.isNotBlank() } // Only show payslips with a valid URL

                _uiState.update { it.copy(isLoading = false, payslips = records) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
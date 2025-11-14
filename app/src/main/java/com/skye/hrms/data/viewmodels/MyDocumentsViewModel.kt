package com.skye.hrms.data.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Model for a document's metadata
data class DocumentInfo(
    val id: String = "",
    val title: String = "",
    val fileName: String = "",
    val dateUploaded: String = "",
    val downloadUrl: String = "",
    val storagePath: String = "" // Needed for deletion
)

// UI state for the screen
sealed interface DocumentUiState {
    object Loading : DocumentUiState
    data class Success(val documents: List<DocumentInfo>) : DocumentUiState
    data class Error(val message: String) : DocumentUiState
}

// State for the upload process
sealed interface UploadState {
    object Idle : UploadState
    object Uploading : UploadState
    data class Error(val message: String) : UploadState
}

class MyDocumentsViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow<DocumentUiState>(DocumentUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    init {
        fetchDocuments()
    }

    fun fetchDocuments() {
        _uiState.value = DocumentUiState.Loading
        viewModelScope.launch {
            try {
                val snapshot = db.collection("employees").document(userId)
                    .collection("documents")
                    .orderBy("dateUploaded", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val documents = snapshot.documents.mapNotNull { doc ->
                    val date = (doc.getTimestamp("dateUploaded")?.toDate() ?: Date())
                    DocumentInfo(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        fileName = doc.getString("fileName") ?: "Unknown File",
                        dateUploaded = dateFormatter.format(date),
                        downloadUrl = doc.getString("downloadUrl") ?: "",
                        storagePath = doc.getString("storagePath") ?: ""
                    )
                }.filter { it.downloadUrl.isNotBlank() }

                _uiState.value = DocumentUiState.Success(documents)

            } catch (e: Exception) {
                _uiState.value = DocumentUiState.Error(e.message ?: "Failed to fetch documents")
            }
        }
    }

    fun uploadDocument(uri: Uri, context: Context) {
        _uploadState.value = UploadState.Uploading
        val fileName = context.getFileName(uri) ?: "unknown_file_${System.currentTimeMillis()}"
        val storageRef = storage.reference.child("documents/$userId/$fileName")

        viewModelScope.launch {
            try {
                // 1. Upload file to Firebase Storage
                val uploadTask = storageRef.putFile(uri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                // 2. Create metadata in Firestore
                val documentMetadata = hashMapOf(
                    "title" to fileName.substringBeforeLast('.'), // Use filename as title for simplicity
                    "fileName" to fileName,
                    "dateUploaded" to FieldValue.serverTimestamp(),
                    "downloadUrl" to downloadUrl,
                    "storagePath" to storageRef.path
                )

                db.collection("employees").document(userId)
                    .collection("documents")
                    .add(documentMetadata)
                    .await()

                _uploadState.value = UploadState.Idle
                fetchDocuments() // Refresh the list after successful upload

            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "File upload failed")
            }
        }
    }

    fun deleteDocument(document: DocumentInfo) {
        viewModelScope.launch {
            try {
                // Step 1: Delete the file from Firebase Storage
                // We use storagePath (e.g., "documents/userId/file.pdf")
                if (document.storagePath.isNotEmpty()) {
                    storage.getReference(document.storagePath).delete().await()
                } else {
                    // Fallback for older data without storagePath
                    storage.getReferenceFromUrl(document.downloadUrl).delete().await()
                }

                // Step 2: Delete the metadata document from Cloud Firestore
                db.collection("employees").document(userId)
                    .collection("documents")
                    .document(document.id)
                    .delete()
                    .await()

                // Step 3: Refresh the local UI list
                fetchDocuments()

            } catch (e: Exception) {
                // Handle errors (e.g., show a toast via a state)
                Log.e("DeleteError", "Failed to delete document: ${e.message}")
                // Optionally re-fetch to ensure UI is in sync
                fetchDocuments()
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

// Helper function to get the real file name from a content URI
private fun Context.getFileName(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}
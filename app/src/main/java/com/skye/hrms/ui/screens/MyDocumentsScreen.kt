package com.skye.hrms.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.DocumentInfo
import com.skye.hrms.data.viewmodels.DocumentUiState
import com.skye.hrms.data.viewmodels.MyDocumentsViewModel
import com.skye.hrms.data.viewmodels.UploadState
import com.skye.hrms.ui.themes.HRMSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(
    onBackClicked: () -> Unit,
    viewModel: MyDocumentsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadDocument(it, context)
        }
    }

    // Handle Upload State changes
    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is UploadState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetUploadState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Documents", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Upload Document")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is DocumentUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is DocumentUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is DocumentUiState.Success -> {
                    if (state.documents.isEmpty()) {
                        Text(
                            text = "No documents found. Tap the + button to upload.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.documents) { doc ->
                                DocumentItem(
                                    document = doc,
                                    onViewClicked = {
                                        downloadDocument(context, doc.downloadUrl, doc.fileName)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Show a full-screen loading overlay during upload
            AnimatedVisibility(visible = uploadState is UploadState.Uploading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading file, please wait...")
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItem(document: DocumentInfo, onViewClicked: () -> Unit) {
    // Simple icon logic, you can expand this
    val icon = when {
        document.title.contains("Letter", true) -> Icons.Outlined.Badge
        else -> Icons.AutoMirrored.Outlined.Article
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Document",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Uploaded: ${document.dateUploaded}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onViewClicked) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = "Download Document",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper function to start the download
private fun downloadDocument(context: Context, url: String, title: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Downloading Document...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)

        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true)
@Composable
fun MyDocumentsScreenPreview() {
    HRMSTheme {
        MyDocumentsScreen(onBackClicked = {})
    }
}
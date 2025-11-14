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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
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
import com.skye.hrms.ui.components.DocumentFabMenu
import com.skye.hrms.ui.themes.HRMSTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyDocumentsScreen(
    onBackClicked: () -> Unit,
    viewModel: MyDocumentsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var fabExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var documentToDelete by remember { mutableStateOf<DocumentInfo?>(null) }

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
            DocumentFabMenu(
                expanded = fabExpanded,
                onToggle = { fabExpanded = it },
                onItemClicked = { item ->
                    // Launch the file picker with the specific MIME type
                    filePickerLauncher.launch(item.mimeType)
                    // Close the menu after clicking
                    fabExpanded = false
                }
            )
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
                    CircularWavyProgressIndicator()
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
                                    },
                                    onDeleteClicked = {
                                        documentToDelete = doc
                                        showDeleteDialog = true
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularWavyProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading file, please wait...")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                documentToDelete = null
            },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to delete '${documentToDelete?.fileName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentToDelete?.let {
                            viewModel.deleteDocument(it) // Call the ViewModel function
                        }
                        showDeleteDialog = false
                        documentToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        documentToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DocumentItem(
    document: DocumentInfo,
    onViewClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    // Get the file extension from the fileName
    val fileExtension = document.fileName.substringAfterLast('.', "").lowercase()

    // Choose an icon based on the file extension
    val icon: ImageVector = when (fileExtension) {
        "pdf" -> Icons.Outlined.PictureAsPdf
        "jpg", "jpeg", "png" -> Icons.Outlined.Image
        "doc", "docx" -> Icons.AutoMirrored.Outlined.Article
        else -> Icons.AutoMirrored.Outlined.Article // Default file icon
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, // <-- New dynamic icon
                contentDescription = "File Type",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title, // <-- Still shows title
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = document.fileName, // <-- Shows the actual filename
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Download button
            IconButton(onClick = onViewClicked) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = "Download Document",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Delete button
            IconButton(onClick = onDeleteClicked) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete Document",
                    tint = MaterialTheme.colorScheme.error
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
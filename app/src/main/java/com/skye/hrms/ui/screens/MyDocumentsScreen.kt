package com.skye.hrms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class DocumentInfo(
    val id: String,
    val title: String,
    val category: String,
    val dateUploaded: String
)

private sealed interface DocumentUiState {
    object Loading : DocumentUiState
    data class Success(val documents: List<DocumentInfo>) : DocumentUiState
    data class Error(val message: String) : DocumentUiState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyDocumentsScreen(
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<DocumentUiState>(DocumentUiState.Loading) }

    LaunchedEffect(Unit) {
        delay(1500)

        val dummyDocuments = listOf(
            DocumentInfo("1", "Offer Letter", "Onboarding", "Jul 15, 2025"),
            DocumentInfo("2", "Employment Contract", "Onboarding", "Jul 20, 2025"),
            DocumentInfo("3", "ID Proof (Aadhaar)", "Verification", "Jul 16, 2025"),
            DocumentInfo("4", "Form 16 - 2024", "Tax", "Apr 30, 2025"),
            DocumentInfo("5", "NDA Agreement", "Legal", "Jul 20, 2025")
        )

        uiState = DocumentUiState.Success(dummyDocuments)
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
                            text = "No documents found.",
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
                                        Toast.makeText(context, "Viewing ${doc.title}...", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItem(document: DocumentInfo, onViewClicked: () -> Unit) {
    val icon = when (document.category) {
        "Onboarding" -> Icons.Outlined.Badge
        "Verification" -> Icons.Outlined.GppGood
        else -> Icons.AutoMirrored.Outlined.Article
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = onViewClicked
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = document.category,
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
        }
    }
}
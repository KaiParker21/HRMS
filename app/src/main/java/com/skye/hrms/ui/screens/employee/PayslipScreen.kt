package com.skye.hrms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class PayslipInfoMock(
    val id: String,
    val month: String,
    val year: String,
    val issueDate: String
)

private sealed interface PayslipUiState {
    object Loading : PayslipUiState
    data class Success(val payslips: List<PayslipInfoMock>) : PayslipUiState
    data class Error(val message: String) : PayslipUiState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PayslipScreen(
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<PayslipUiState>(PayslipUiState.Loading) }

    LaunchedEffect(Unit) {
        delay(1500)

        val dummyPayslips = listOf(
            PayslipInfoMock("1", "October", "2025", "Oct 31, 2025"),
            PayslipInfoMock("2", "September", "2025", "Sep 30, 2025"),
            PayslipInfoMock("3", "August", "2025", "Aug 31, 2025"),
            PayslipInfoMock("4", "July", "2025", "Jul 31, 2025"),
            PayslipInfoMock("5", "June", "2025", "Jun 30, 2025"),
            PayslipInfoMock("6", "May", "2025", "May 31, 2025")
        )

        uiState = PayslipUiState.Success(dummyPayslips)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payslips", fontWeight = FontWeight.SemiBold) },
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
                is PayslipUiState.Loading -> {
                    CircularWavyProgressIndicator()
                }
                is PayslipUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is PayslipUiState.Success -> {
                    if (state.payslips.isEmpty()) {
                        Text(
                            text = "No payslips found.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.payslips) { payslip ->
                                PayslipItem(
                                    payslip = payslip,
                                    onDownloadClicked = {
                                        Toast.makeText(context, "Downloading ${payslip.month} payslip...", Toast.LENGTH_SHORT).show()
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
fun PayslipItem(payslip: PayslipInfoMock, onDownloadClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${payslip.month} ${payslip.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Issued: ${payslip.issueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDownloadClicked) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = "Download Payslip",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
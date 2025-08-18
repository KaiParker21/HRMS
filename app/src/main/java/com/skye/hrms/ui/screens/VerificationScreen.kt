package com.skye.hrms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.data.viewmodels.VerificationState

@Composable
fun VerificationScreen(
    authViewModel: AuthViewModel,
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val verificationState by authViewModel.verificationState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(verificationState) {
        when (val state = verificationState) {
            is VerificationState.Verified -> {
                onVerified()
            }
            is VerificationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                authViewModel.resetVerificationState()
            }
            is VerificationState.EmailSent -> {
                snackbarHostState.showSnackbar("A new verification email has been sent.")
                authViewModel.resetVerificationState()
            }
            else -> Unit
        }
    }

    val isLoading = verificationState is VerificationState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MarkEmailRead,
                    contentDescription = "Email Verification",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We've sent a verification link to your email address. Please click the link to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { authViewModel.checkVerificationStatus() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("I've Verified My Email")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { authViewModel.sendVerificationEmail() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Resend Verification Email")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBackToLogin, enabled = !isLoading) {
                    Text("Back to Login")
                }
            }
        }
    }
}

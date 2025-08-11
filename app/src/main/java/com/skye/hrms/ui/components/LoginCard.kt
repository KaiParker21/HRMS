package com.skye.hrms.ui.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skye.hrms.data.viewmodels.AuthState
import com.skye.hrms.data.viewmodels.AuthViewModel

val expressiveCardShape: Shape = RoundedCornerShape(
    topStart = 40.dp, topEnd = 12.dp, bottomEnd = 40.dp, bottomStart = 12.dp
)
val expressiveButtonShape: Shape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 4.dp
)

@Composable
fun LoginCard(
    onGoToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        val context = LocalContext.current
        val supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        if (supportsBlur) {
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = (-100).dp, y = (-150).dp)
                    .blur(100.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(400.dp)
                    .offset(x = (120).dp, y = (100).dp)
                    .blur(100.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }

        LoginCardContent(
            onGoToSignup = onGoToSignup,
            onLoginSuccess = onLoginSuccess,
            authViewModel = authViewModel
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LoginCardContent(
    onGoToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localErrorText by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.observeAsState(initial = AuthState.Idle)
    val isLoading = authState is AuthState.Loading
    val firebaseErrorText = (authState as? AuthState.Error)?.message
    val currentDisplayErrorText = localErrorText ?: firebaseErrorText

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                localErrorText = null
                onLoginSuccess()
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> { localErrorText = null }
            else -> { localErrorText = null }
        }
    }

    val performLogin = {
        focusManager.clearFocus()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        when {
            email.isBlank() || password.isBlank() -> {
                localErrorText = "Email and password cannot be empty."
            }
            password.length < 8 -> {
                localErrorText = "Password must be at least 8 characters."
            }
            else -> {
                localErrorText = null
                authViewModel.signInWithEmailAndPassword(email.trim(), password)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            shape = expressiveCardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let's Go!",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sign in to continue your journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier.height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isLoading to currentDisplayErrorText,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        }, label = "Status Animation"
                    ) { (loading, error) ->
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        } else if (error != null) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; localErrorText = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email Address") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = currentDisplayErrorText != null
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; localErrorText = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { performLogin() }),
                    singleLine = true,
                    isError = currentDisplayErrorText != null
                )

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = { performLogin() },
                    shape = expressiveButtonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(12.dp))

                ElevatedButton(
                    onClick = onGoToSignup,
                    shape = expressiveButtonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    enabled = !isLoading
                ) {
                    Text(
                        "Create an Account",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
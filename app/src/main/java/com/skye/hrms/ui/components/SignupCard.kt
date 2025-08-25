//package com.skye.hrms.ui.components
//
//import android.annotation.SuppressLint
//import android.os.Build
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.imePadding
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ElevatedButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.blur
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardCapitalization
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import com.skye.hrms.data.viewmodels.AuthState
//import com.skye.hrms.data.viewmodels.AuthViewModel
//
//@Composable
//fun SignupCard(
//    onGoToLogin: () -> Unit,
//    onSignupSuccess: () -> Unit,
//    authViewModel: AuthViewModel
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        MaterialTheme.colorScheme.surface,
//                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
//                    )
//                )
//            )
//    ) {
//        val context = LocalContext.current
//        val supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
//
//        if (supportsBlur) {
//            Box(
//                modifier = Modifier
//                    .size(350.dp)
//                    .offset(x = (80).dp, y = (550).dp)
//                    .blur(100.dp)
//                    .background(
//                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
//                        shape = CircleShape
//                    )
//            )
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .size(400.dp)
//                    .offset(x = (120).dp, y = (-100).dp)
//                    .blur(100.dp)
//                    .background(
//                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
//                        shape = CircleShape
//                    )
//            )
//        }
//
//        SignupCardContent(
//            onGoToLogin = onGoToLogin,
//            onSignupSuccess = onSignupSuccess,
//            authViewModel = authViewModel
//        )
//    }
//}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//private fun SignupCardContent(
//    onGoToLogin: () -> Unit,
//    onSignupSuccess: () -> Unit,
//    authViewModel: AuthViewModel
//) {
//    val haptics = LocalHapticFeedback.current
//    val focusManager = LocalFocusManager.current
//
//    var name by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var isPasswordVisible by remember { mutableStateOf(false) }
//    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
//    var localErrorText by remember { mutableStateOf<String?>(null) }
//
//    val authState by authViewModel.authState.observeAsState(initial = AuthState.Idle)
//    val isLoading = authState is AuthState.Loading
//    val firebaseErrorText = (authState as? AuthState.Error)?.message
//    val currentDisplayErrorText = localErrorText ?: firebaseErrorText
//
//    LaunchedEffect(authState) {
//        when (val state = authState) {
//            is AuthState.Success -> {
//                localErrorText = null
//                onSignupSuccess()
//                authViewModel.resetAuthState()
//            }
//            is AuthState.Error -> { localErrorText = null }
//            else -> { localErrorText = null }
//        }
//    }
//
//    val performSignup = {
//        focusManager.clearFocus()
//        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
//        when {
//            name.isBlank() || email.isBlank() || password.isBlank() -> {
//                localErrorText = "All fields are required."
//            }
//            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
//                localErrorText = "Please enter a valid email address."
//            }
//            password.length < 8 -> {
//                localErrorText = "Password must be at least 8 characters."
//            }
//            password != confirmPassword -> {
//                localErrorText = "Passwords do not match."
//            }
//            else -> {
//                localErrorText = null
//                authViewModel.signUpWithEmailAndPassword(email.trim(), password)
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .imePadding()
//            .pointerInput(Unit) {
//                detectTapGestures(onTap = { focusManager.clearFocus() })
//            },
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth(0.9f)
//                .verticalScroll(rememberScrollState()),
//            shape = expressiveCardShape,
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(horizontal = 28.dp, vertical = 40.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Create Account",
//                    style = MaterialTheme.typography.displaySmall,
//                    color = MaterialTheme.colorScheme.primary
//                )
//                Text(
//                    text = "Start your journey with us",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Spacer(Modifier.height(24.dp))
//
//                Box(
//                    modifier = Modifier.height(40.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    AnimatedContent(
//                        targetState = isLoading to currentDisplayErrorText,
//                        transitionSpec = {
//                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
//                        }, label = "Status Animation"
//                    ) { (loading, error) ->
//                        if (loading) {
//                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
//                        } else if (error != null) {
//                            Text(
//                                text = error,
//                                color = MaterialTheme.colorScheme.error,
//                                style = MaterialTheme.typography.bodyMedium,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it; localErrorText = null },
//                    modifier = Modifier.fillMaxWidth(),
//                    label = { Text("Full Name") },
//                    shape = RoundedCornerShape(16.dp),
//                    keyboardOptions = KeyboardOptions(
//                        capitalization = KeyboardCapitalization.Words,
//                        imeAction = ImeAction.Next
//                    ),
//                    singleLine = true,
//                    isError = currentDisplayErrorText != null && name.isBlank()
//                )
//
//                Spacer(Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it; localErrorText = null },
//                    modifier = Modifier.fillMaxWidth(),
//                    label = { Text("Email Address") },
//                    shape = RoundedCornerShape(16.dp),
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Email,
//                        imeAction = ImeAction.Next
//                    ),
//                    singleLine = true,
//                    isError = currentDisplayErrorText != null
//                )
//
//                Spacer(Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = password,
//                    onValueChange = { password = it; localErrorText = null },
//                    modifier = Modifier.fillMaxWidth(),
//                    label = { Text("Password") },
//                    shape = RoundedCornerShape(16.dp),
//                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                    trailingIcon = {
//                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                            Icon(
//                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                contentDescription = "Toggle password visibility"
//                            )
//                        }
//                    },
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Password,
//                        imeAction = ImeAction.Next
//                    ),
//                    singleLine = true,
//                    isError = currentDisplayErrorText != null
//                )
//
//                Spacer(Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = confirmPassword,
//                    onValueChange = { confirmPassword = it; localErrorText = null },
//                    modifier = Modifier.fillMaxWidth(),
//                    label = { Text("Confirm Password") },
//                    shape = RoundedCornerShape(16.dp),
//                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                    trailingIcon = {
//                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
//                            Icon(
//                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                                contentDescription = "Toggle password visibility"
//                            )
//                        }
//                    },
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Password,
//                        imeAction = ImeAction.Done
//                    ),
//                    keyboardActions = KeyboardActions(onDone = { performSignup() }),
//                    singleLine = true,
//                    isError = currentDisplayErrorText != null && password != confirmPassword
//                )
//
//                Spacer(Modifier.height(32.dp))
//
//                Button(
//                    onClick = { performSignup() },
//                    shape = expressiveButtonShape,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    enabled = !isLoading
//                ) {
//                    Text(
//                        text = "Sign Up",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                }
//
//                Spacer(Modifier.height(12.dp))
//
//                ElevatedButton(
//                    onClick = onGoToLogin,
//                    shape = expressiveButtonShape,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    colors = ButtonDefaults.elevatedButtonColors(
//                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
//                    ),
//                    enabled = !isLoading
//                ) {
//                    Text(
//                        "Already have an account? Log In",
//                        style = MaterialTheme.typography.titleMedium,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//        }
//    }
//}
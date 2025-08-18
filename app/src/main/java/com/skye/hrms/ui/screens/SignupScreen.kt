package com.skye.hrms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skye.hrms.data.viewmodels.AuthState
import com.skye.hrms.data.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
) {
    val authState by authViewModel.authState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                onSignupSuccess()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    val isLoading = authState is AuthState.Loading

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
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(1000)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(50.dp, 10.dp, 30.dp, 30.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start your adventure with us.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 500)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(1000, delayMillis = 500)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)
                    ) {

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusEvent { event ->
                                    if (event.isFocused) {
                                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                    }
                                },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email Icon"
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusEvent { event ->
                                    if (event.isFocused) {
                                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                    }
                                },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Password Icon"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    authViewModel.signUpWithEmailAndPassword(
                                        email = email.trim(),
                                        password = password
                                    )
                                }
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                authViewModel.signUpWithEmailAndPassword(
                                    email = email.trim(),
                                    password = password
                                )
                                focusManager.clearFocus()
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(text = "Sign Up", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Sign Up Arrow"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 1000)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(1000, delayMillis = 1000)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                            Text("Log In", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }
        }
    }
}
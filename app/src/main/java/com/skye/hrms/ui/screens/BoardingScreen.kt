package com.skye.hrms.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skye.hrms.R
import com.skye.hrms.ui.themes.HRMSTheme

@Composable
fun BoardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    val haptics = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val isDark = isSystemInDarkTheme()

    val textColor = if (isDark) Color.White else Color.Black
    val buttonColor = if (isDark) Color(
        0xFF5D9C39
    ) else Color(
        0xFF73BD44
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.institute_logo),
                contentDescription = "institute_logo",
                modifier = Modifier.size(220.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedButton(
                onClick = {
                    onNavigateToRegister()
                    haptics.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(
                        0xFF72BC43
                    )
                ),
                modifier = Modifier
                    .padding(
                        top = 24.dp,
                        bottom = 2.dp
                    )
                    .widthIn(max = 320.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(
                        0xFF72BC43
                    ),
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Register",
                    fontSize = 18.sp
                )
            }

            ElevatedButton(
                onClick = {
                    onNavigateToLogin()
                    haptics.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                },
                modifier = Modifier
                    .padding(
                        top = 24.dp,
                        bottom = 2.dp
                    )
                    .widthIn(max = 320.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = buttonColor
                )
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp
                )
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoardingScreenPreview() {
    HRMSTheme {
        BoardingScreen(
            onNavigateToRegister = {},
            onNavigateToLogin = {}
        )
    }
}
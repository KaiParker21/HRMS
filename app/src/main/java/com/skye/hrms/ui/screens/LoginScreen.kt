package com.skye.hrms.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.skye.hrms.R
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.ui.components.LoginCard

@Composable
fun LoginScreen(
    onGoToSignup: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.discs_background),
//            contentDescription = "Logo",
//            contentScale = ContentScale.FillBounds,
//            modifier = Modifier
//                .fillMaxSize(),
//            alignment = Alignment.TopStart
//        )

        LoginCard(
            onGoToSignup = onGoToSignup,
            onLoginSuccess = onLoginSuccess,
            authViewModel = authViewModel
        )
    }
}
package com.skye.hrms.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.hrms.ui.helpers.Screens
import com.skye.hrms.ui.themes.HRMSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(
    navController: NavController,
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            navController.navigate(Screens.LoginScreen.route) {
                popUpTo(Screens.SplashScreen.route) { inclusive = true }
            }
        } else {
            if (!currentUser.isEmailVerified) {
                navController.navigate(Screens.VerificationScreen.route) {
                    popUpTo(Screens.SplashScreen.route) { inclusive = true }
                }
            } else {
                try {
                    val doc = firestore.collection("employees")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    if (doc.exists()) {
                        navController.navigate(Screens.DashboardScreen.route) {
                            popUpTo(Screens.SplashScreen.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screens.OnBoardingScreen.route) {
                            popUpTo(Screens.SplashScreen.route) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.SplashScreen.route) { inclusive = true }
                    }
                    Log.e("SplashScreen", "Error checking user: ${e.message}")
                }
            }
        }

        isChecking = false
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isChecking) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Checking your account...", fontSize = 18.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun CircularBarPreview() {
    HRMSTheme {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (true) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Checking your account...", fontSize = 18.sp)
                    }
                }
            }
        }

    }
}

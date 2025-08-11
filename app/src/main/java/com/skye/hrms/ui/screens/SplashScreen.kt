package com.skye.hrms.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashScreen(
    onNavigateToBoarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("tree.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        speed = 1.0F
    )

    val currentScale = remember { Animatable(0f) }

    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        currentScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutQuad
            )
        )
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            currentScale.animateTo(
                targetValue = 0.0f,
                animationSpec = tween(durationMillis = 700)
            )
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 700)) {
                onNavigateToBoarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .scale(currentScale.value)
            .background(Color.White.copy(alpha = alpha.value)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.fillMaxSize()
        )
    }

}
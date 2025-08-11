package com.skye.hrms.ui.helpers

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

object ScreenTransitions {

    // Unified animation specs for consistency across screen + components
    val unifiedEnterSpec = tween<Float>(
        durationMillis = 600,
        easing = FastOutSlowInEasing
    )

    val unifiedExitSpec = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    // Optional: used for scale transitions as well
    val scaleInEnterSpec = tween<Float>(
        durationMillis = 600,
        easing = FastOutSlowInEasing
    )

    val scaleOutExitSpec = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    val fadeScaleEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        fadeIn(animationSpec = unifiedEnterSpec) + scaleIn(
            initialScale = 0.96f,
            animationSpec = scaleInEnterSpec
        )
    }

    val fadeScaleExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        fadeOut(animationSpec = unifiedExitSpec) + scaleOut(
            targetScale = 1.02f,
            animationSpec = scaleOutExitSpec
        )
    }

    val fadeScalePopEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        fadeIn(animationSpec = unifiedEnterSpec) + scaleIn(
            initialScale = 0.96f,
            animationSpec = scaleInEnterSpec
        )
    }

    val fadeScalePopExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        fadeOut(animationSpec = unifiedExitSpec) + scaleOut(
            targetScale = 1.02f,
            animationSpec = scaleOutExitSpec
        )
    }
}
package com.skye.hrms.ui.helpers

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.ui.screens.BoardingScreen
import com.skye.hrms.ui.screens.HomeScreen
import com.skye.hrms.ui.screens.LoginScreen
import com.skye.hrms.ui.screens.OnBoardingScreen
import com.skye.hrms.ui.screens.SignupScreen
import com.skye.hrms.ui.screens.SplashScreen

@Composable
fun Navigation(
    authViewModel: AuthViewModel
) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.LoginScreen.route
    ) {
        composable(
            route = Screens.SplashScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            SplashScreen(
                onNavigateToBoarding = {
                    navController.navigate(Screens.SplashScreen.route)
                },
                onNavigateToLogin = {

                }
            )
        }

        composable(
            route = Screens.BoardingScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            BoardingScreen(
                onNavigateToRegister = {
                    navController.navigate(Screens.SignupScreen.route) {
                        popUpTo(Screens.BoardingScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.BoardingScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.LoginScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            LoginScreen(
                onGoToSignup = {
                    navController.navigate(Screens.SignupScreen.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screens.HomeScreen.route)
                },
                authViewModel = authViewModel
            )

        }

        composable(
            route = Screens.SignupScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            SignupScreen(
                onGoToLogin = {
                    navController.navigate(Screens.LoginScreen.route)
                },
                onSignupSuccess = {
                    navController.navigate(Screens.OnBoardingScreen.route)
                },
                authViewModel = authViewModel
            )

        }

        composable(
            route = Screens.HomeScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            HomeScreen()
        }

        composable(
            route = Screens.OnBoardingScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            OnBoardingScreen(
                onFormSubmitted = {
                    navController.navigate(Screens.HomeScreen.route)
                }
            )
        }


    }
}
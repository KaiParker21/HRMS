package com.skye.hrms.ui.helpers

sealed class Screens(val route: String) {
    object SplashScreen : Screens("splash_screen")
    object LoginScreen: Screens("login_screen")
    object SignupScreen: Screens("register_screen")
    object HomeScreen: Screens("home_screen")
    object BoardingScreen: Screens("boarding_screen")
    object OnBoardingScreen: Screens("onboarding_screen")
    object VerificationScreen: Screens("verification_screen")
    object DashboardScreen: Screens("dashboard_screen")
    object ApplyLeaveScreen: Screens("apply_leave_screen")
}
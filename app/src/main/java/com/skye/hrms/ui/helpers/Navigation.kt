package com.skye.hrms.ui.helpers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skye.hrms.data.viewmodels.AuthViewModel
import com.skye.hrms.ui.screens.ApplyLeaveScreen
import com.skye.hrms.ui.screens.AttendanceScreen
import com.skye.hrms.ui.screens.DashboardScreen
import com.skye.hrms.ui.screens.LoginScreen
import com.skye.hrms.ui.screens.MyDocumentsScreen
import com.skye.hrms.ui.screens.OnBoardingScreen
import com.skye.hrms.ui.screens.PayslipScreen
import com.skye.hrms.ui.screens.SignupScreen
import com.skye.hrms.ui.screens.SplashScreen
import com.skye.hrms.ui.screens.VerificationScreen
import com.skye.hrms.ui.screens.admin.AdminDashboardScreen
import com.skye.hrms.ui.screens.admin.EmployeeDetailScreen
import com.skye.hrms.ui.screens.admin.EmployeeListScreen
import com.skye.hrms.ui.screens.admin.LeaveApprovalScreen

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Navigation(
    authViewModel: AuthViewModel
) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.SplashScreen.route
    ) {
        composable(
            route = Screens.SplashScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            SplashScreen(
                navController = navController
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
                authViewModel = authViewModel,
                onNavigateToSignup = {
                    navController.navigate(Screens.SignupScreen.route) {
                        popUpTo(Screens.LoginScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screens.SplashScreen.route) {
                        popUpTo(Screens.LoginScreen.route) {
                            inclusive = true
                        }
                    }
                }
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
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.SignupScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onSignupSuccess = {
                    navController.navigate(Screens.SplashScreen.route) {
                        popUpTo(Screens.SignupScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )

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
                    navController.navigate(Screens.DashboardScreen.route) {
                        popUpTo(Screens.OnBoardingScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.VerificationScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            VerificationScreen(
                authViewModel = authViewModel,
                onVerified = {
                    navController.navigate(Screens.OnBoardingScreen.route) {
                        popUpTo(Screens.VerificationScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.VerificationScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.DashboardScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            DashboardScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    navController.navigate(Screens.SplashScreen.route) {
                        popUpTo(Screens.DashboardScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onApplyLeaveClicked = {
                    navController.navigate(Screens.ApplyLeaveScreen.route)
                },
                onAttendanceClicked = {
                    navController.navigate(Screens.AttendanceScreen.route)
                },
                onPayslipClicked = {
                    navController.navigate(Screens.PayslipScreen.route)
                },
                onMyDocumentsClicked = {
                    navController.navigate(Screens.MyDocumentsScreen.route)
                }
            )
        }

        composable(
            route = Screens.MyDocumentsScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            MyDocumentsScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screens.PayslipScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            PayslipScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screens.AttendanceScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            AttendanceScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screens.ApplyLeaveScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            ApplyLeaveScreen(
                onBackClicked = {
                    navController.navigate(Screens.DashboardScreen.route) {
                        popUpTo(Screens.ApplyLeaveScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.AdminDashboardScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            AdminDashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                onNavigateToEmployeeList = {
                    navController.navigate(Screens.EmployeeListScreen.route)
                },
                onNavigateToLeaveApprovals = {
                    navController.navigate(Screens.LeaveApprovalScreen.route)
                },
                onSignOut = {
                    navController.navigate(Screens.SplashScreen.route) {
                        popUpTo(Screens.DashboardScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screens.LeaveApprovalScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            LeaveApprovalScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screens.EmployeeListScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit
        ) {
            EmployeeListScreen(
                onBackClicked = {
                    navController.popBackStack()
                },
                onEmployeeClicked = { userId ->
                    navController.navigate(Screens.EmployeeDetailScreen.createRoute(userId))
                }
            )
        }

        composable(
            route = Screens.EmployeeDetailScreen.route,
            enterTransition = ScreenTransitions.fadeScaleEnter,
            exitTransition = ScreenTransitions.fadeScaleExit,
            popEnterTransition = ScreenTransitions.fadeScalePopEnter,
            popExitTransition = ScreenTransitions.fadeScalePopExit,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            EmployeeDetailScreen(
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
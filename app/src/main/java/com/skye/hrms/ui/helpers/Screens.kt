package com.skye.hrms.ui.helpers

sealed class Screens(val route: String) {
    object SplashScreen : Screens("splash_screen")
    object LoginScreen: Screens("login_screen")
    object SignupScreen: Screens("register_screen")
    object OnBoardingScreen: Screens("onboarding_screen")
    object VerificationScreen: Screens("verification_screen")
    object DashboardScreen: Screens("dashboard_screen")
    object ApplyLeaveScreen: Screens("apply_leave_screen")
    object AttendanceScreen: Screens("attendance_screen")
    object PayslipScreen: Screens("payslip_screen")
    object MyDocumentsScreen: Screens("my_documents_screen")
    object AdminDashboardScreen: Screens("admin_dashboard_screen")
    object LeaveApprovalScreen: Screens("leave_approval_screen")
    object EmployeeListScreen: Screens("employee_list_screen")

}
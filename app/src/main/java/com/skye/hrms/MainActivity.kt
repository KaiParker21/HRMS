package com.skye.hrms

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.skye.hrms.data.viewmodels.common.AuthViewModel
import com.skye.hrms.ui.helpers.Navigation
import com.skye.hrms.ui.themes.HRMSTheme


class MainActivity :
    ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            HRMSTheme {
                Navigation(
                    authViewModel = authViewModel
                )
            }
        }
    }
}

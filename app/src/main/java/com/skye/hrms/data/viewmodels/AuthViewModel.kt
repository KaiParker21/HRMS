package com.skye.hrms.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle: AuthState()
    object Loading: AuthState()
    object Success: AuthState()
    data class Error(val message: String): AuthState()
}

sealed class VerificationState {
    object Idle: VerificationState()
    object Loading: VerificationState()
    object Verified: VerificationState()
    object EmailSent: VerificationState()
    data class Error(val message: String): VerificationState()
}

class AuthViewModel: ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> get() = _authState

    private val _verificationState = MutableLiveData<VerificationState>(VerificationState.Idle)
    val verificationState: LiveData<VerificationState> get() = _verificationState

    fun signUpWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.sendEmailVerification()?.await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "This email address is already in use."
                    is FirebaseAuthInvalidCredentialsException -> "The password is too weak or the email is invalid."
                    else -> e.message ?: "An unknown error occurred during sign up."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No user found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> e.message ?: "An unknown error occurred during sign in."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                _verificationState.value = VerificationState.EmailSent
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to send email.")
            }
        }
    }

    fun checkVerificationStatus() {
        _verificationState.value = VerificationState.Loading
        viewModelScope.launch {
            try {
                auth.currentUser?.reload()?.await()
                if (auth.currentUser?.isEmailVerified == true) {
                    _verificationState.value = VerificationState.Verified
                } else {
                    _verificationState.value = VerificationState.Error("Email is not yet verified. Please check your inbox.")
                }
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun resetAuthState() {
        if (_authState.value != AuthState.Loading) {
            _authState.value = AuthState.Idle
        }
    }

    fun resetVerificationState() {
        if (_verificationState.value != VerificationState.Loading) {
            _verificationState.value = VerificationState.Idle
        }
    }
}

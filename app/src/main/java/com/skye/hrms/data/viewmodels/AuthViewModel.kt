package com.skye.hrms.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle: AuthState()
    object Loading: AuthState()
    object Success: AuthState()
    data class Error(val message: String): AuthState()
}

class AuthViewModel: ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState>
        get() = _authState

    fun signUpWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "This email address is already in use."
                    is FirebaseAuthInvalidCredentialsException -> "The password is too weak or invalid email."
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
                val result = auth.signInWithEmailAndPassword(email, password).await()
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

    fun signOut() {
        if (_authState.value == AuthState.Loading) {
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Unknown error during sign out"
                _authState.value = AuthState.Error("Sign out failed: $errorMessage")
            }
        }
    }

    fun resetAuthState() {
        if (_authState.value != AuthState.Loading) {
            _authState.value =
                AuthState.Idle
        }
    }


}
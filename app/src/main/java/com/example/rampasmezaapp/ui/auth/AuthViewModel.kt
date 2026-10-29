package com.example.rampasmezaapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {

        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = _authState.value.copy(user = firebaseAuth.currentUser)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
            }
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .await()
                }
                
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "El formato del email no es válido"
            "The password is invalid or the user does not have a password." -> "Las credenciales son incorrectas o no existen"
            "The supplied auth credential is incorrect." -> "Las credenciales son incorrectas o no existen"
            "The supplied auth credential is incorrect, malformed or has expired." -> "Las credenciales son incorrectas o no existen"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No existe una cuenta con este email"
            "The email address is already in use by another account." -> "Este email ya está registrado"
            "The given password is invalid. [ Password should be at least 6 characters ]" -> "La contraseña debe tener al menos 6 caracteres"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Error de conexión. Verifica tu internet"
            "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]" -> "Las credenciales son incorrectas o no existen"
            "An internal error has occurred. [ INVALID_EMAIL ]" -> "El email no es válido"
            "An internal error has occurred. [ USER_NOT_FOUND ]" -> "No existe una cuenta con este email"
            "An internal error has occurred. [ WRONG_PASSWORD ]" -> "Las credenciales son incorrectas o no existen"
            else -> "Las credenciales son incorrectas o no existen"
        }
    }
}

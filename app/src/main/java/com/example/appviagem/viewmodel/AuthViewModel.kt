package com.example.appviagem.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appviagem.data.local.entity.Usuario
import com.example.appviagem.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UsuarioRepository) : ViewModel() {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")

    // Usuário logado
    var loggedUser by mutableStateOf<Usuario?>(null)
        private set

    fun login(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Preencha todos os campos"
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.login(email, password)
                if (user != null) {
                    loggedUser = user
                    errorMessage = ""
                    clearFields()
                    onSuccess()
                } else {
                    errorMessage = "Email ou senha inválidos"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao realizar login"
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || phone.isBlank()
            || password.isBlank() || confirmPassword.isBlank()
        ) {
            errorMessage = "Todos os campos são obrigatórios"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Senhas não coincidem"
            return
        }
        viewModelScope.launch {
            try {
                repository.salvar(
                    Usuario(
                        nome = name,
                        email = email,
                        telefone = phone,
                        senha = password
                    )
                )
                successMessage = "Usuário cadastrado com sucesso!"
                errorMessage = ""
                clearFields()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Erro ao salvar usuário"
            }
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        if (email.isBlank()) {
            errorMessage = "Informe o email"
        } else {
            errorMessage = ""
            onSuccess()
        }
    }

    fun logout() {
        loggedUser = null
        clearFields()
    }

    private fun clearFields() {
        name = ""
        email = ""
        phone = ""
        password = ""
        confirmPassword = ""
    }
}
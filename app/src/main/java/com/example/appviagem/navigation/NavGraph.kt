package com.example.appviagem.navigation

import android.content.Context
import com.example.appviagem.ui.screens.LoginScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appviagem.data.local.AppDatabase
import com.example.appviagem.data.repository.UsuarioRepository
import com.example.appviagem.ui.screens.ForgotPasswordScreen
import com.example.appviagem.ui.screens.MenuScreen
import com.example.appviagem.ui.screens.RegisterScreen
import com.example.appviagem.viewmodel.AuthViewModel

@Composable
fun NavGraph(context: Context) {

    val navController = rememberNavController()

    // 🔹 Banco
    val db = AppDatabase.getDatabase(context)
    val dao = db.usuarioDao()

    // 🔹 Repository
    val repository = UsuarioRepository(dao)

    // 🔹 ViewModel com factory
    val vm: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repository) as T
            }
        }
    )

    // 🔹 Navegação
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController, vm)
        }

        composable("register") {
            RegisterScreen(navController, vm)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController, vm)
        }

        composable(
            route = "menu/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            MenuScreen(email)
        }
    }
}
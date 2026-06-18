package com.example.appviagem.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.appviagem.data.local.AppDatabase
import com.example.appviagem.data.repository.UsuarioRepository
import com.example.appviagem.data.repository.ViagemRepository
import com.example.appviagem.ui.screens.*
import com.example.appviagem.viewmodel.AuthViewModel
import com.example.appviagem.viewmodel.ViagemViewModel

@Composable
fun NavGraph(context: Context) {
    val navController = rememberNavController()

    // Banco
    val db = AppDatabase.getDatabase(context)

    // Repositories
    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val viagemRepository = ViagemRepository(db.viagemDao())

    // ViewModels
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(usuarioRepository) as T
            }
        }
    )

    val viagemViewModel: ViagemViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ViagemViewModel(viagemRepository) as T
            }
        }
    )

    // Navegação
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        composable("forgot") {
            ForgotPasswordScreen(navController, authViewModel)
        }

        composable("menu") {
            MenuScreen(navController, authViewModel)
        }

        composable("nova_viagem") {
            NovaViagemScreen(navController, authViewModel, viagemViewModel)
        }

        composable("minhas_viagens") {
            MinhasViagensScreen(navController, authViewModel, viagemViewModel)
        }

        composable("sobre") {
            SobreScreen(navController)
        }

        composable(
            route = "galeria_fotos/{viagemId}/{destino}",
            arguments = listOf(
                navArgument("viagemId") { type = NavType.IntType },
                navArgument("destino") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getInt("viagemId") ?: 0
            val destino = backStackEntry.arguments?.getString("destino").orEmpty()
            GaleriaFotosScreen(
                navController = navController,
                viagemId = viagemId,
                destino = destino
            )
        }
    }
}

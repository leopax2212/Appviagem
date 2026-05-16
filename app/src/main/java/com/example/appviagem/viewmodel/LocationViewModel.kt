package com.example.appviagem.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appviagem.data.local.AppDatabase
import com.example.appviagem.data.local.entity.Viagem
import com.example.appviagem.data.location.LocationRepository
import com.example.appviagem.data.repository.ViagemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val city: String? = null,
    val viagemAtual: Viagem? = null,
    val totalGastos: Double = 0.0
)

class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val viagemRepository: ViagemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasPermission = true, isLoading = true) }
        viewModelScope.launch {
            locationRepository.locationWithCityFlow()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { location ->
                    _uiState.update {
                        it.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            city = location.city,
                            isLoading = false
                        )
                    }

                    // Buscar viagem ativa para a cidade
                    location.city?.let { cidade ->
                        buscarViagemPorCidade(cidade)
                    }
                }
        }
    }

    private suspend fun buscarViagemPorCidade(cidade: String) {
        val viagem = viagemRepository.buscarViagemAtivaPorCidade(cidade)
        _uiState.update {
            it.copy(
                viagemAtual = viagem,
                totalGastos = 0.0 // Por enquanto zero, conforme solicitado
            )
        }
    }

    fun limparViagem() {
        _uiState.update { it.copy(viagemAtual = null) }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val db = AppDatabase.getDatabase(appContext)
                val locationRepository = LocationRepository(appContext)
                val viagemRepository = ViagemRepository(db.viagemDao())
                LocationViewModel(locationRepository, viagemRepository)
            }
        }
    }
}
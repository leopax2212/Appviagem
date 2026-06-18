package com.example.appviagem.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appviagem.data.local.AppDatabase
import com.example.appviagem.data.local.entity.Foto
import com.example.appviagem.data.repository.FotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FotoViewModel(
    private val repository: FotoRepository
) : ViewModel() {

    private val _fotos = MutableStateFlow<List<Foto>>(emptyList())
    val fotos: StateFlow<List<Foto>> = _fotos

    private var viagemIdAtual: Int? = null

    fun carregarFotos(viagemId: Int) {
        viagemIdAtual = viagemId
        viewModelScope.launch {
            repository.listarPorViagem(viagemId).collectLatest { lista ->
                _fotos.value = lista
            }
        }
    }

    // Cria o arquivo de destino para captura via camera e retorna seu caminho
    fun criarArquivoCaptura(): File = repository.novoArquivoFoto()

    // Vincula uma foto capturada pela camera (ja gravada no caminho informado)
    fun vincularFotoCapturada(caminho: String) {
        val viagemId = viagemIdAtual ?: return
        viewModelScope.launch {
            repository.vincularFoto(viagemId, caminho)
        }
    }

    // Importa uma foto selecionada da galeria e vincula a viagem
    fun vincularFotoDaGaleria(uri: Uri) {
        val viagemId = viagemIdAtual ?: return
        viewModelScope.launch {
            runCatching {
                val caminho = repository.copiarDeUri(uri)
                repository.vincularFoto(viagemId, caminho)
            }
        }
    }

    fun excluir(foto: Foto) {
        viewModelScope.launch {
            repository.excluir(foto)
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val db = AppDatabase.getDatabase(appContext)
                val fotoRepository = FotoRepository(db.fotoDao(), appContext)
                FotoViewModel(fotoRepository)
            }
        }
    }
}

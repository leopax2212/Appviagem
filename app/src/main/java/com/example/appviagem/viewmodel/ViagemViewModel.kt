package com.example.appviagem.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appviagem.data.local.entity.Viagem
import com.example.appviagem.data.repository.ViagemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViagemViewModel(private val repository: ViagemRepository) : ViewModel() {

    // Campos do formulário
    var destino by mutableStateOf("")
    var tipo by mutableStateOf("lazer")
    var dataInicio by mutableStateOf<Long?>(null)
    var dataFim by mutableStateOf<Long?>(null)
    var orcamento by mutableStateOf("")

    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")

    // Viagem em edição
    var viagemEmEdicao by mutableStateOf<Viagem?>(null)
        private set

    // Lista de viagens
    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens

    fun carregarViagens(userId: Int) {
        viewModelScope.launch {
            repository.listarPorUsuario(userId).collectLatest { lista ->
                _viagens.value = lista
            }
        }
    }

    fun salvar(userId: Int, onSuccess: () -> Unit) {
        // Validações
        if (destino.isBlank()) {
            errorMessage = "Informe o destino"
            return
        }
        if (dataInicio == null) {
            errorMessage = "Informe a data de início"
            return
        }
        if (dataFim == null) {
            errorMessage = "Informe a data de fim"
            return
        }
        if (dataFim!! < dataInicio!!) {
            errorMessage = "Data fim deve ser maior que data início"
            return
        }
        val orcamentoValue = orcamento.replace(",", ".").toDoubleOrNull()
        if (orcamentoValue == null || orcamentoValue <= 0) {
            errorMessage = "Informe um orçamento válido"
            return
        }

        viewModelScope.launch {
            try {
                val viagem = Viagem(
                    id = viagemEmEdicao?.id ?: 0,
                    destino = destino,
                    tipo = tipo,
                    dataInicio = dataInicio!!,
                    dataFim = dataFim!!,
                    orcamento = orcamentoValue,
                    userId = userId
                )

                if (viagemEmEdicao != null) {
                    repository.atualizar(viagem)
                    successMessage = "Viagem atualizada com sucesso!"
                } else {
                    repository.salvar(viagem)
                    successMessage = "Viagem salva com sucesso!"
                }

                errorMessage = ""
                limparCampos()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Erro ao salvar viagem"
            }
        }
    }

    fun excluir(viagem: Viagem, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.excluir(viagem)
                successMessage = "Viagem excluída com sucesso!"
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Erro ao excluir viagem"
            }
        }
    }

    fun prepararEdicao(viagem: Viagem) {
        viagemEmEdicao = viagem
        destino = viagem.destino
        tipo = viagem.tipo
        dataInicio = viagem.dataInicio
        dataFim = viagem.dataFim
        orcamento = viagem.orcamento.toString()
    }

    fun limparCampos() {
        viagemEmEdicao = null
        destino = ""
        tipo = "lazer"
        dataInicio = null
        dataFim = null
        orcamento = ""
        errorMessage = ""
    }
}
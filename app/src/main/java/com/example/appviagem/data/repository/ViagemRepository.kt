package com.example.appviagem.data.repository

import com.example.appviagem.data.local.dao.ViagemDao
import com.example.appviagem.data.local.entity.Viagem
import kotlinx.coroutines.flow.Flow

class ViagemRepository(private val dao: ViagemDao) {

    suspend fun salvar(viagem: Viagem): Long {
        return dao.inserir(viagem)
    }

    suspend fun atualizar(viagem: Viagem) {
        dao.atualizar(viagem)
    }

    suspend fun excluir(viagem: Viagem) {
        dao.excluir(viagem)
    }

    fun listarPorUsuario(userId: Int): Flow<List<Viagem>> {
        return dao.listarPorUsuario(userId)
    }

    suspend fun buscarPorId(id: Int): Viagem? {
        return dao.buscarPorId(id)
    }
}
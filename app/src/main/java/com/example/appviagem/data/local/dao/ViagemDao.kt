package com.example.appviagem.data.local.dao

import androidx.room.*
import com.example.appviagem.data.local.entity.Viagem
import kotlinx.coroutines.flow.Flow

@Dao
interface ViagemDao {

    @Insert
    suspend fun inserir(viagem: Viagem): Long

    @Update
    suspend fun atualizar(viagem: Viagem)

    @Delete
    suspend fun excluir(viagem: Viagem)

    @Query("SELECT * FROM viagens WHERE userId = :userId ORDER BY dataInicio DESC")
    fun listarPorUsuario(userId: Int): Flow<List<Viagem>>

    @Query("SELECT * FROM viagens WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Viagem?

    // Busca viagem ativa por cidade (case insensitive) dentro do intervalo de datas
    @Query("""
        SELECT * FROM viagens 
        WHERE LOWER(destino) = LOWER(:cidade) 
        AND :dataAtual >= dataInicio 
        AND :dataAtual <= dataFim 
        LIMIT 1
    """)
    suspend fun buscarViagemAtivaPorCidade(cidade: String, dataAtual: Long): Viagem?
}
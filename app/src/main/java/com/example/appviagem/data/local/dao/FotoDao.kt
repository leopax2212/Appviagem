package com.example.appviagem.data.local.dao

import androidx.room.*
import com.example.appviagem.data.local.entity.Foto
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoDao {

    @Insert
    suspend fun inserir(foto: Foto): Long

    @Delete
    suspend fun excluir(foto: Foto)

    @Query("SELECT * FROM fotos WHERE viagemId = :viagemId ORDER BY criadoEm DESC")
    fun listarPorViagem(viagemId: Int): Flow<List<Foto>>
}

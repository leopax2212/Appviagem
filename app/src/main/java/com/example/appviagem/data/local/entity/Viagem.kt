package com.example.appviagem.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "viagens",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Viagem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val destino: String,
    val tipo: String, // "lazer" ou "negocios"
    val dataInicio: Long, // timestamp
    val dataFim: Long, // timestamp
    val orcamento: Double,
    val userId: Int
)
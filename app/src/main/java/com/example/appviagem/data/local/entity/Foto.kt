package com.example.appviagem.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = Viagem::class,
            parentColumns = ["id"],
            childColumns = ["viagemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("viagemId")]
)
data class Foto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val viagemId: Int,
    val caminho: String, // caminho do arquivo salvo no app
    val criadoEm: Long = System.currentTimeMillis()
)

package com.example.appviagem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appviagem.data.local.dao.UsuarioDao
import com.example.appviagem.data.local.dao.ViagemDao
import com.example.appviagem.data.local.entity.Usuario
import com.example.appviagem.data.local.entity.Viagem

@Database(entities = [Usuario::class, Viagem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun viagemDao(): ViagemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration de versão 1 para 2 (adiciona tabela viagens)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS viagens (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        destino TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        dataInicio INTEGER NOT NULL,
                        dataFim INTEGER NOT NULL,
                        orcamento REAL NOT NULL,
                        userId INTEGER NOT NULL,
                        FOREIGN KEY (userId) REFERENCES usuarios(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
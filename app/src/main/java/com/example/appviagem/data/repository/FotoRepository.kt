package com.example.appviagem.data.repository

import android.content.Context
import android.net.Uri
import com.example.appviagem.data.local.dao.FotoDao
import com.example.appviagem.data.local.entity.Foto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FotoRepository(
    private val dao: FotoDao,
    private val context: Context
) {

    fun listarPorViagem(viagemId: Int): Flow<List<Foto>> {
        return dao.listarPorViagem(viagemId)
    }

    suspend fun vincularFoto(viagemId: Int, caminho: String): Long {
        return dao.inserir(Foto(viagemId = viagemId, caminho = caminho))
    }

    suspend fun excluir(foto: Foto) {
        dao.excluir(foto)
        // Remove tambem o arquivo fisico
        runCatching { File(foto.caminho).delete() }
    }

    // Diretorio onde as fotos da viagem sao armazenadas
    fun diretorioFotos(): File {
        val dir = File(context.filesDir, "fotos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // Cria um novo arquivo de destino para captura/importacao
    fun novoArquivoFoto(): File {
        return File(diretorioFotos(), "foto_${System.currentTimeMillis()}.jpg")
    }

    // Copia uma imagem selecionada da galeria para o armazenamento do app
    suspend fun copiarDeUri(origem: Uri): String = withContext(Dispatchers.IO) {
        val destino = novoArquivoFoto()
        context.contentResolver.openInputStream(origem)?.use { input ->
            FileOutputStream(destino).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Nao foi possivel abrir a imagem selecionada")
        destino.absolutePath
    }
}

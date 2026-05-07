package com.example.appviagem.data.repository

import com.example.appviagem.data.local.dao.UsuarioDao
import com.example.appviagem.data.local.entity.Usuario

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun salvar(usuario: Usuario): Long {
        return dao.inserir(usuario)
    }

    suspend fun login(email: String, senha: String): Usuario? {
        return dao.login(email, senha)
    }
}
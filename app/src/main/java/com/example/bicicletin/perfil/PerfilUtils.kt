package com.example.bicicletin.perfil

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseUser

/** Nome que aparece no perfil: nome do Firebase, ou o começo do email, ou "Usuário". */
fun FirebaseUser.nomeExibicao(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Usuário"

/** Sobe a view acima da barra de navegação (para o fim da tela não ficar escondido). */
fun View.aplicarInsetBase() {
    val baseOriginal = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, baseOriginal + barras.bottom)
        insets
    }
}

/** Empurra a view para baixo da barra de status (para o cabeçalho não ficar escondido). */
fun View.aplicarInsetTopo() {
    val topoOriginal = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(v.paddingLeft, topoOriginal + barras.top, v.paddingRight, v.paddingBottom)
        insets
    }
}

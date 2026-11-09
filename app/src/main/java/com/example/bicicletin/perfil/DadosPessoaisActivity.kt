package com.example.bicicletin.perfil

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth

/** Mostra email, senha (mascarada) e nome do usuário logado no Firebase. */
class DadosPessoaisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dados_pessoais)
        supportActionBar?.hide()

        findViewById<View>(R.id.headerSub).aplicarInsetTopo()
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.txtEmail).text = usuario.email ?: "Não informado"
        findViewById<TextView>(R.id.txtNomeDado).text = usuario.nomeExibicao()

        // O Firebase Auth NUNCA devolve a senha (ela é guardada criptografada),
        // então mostramos só os pontinhos.
        findViewById<TextView>(R.id.txtSenha).text = "••••••••"
    }
}

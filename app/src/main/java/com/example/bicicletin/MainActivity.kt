package com.example.bicicletin.com.example.bicicletin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private lateinit var usuarioEdt: EditText
    private lateinit var senhaEdt: EditText
    private lateinit var entrarBtn: Button

    private lateinit var auth: FirebaseAuth

    private lateinit var registroTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        usuarioEdt = findViewById(R.id.usuario_edt)
        senhaEdt = findViewById(R.id.senha_edt)
        entrarBtn = findViewById(R.id.entrar_btn)
        registroTxt = findViewById(R.id.registrar_txt)

        entrarBtn.setOnClickListener {
            val email = usuarioEdt.text.toString().trim()
            val senha = senhaEdt.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT)
                                .show()
                            // TODO: Mudar para a tela principal/home do app
                        } else {
                            Toast.makeText(
                                this,
                                "Erro ao entrar: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Insira e-mail e senha", Toast.LENGTH_SHORT).show()
            }

            registroTxt.setOnClickListener {
                // Se for abrir uma nova Activity de cadastro:
                val intent = Intent(this, CadastroActivity::class.java)
                startActivity(intent)
            }

        }
    }
}
package com.example.bicicletin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.perfil.PerfilActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var usuarioEdt: EditText
    private lateinit var senhaEdt: EditText
    private lateinit var entrarBtn: Button
    private lateinit var registrarBtn: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        usuarioEdt = findViewById(R.id.usuario_edt)
        senhaEdt = findViewById(R.id.senha_edt)
        entrarBtn = findViewById(R.id.entrar_btn)
        registrarBtn = findViewById(R.id.registrar_btn)

        entrarBtn.setOnClickListener {
            val email = usuarioEdt.text.toString().trim()
            val senha = senhaEdt.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show()

                            // Abre a tela de Perfil após o login
                            val intent = Intent(this, AluguelActivity::class.java)
                            startActivity(intent)
                            finish() // Fecha a tela de login
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
        }

        // Clique para abrir a tela de cadastro
        registrarBtn.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }
}
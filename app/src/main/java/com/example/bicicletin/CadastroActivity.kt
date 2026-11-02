package com.example.bicicletin.com.example.bicicletin

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var nomeEdt: EditText
    private lateinit var emailEdt: EditText
    private lateinit var senhaEdt: EditText
    private lateinit var finalizarBtn: Button
    private lateinit var escolherFotoBtn: Button
    private lateinit var voltarLoginBtn: Button

    // Lançador para abrir a galeria do celular
    private val selecionarImagemLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Log.i("Imagem", "Uri da imagem selecionada: $uri")
            // TODO: Aqui você pode exibir a imagem num ImageView se quiser
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        auth = FirebaseAuth.getInstance()

        nomeEdt = findViewById(R.id.nome_edt)
        emailEdt = findViewById(R.id.email_cadastro_edt)
        senhaEdt = findViewById(R.id.senha_cadastro_edt)
        finalizarBtn = findViewById(R.id.finalizarCadastroBtn)
        escolherFotoBtn = findViewById(R.id.escolherFotoBtn)
        voltarLoginBtn = findViewById(R.id.voltarLoginBtn)

        // Botão para escolher imagem
        escolherFotoBtn.setOnClickListener {
            // Abre a galeria filtrando por imagens
            selecionarImagemLauncher.launch("image/*")
        }

        // Botão de voltar para a tela de login (fecha a Activity atual)
        voltarLoginBtn.setOnClickListener {
            finish()
        }

        finalizarBtn.setOnClickListener {
            val email = emailEdt.text.toString().trim()
            val senha = senhaEdt.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            finish() // Volta para o login
                        } else {
                            Toast.makeText(this, "Erro no cadastro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
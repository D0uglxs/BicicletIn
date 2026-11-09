package com.example.bicicletin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var nomeEdt: EditText
    private lateinit var cpfEdt: EditText
    private lateinit var emailEdt: EditText
    private lateinit var senhaEdt: EditText
    private lateinit var finalizarBtn: Button
    private lateinit var voltarLoginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        nomeEdt = findViewById(R.id.nome_edt)
        cpfEdt = findViewById(R.id.cpf_edt)
        emailEdt = findViewById(R.id.email_cadastro_edt)
        senhaEdt = findViewById(R.id.senha_cadastro_edt)
        finalizarBtn = findViewById(R.id.finalizarCadastroBtn)
        voltarLoginBtn = findViewById(R.id.voltarLoginBtn)

        voltarLoginBtn.setOnClickListener { finish() }

        finalizarBtn.setOnClickListener {
            val nome = nomeEdt.text.toString().trim()
            val cpf = cpfEdt.text.toString().trim()
            val email = emailEdt.text.toString().trim()
            val senha = senhaEdt.text.toString().trim()

            if (nome.isNotEmpty() && cpf.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                val userMap = mapOf(
                                    "nome" to nome,
                                    "cpf" to cpf,
                                    "email" to email
                                )
                                FirebaseDatabase.getInstance().getReference("Usuarios")
                                    .child(uid)
                                    .setValue(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
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
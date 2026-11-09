package com.example.bicicletin.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PerfilActivity : AppCompatActivity() {

    private lateinit var uid: String
    private lateinit var imgFoto: ImageView
    private lateinit var txtNome: TextView
    private lateinit var txtCpf: TextView
    private lateinit var btnVoltarAluguel: Button

    private val escolherFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            if (PerfilStorage.salvarFoto(this, uid, uri)) {
                mostrarFoto()
            } else {
                Toast.makeText(this, "Não foi possível usar essa imagem.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        supportActionBar?.hide()

        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Toast.makeText(this, "Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        uid = usuario.uid

        imgFoto = findViewById(R.id.imgFoto)
        txtNome = findViewById(R.id.txtNome)
        txtCpf = findViewById(R.id.txtCpf)
        btnVoltarAluguel = findViewById(R.id.btnVoltarAluguel)

        // Busca Nome e CPF salvos no Realtime Database
        carregarDadosUsuario()

        // Foto de Perfil Local
        mostrarFoto()
        findViewById<View>(R.id.containerFoto).setOnClickListener {
            escolherFoto.launch("image/*")
        }

        // Programação do Botão Comum de Voltar para o Aluguel
        btnVoltarAluguel.setOnClickListener {
            finish() // Fecha a tela de Perfil e retorna para a tela de Aluguel
        }

        // Navegação para as sub-telas
        findViewById<View>(R.id.itemDadosPessoais).setOnClickListener {
            startActivity(Intent(this, DadosPessoaisActivity::class.java))
        }
        findViewById<View>(R.id.itemPagamento).setOnClickListener {
            startActivity(Intent(this, PagamentoActivity::class.java))
        }
        findViewById<View>(R.id.txtVerRotas).setOnClickListener {
            Toast.makeText(this, "Mapa indisponível no momento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza as estatísticas de KM, Bikes e Tempo direto do Firebase sempre que a tela abrir
        carregarAtividadesDoFirebase()
    }

    private fun carregarDadosUsuario() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isFinishing || isDestroyed) return

                if (snapshot.exists()) {
                    val nome = snapshot.child("nome").getValue(String::class.java)
                    val cpf = snapshot.child("cpf").getValue(String::class.java)

                    txtNome.text = nome ?: "Nome não informado"
                    txtCpf.text = "CPF: ${cpf ?: "não informado"}"
                } else {
                    txtNome.text = "Usuário"
                    txtCpf.text = "CPF: não informado"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun carregarAtividadesDoFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("atividade")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isFinishing || isDestroyed) return

                val km = snapshot.child("km").getValue(Double::class.java) ?: 0.0
                val bikes = snapshot.child("bikesAlugadas").getValue(Int::class.java) ?: 0
                val minutos = snapshot.child("minutosAlugados").getValue(Int::class.java) ?: 0

                findViewById<TextView>(R.id.txtKm).text = String.format("%.1f km", km)
                findViewById<TextView>(R.id.txtBikes).text = bikes.toString()
                findViewById<TextView>(R.id.txtTempo).text = "${minutos / 60} h ${minutos % 60} min"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarFoto() {
        val foto = PerfilStorage.lerFoto(this, uid)
        if (foto != null) imgFoto.setImageBitmap(foto)
        else imgFoto.setImageResource(R.drawable.perfil_avatar_padrao)
    }
}
package com.example.bicicletin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.perfil.PerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AluguelActivity : AppCompatActivity() {

    private lateinit var spinnerEstacoes: Spinner
    private lateinit var edtCodigoBike: EditText
    private lateinit var btnDesbloquear: Button
    private lateinit var cardAluguelAtivo: View
    private lateinit var txtTimer: TextView
    private lateinit var btnEncerrar: Button

    private var segundosDecorridos = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnableTimer = object : Runnable {
        override fun run() {
            segundosDecorridos++
            val hrs = segundosDecorridos / 3600
            val mins = (segundosDecorridos % 3600) / 60
            val secs = segundosDecorridos % 60
            txtTimer.text = String.format("%02d:%02d:%02d", hrs, mins, secs)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aluguel)
        supportActionBar?.hide()

        // Botão para ir ao Perfil
        findViewById<View>(R.id.btnIrPerfil).setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        spinnerEstacoes = findViewById(R.id.spinnerEstacoes)
        edtCodigoBike = findViewById(R.id.edtCodigoBike)
        btnDesbloquear = findViewById(R.id.btnDesbloquear)
        cardAluguelAtivo = findViewById(R.id.cardAluguelAtivo)
        txtTimer = findViewById(R.id.txtTimer)
        btnEncerrar = findViewById(R.id.btnEncerrarAluguel)

        val estacoes = listOf(
            "Estação 01 - Praça Central",
            "Estação 02 - Campus Universitário",
            "Estação 03 - Parque das Bicicletas",
            "Estação 04 - Terminal Rodoviário"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estacoes)
        spinnerEstacoes.adapter = adapter

        btnDesbloquear.setOnClickListener {
            val codigo = edtCodigoBike.text.toString().trim()
            if (codigo.isEmpty()) {
                edtCodigoBike.error = "Digite o código da bike"
                return@setOnClickListener
            }

            cardAluguelAtivo.visibility = View.VISIBLE
            btnDesbloquear.isEnabled = false
            segundosDecorridos = 0
            handler.post(runnableTimer)

            Toast.makeText(this, "Bike $codigo liberada!", Toast.LENGTH_SHORT).show()
        }

        btnEncerrar.setOnClickListener {
            btnEncerrar.isEnabled = false
            handler.removeCallbacks(runnableTimer)
            cardAluguelAtivo.visibility = View.GONE
            btnDesbloquear.isEnabled = true

            val codigoBike = edtCodigoBike.text.toString().trim()
            edtCodigoBike.text.clear()

            val kmRodados = (segundosDecorridos / 10.0) * 0.5
            val minutosGastos = (segundosDecorridos / 60).coerceAtLeast(1)

            salvarAtividadeNoFirebase(kmRodados, minutosGastos, codigoBike)
        }
    }

    private fun salvarAtividadeNoFirebase(kmAdicional: Double, minutosAdicionais: Int, bike: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid).child("atividade")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isFinishing || isDestroyed) return

                val kmAtual = snapshot.child("km").getValue(Double::class.java) ?: 0.0
                val bikesAtual = snapshot.child("bikesAlugadas").getValue(Int::class.java) ?: 0
                val minutosAtual = snapshot.child("minutosAlugados").getValue(Int::class.java) ?: 0

                val novosDados = mapOf(
                    "km" to (kmAtual + kmAdicional),
                    "bikesAlugadas" to (bikesAtual + 1),
                    "minutosAlugados" to (minutosAtual + minutosAdicionais),
                    "ultimaBike" to bike
                )

                ref.setValue(novosDados).addOnSuccessListener {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@AluguelActivity, "Aluguel encerrado e salvo na base!", Toast.LENGTH_LONG).show()
                        btnEncerrar.isEnabled = true
                    }
                }.addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        btnEncerrar.isEnabled = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isFinishing && !isDestroyed) {
                    btnEncerrar.isEnabled = true
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnableTimer)
    }
}
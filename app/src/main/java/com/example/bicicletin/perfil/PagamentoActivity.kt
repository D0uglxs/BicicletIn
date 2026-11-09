package com.example.bicicletin.perfil

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth

/** Mostra o cartão cadastrado e deixa cadastrar/trocar o cartão. */
class PagamentoActivity : AppCompatActivity() {

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)
        supportActionBar?.hide()

        findViewById<View>(R.id.headerSub).aplicarInsetTopo()
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            finish()
            return
        }
        uid = usuario.uid

        findViewById<View>(R.id.btnCartao).setOnClickListener { abrirDialogCartao() }
        mostrarCartao()
    }

    private fun mostrarCartao() {
        val cartao = PerfilStorage.lerCartao(this, uid)
        val card = findViewById<View>(R.id.cardCartao)
        val semCartao = findViewById<View>(R.id.txtSemCartao)
        val botao = findViewById<TextView>(R.id.btnCartao)

        if (cartao == null) {
            card.visibility = View.GONE
            semCartao.visibility = View.VISIBLE
            botao.text = "Cadastrar cartão"
        } else {
            card.visibility = View.VISIBLE
            semCartao.visibility = View.GONE
            botao.text = "Trocar cartão"
            findViewById<TextView>(R.id.txtBandeira).text = cartao.bandeira
            findViewById<TextView>(R.id.txtNumeroCartao).text = "•••• •••• •••• ${cartao.ultimos4}"
            findViewById<TextView>(R.id.txtTitular).text = cartao.titular
            findViewById<TextView>(R.id.txtValidade).text = cartao.validade
        }
    }

    private fun abrirDialogCartao() {
        val conteudo = layoutInflater.inflate(R.layout.dialog_cartao, null)
        val edtTitular = conteudo.findViewById<EditText>(R.id.edtTitular)
        val edtNumero = conteudo.findViewById<EditText>(R.id.edtNumero)
        val edtValidade = conteudo.findViewById<EditText>(R.id.edtValidade)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Dados do cartão")
            .setView(conteudo)
            .setPositiveButton("Salvar", null) // o clique é definido abaixo (para validar antes de fechar)
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val titular = edtTitular.text.toString().trim()
            val numero = edtNumero.text.toString().filter { it.isDigit() }
            val validade = edtValidade.text.toString().trim()

            when {
                titular.isEmpty() -> {
                    edtTitular.error = "Informe o nome"
                }
                numero.length < 13 -> {
                    edtNumero.error = "Número inválido"
                }
                !Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(validade) -> {
                    edtValidade.error = "Use o formato MM/AA"
                }
                else -> {
                    // Guardamos só os 4 últimos dígitos. Nunca o número inteiro nem o CVV.
                    val bandeira = when (numero.first()) {
                        '4' -> "VISA"
                        '5', '2' -> "MASTERCARD"
                        else -> "CARTÃO"
                    }
                    PerfilStorage.salvarCartao(
                        this, uid, Cartao(titular, bandeira, numero.takeLast(4), validade)
                    )
                    mostrarCartao()
                    dialog.dismiss()
                    Toast.makeText(this, "Cartão salvo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

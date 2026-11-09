package com.example.bicicletin.perfil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File

/** Dados do cartão. NUNCA guardamos o número completo nem o CVV. */
data class Cartao(
    val titular: String,
    val bandeira: String,
    val ultimos4: String,
    val validade: String
)

/** Números do cartão "Minha Atividade". */
data class Atividade(val km: Double, val bikesAlugadas: Int, val minutosAlugados: Int)

/**
 * Guarda no próprio aparelho (por usuário):
 *  - a foto de perfil (arquivo na pasta interna do app)
 *  - o cartão cadastrado (SharedPreferences)
 *
 * Quando o grupo quiser sincronizar na nuvem, é só trocar o conteúdo
 * destas funções por chamadas ao Firebase (Storage / Firestore).
 */
object PerfilStorage {

    private const val PREFS = "perfil_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ------------------------------------------------------------ FOTO

    private fun arquivoFoto(context: Context, uid: String) =
        File(context.filesDir, "foto_perfil_$uid.jpg")

    /** Lê a foto escolhida em [uri], reduz (máx. 1024px), corrige a rotação e salva. */
    fun salvarFoto(context: Context, uid: String, uri: Uri): Boolean {
        return try {
            val resolver = context.contentResolver

            // 1) descobre o tamanho para não estourar a memória
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            var amostra = 1
            while (limites.outWidth / amostra > 1024 || limites.outHeight / amostra > 1024) amostra *= 2

            // 2) carrega reduzida
            val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
            var bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opcoes)
            } ?: return false

            // 3) corrige rotação (fotos tiradas com a câmera)
            val orientacao = resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            }
            val graus = when (orientacao) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (graus != 0f) {
                val matriz = Matrix().apply { postRotate(graus) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
            }

            // 4) salva
            arquivoFoto(context, uid).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Devolve a foto salva ou null se o usuário ainda não escolheu. */
    fun lerFoto(context: Context, uid: String): Bitmap? {
        val arquivo = arquivoFoto(context, uid)
        return if (arquivo.exists()) BitmapFactory.decodeFile(arquivo.absolutePath) else null
    }

    // ------------------------------------------------------------ ATIVIDADE

    /**
     * Quem fizer o histórico de aluguéis chama esta função para atualizar o perfil:
     *   PerfilStorage.salvarAtividade(context, uid, Atividade(12.5, 3, 95))
     * (Depois dá para trocar por leitura direto do banco de dados.)
     */
    fun salvarAtividade(context: Context, uid: String, atividade: Atividade) {
        prefs(context).edit()
            .putFloat("atv_km_$uid", atividade.km.toFloat())
            .putInt("atv_bikes_$uid", atividade.bikesAlugadas)
            .putInt("atv_minutos_$uid", atividade.minutosAlugados)
            .apply()
    }

    fun lerAtividade(context: Context, uid: String): Atividade {
        val p = prefs(context)
        return Atividade(
            km = p.getFloat("atv_km_$uid", 0f).toDouble(),
            bikesAlugadas = p.getInt("atv_bikes_$uid", 0),
            minutosAlugados = p.getInt("atv_minutos_$uid", 0)
        )
    }

    // ------------------------------------------------------------ CARTAO

    fun salvarCartao(context: Context, uid: String, cartao: Cartao) {
        prefs(context).edit()
            .putString("cartao_titular_$uid", cartao.titular)
            .putString("cartao_bandeira_$uid", cartao.bandeira)
            .putString("cartao_ultimos4_$uid", cartao.ultimos4)
            .putString("cartao_validade_$uid", cartao.validade)
            .apply()
    }

    fun lerCartao(context: Context, uid: String): Cartao? {
        val p = prefs(context)
        val ultimos4 = p.getString("cartao_ultimos4_$uid", null) ?: return null
        return Cartao(
            titular = p.getString("cartao_titular_$uid", "") ?: "",
            bandeira = p.getString("cartao_bandeira_$uid", "CARTÃO") ?: "CARTÃO",
            ultimos4 = ultimos4,
            validade = p.getString("cartao_validade_$uid", "") ?: ""
        )
    }
}

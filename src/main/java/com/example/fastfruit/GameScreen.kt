package com.example.fastfruit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileWriter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class GameScreen : AppCompatActivity() {

    private lateinit var imageButtons: List<ImageView>
    private lateinit var frutasEmJogo: MutableList<Int>
    private var frutaAtual: Int =0
    private var nivel: Int = 1
    private var acertos: Int = 0
    private var erros: Int = 0
    private var totalAcertos: Int = 0
    private var numeroDicas: Int = 0
    private var indiceFruta: Int = 0
    var sessaoCompleta: Boolean = false
    private lateinit var relatorio: StringBuilder
    private lateinit var horaInicio: ZonedDateTime
    private lateinit var horaFim: ZonedDateTime
    private val handler = Handler(Looper.getMainLooper())
    private var dicaRun: Runnable? = null
    private var errosConsecutivos = 0 // Contador de erros consecutivos



    private val frutas = arrayOf(
        R.drawable.apple, R.drawable.banana, R.drawable.cherry, R.drawable.coconut,
        R.drawable.grape, R.drawable.kiwi, R.drawable.lemon, R.drawable.mango,
        R.drawable.orange, R.drawable.papaya, R.drawable.pear, R.drawable.persimmon,
        R.drawable.pineapple, R.drawable.strawberry, R.drawable.tomato, R.drawable.watermelon,
        R.drawable.plum, R.drawable.sourspop, R.drawable.peach, R.drawable.guava,
        R.drawable.green_apple, R.drawable.green_grape
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startGame()
        startService(Intent(this, MusicService::class.java))
    }

    private fun startGame(){
        selecionarLayout()
        getImagesButton()
        frutasEmJogo = frutasSelecionadas().toMutableList()
        definirImagemDestaque()
        posicionarImagens()
        horaInicio = horaAtual()
        jogar()

    }

    private fun horaAtual(): ZonedDateTime{
        val datahora = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        return datahora
    }

    private fun selecionarLayout(){
        when(getNivel()){
            1 -> setContentView(R.layout.activity_game_screen_nivel_one)
            2 -> setContentView(R.layout.activity_game_screen_nivel_two)
            3 -> setContentView(R.layout.activity_game_screen_nivel_three)
        }
    }

    private fun getNivel(): Int {
        return nivel
    }

    private fun qtdFrutas():Int {
        return when(getNivel()) {
            1 -> 4
            2 -> 9
            3 -> 16
            else -> 0
        }
    }

    private fun getImagesButton(){
        imageButtons = (1..qtdFrutas()).mapNotNull {i ->
            val id = resources.getIdentifier("fruit_$i", "id", packageName)
            findViewById(id)
        }
    }

    private fun frutasSelecionadas() :List<Int> {
        return ((frutas.indices).shuffled().take(qtdFrutas())).map { frutas[it] }
    }

    private fun definirImagemDestaque() {
        val frutasDisponiveis = frutasEmJogo.filter { it != -1 }
        if (frutasDisponiveis.isNotEmpty()) {
            frutaAtual = frutasDisponiveis.random()
            trocarImagem(findViewById(R.id.fruta_destaque), frutaAtual)
        }
    }


    private fun trocarImagem(imagemAtual: ImageView, idNovaImagem: Int ) {
        imagemAtual.setImageResource(idNovaImagem)
    }

    private fun posicionarImagens() {
        getImagesButton()
        imageButtons.forEachIndexed { index, image ->
            val img = frutasEmJogo
            trocarImagem(image, img[index])
        }
    }

    private fun removerImagem(image: ImageView, index: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            if(index in frutasEmJogo.indices){
                frutasEmJogo[index] = -1
                image.visibility = View.INVISIBLE
                image.isEnabled = false

                definirImagemDestaque()
            }
        }, 100)
    }

    private fun verificarNivel(){
        if (acertos == 4){
            nivel++
            if(nivel == 4) {
                sessaoCompleta = true
                finalizarJogo()
            }
            acertos = 0
            resetarJogo()
        }
    }

    private fun resetarJogo(){
        frutasEmJogo = frutasSelecionadas().toMutableList()
        acertos = 0
        definirImagemDestaque()
        selecionarLayout()
        imageButtons
        posicionarImagens()
        jogar()
    }

    private fun acerto(){
        totalAcertos++
        acertos++
        errosConsecutivos = 0

        val sharedPref = getSharedPreferences("AppSettings", 0)
        if (!sharedPref.getBoolean("sounds", true)) return

        val soundCorrect = MediaPlayer.create(this, R.raw.sound_correct)
        soundCorrect?.setOnCompletionListener { it.release() }
        soundCorrect?.start()

    }

    private fun mostrarDica() {
        val index = frutasEmJogo.indexOf(frutaAtual)

        if (index in imageButtons.indices) {
            piscarImagem(imageButtons[index]) // Destaca a fruta correta
            numeroDicas++
        }
    }

    private fun erro(){
        erros++
        errosConsecutivos++
        val sharedPref = getSharedPreferences("AppSettings", 0)
        if (!sharedPref.getBoolean("sounds", true)) return

        val soundError = MediaPlayer.create(this, R.raw.sound_error)
        soundError?.setOnCompletionListener { it.release() }
        soundError?.start()
        if (errosConsecutivos == 3){
            mostrarDica()
            errosConsecutivos = 0

        }
    }

    private fun tempoDeJogo(): Duration{
        return Duration.between(horaInicio, horaFim)
    }

    private fun finalizarJogo(){
        horaFim = horaAtual()

        val dialog = FinishScreen().apply {
            arguments = Bundle().apply {
                putString("horaInicio", horaInicio.toString())
                putString("horaFim", horaFim.toString())
                putString("tempoJogo", tempoDeJogo().seconds.toString())
                putInt("totalAcertos", totalAcertos)
                putInt("erros", erros)
                putInt("numeroDicas", numeroDicas)
                putBoolean("sessaoCompleta", sessaoCompleta)
            }
        }
        dialog.show(supportFragmentManager, "SalvarRelatorioDialog")

    }

    // Adicionar verificação de finalizar sessão
    // Adicionar algo para destacar mais a fruta escolhida

    override fun onPause() {
        super.onPause()
        if (isFinishing) { // Only show if the activity is really finishing
            finalizarJogo()
        }
            stopService(Intent(this, MusicService::class.java))
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this,MusicService::class.java))
    }


    private fun piscarImagem(imageView: ImageView) {
        val animator = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f, 1f)
        animator.duration = 1000 // Duração de cada repetição
        animator.repeatCount = 2 // Número de repetições (0 conta)
        animator.repeatMode = ValueAnimator.RESTART // Modo como pisca
        animator.start()
    }

    private fun temporizadorDica(){
        var tempoDica = 10000L
        val tempoMin = 5000L
        dicaRun?.let { handler.removeCallbacks(it) }

        dicaRun = Runnable {
            val index = frutasEmJogo.indexOf(frutaAtual)

            if (index in imageButtons.indices) {
                piscarImagem(imageButtons[index])
                numeroDicas++
            }

            tempoDica = maxOf(tempoDica - 1000, tempoMin)
            temporizadorDica()
        }
        handler.postDelayed(dicaRun!!, tempoDica)
    }

    private fun pararTemporizador(){
        dicaRun?.let { handler.removeCallbacks(it)}
    }

    private fun piscarColorido(imageView: ImageView, acerto: Boolean){
        val borderDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            if(acerto) {
                setStroke(8, Color.GREEN)
            } else {
                setStroke(8, Color.RED)
            }

            cornerRadius = 20f
        }


        val animator = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 1f, 1f).apply {
            duration = 200
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                imageView.background = borderDrawable
            }

            override fun onAnimationEnd(animation: Animator) {
                imageView.background = null

            }
        })

        animator.start()
    }

    private fun jogar() {
        val aux = frutasEmJogo.toList()
        temporizadorDica()
        imageButtons.forEachIndexed { index, image ->
            image.setOnClickListener{
                pararTemporizador()
                if (frutaAtual == aux[index]) {
                    acerto()
                    piscarColorido(image, true)
                    verificarNivel()
                    removerImagem(image, index)
                }
                else {
                    erro()
                    piscarColorido(image, false)
                }

                temporizadorDica()
            }
        }
    }



}
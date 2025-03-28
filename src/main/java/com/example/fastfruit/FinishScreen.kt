package com.example.fastfruit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.fastfruit.databinding.FirstDialogFragmentBinding
import com.example.fastfruit.databinding.SecondDialogFragmentBinding
import java.io.OutputStream

class FinishScreen : DialogFragment(){

    private lateinit var binding: SecondDialogFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SecondDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.saveButton.setOnClickListener {
            val uriString = loadPreferencesString("arquivoUri")

            if (uriString == null) {
                // Criar um novo arquivo se ainda não existir
                createFileLauncher.launch("Relatorio Tapa Certo.csv")
            } else {
                // Adicionar nova linha ao arquivo existente
                adicionarNovaLinha(uriString)
            }
        }
    }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) {
                savePreferences("arquivoUri", uri.toString()) // Salvar a URI do arquivo criado
                salvarRelatorio(uri.toString(), true) // Criar e escrever o cabeçalho no arquivo
            } else {
                Toast.makeText(requireContext(), "Erro ao criar o arquivo.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun salvarRelatorio(uriString: String, novoArquivo: Boolean) {
        val uri = Uri.parse(uriString)

        requireContext().contentResolver.openOutputStream(uri, if (novoArquivo) "w" else "wa")?.use { outputStream ->
            try {
                outputStream.writer().use { writer ->
                    if (novoArquivo) {
                        // Cabeçalho do CSV (apenas se for um novo arquivo)
                        writer.appendLine("Hora_Inicio,Hora_fim,Tempo_de_Jogo,Total_Acertos,Erros,Numero_de_Dicas,Sessao_Completa,")
                    }
                    // Nova linha com os dados do jogo
                    val horaInicio = arguments?.getString("horaInicio") ?: "0"
                    val horaFim = arguments?.getString("horaFim") ?: "0"
                    val tempoJogo = arguments?.getString("tempoJogo") ?: "0"
                    val totalAcertos = arguments?.getInt("totalAcertos") ?: 0
                    val erros = arguments?.getInt("erros") ?: 0
                    val numeroDicas = arguments?.getInt("numeroDicas") ?: 0
                    val sessaoCompleta = arguments?.getBoolean("sessaoCompleta") ?: false
                    writer.appendLine("$horaInicio,$horaFim,$tempoJogo,$totalAcertos,$erros,$numeroDicas,$sessaoCompleta")
                }
                Toast.makeText(requireContext(), "Relatório salvo com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), HomeScreen::class.java))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun adicionarNovaLinha(uriString: String) {
        salvarRelatorio(uriString, false) // Apenas adiciona uma nova linha ao arquivo existente
    }

    private fun savePreferences(key: String, value: String) {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", 0)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    private fun loadPreferencesString(key: String): String? {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", 0)
        return sharedPref.getString(key, null)
    }
}

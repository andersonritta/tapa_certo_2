package com.example.fastfruit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.fastfruit.databinding.FirstDialogFragmentBinding

class ConfigWindow : DialogFragment() {

    private lateinit var binding: FirstDialogFragmentBinding
    var music = true
    var sounds = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FirstDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        loadPreferences() // Carregar as preferências ao iniciar o diálogo

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.musicButton.setOnClickListener {
            music = !music
            val musicIcon = if (music) R.drawable.home_music else R.drawable.home_music_off
            binding.musicButton.setImageResource(musicIcon)
            savePreferences("music", music)

            if (music) {
                // Iniciar o serviço de música
                requireActivity().startService(Intent(requireContext(), MusicService::class.java))
            } else {
                // Parar o serviço de música
                requireActivity().stopService(Intent(requireContext(), MusicService::class.java))
            }
        }

        binding.soundButton.setOnClickListener {
            sounds = !sounds
            val soundsIcon = if (sounds) R.drawable.home_volume_on else R.drawable.home_volume_off
            binding.soundButton.setImageResource(soundsIcon)
            savePreferences("sounds", sounds)
        }
    }

    private fun savePreferences(key: String, value: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", 0)
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    private fun loadPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", 0)
        music = sharedPref.getBoolean("music", true)
        sounds = sharedPref.getBoolean("sounds", true)

        // Atualiza o ícone com base nas preferências carregadas
        binding.musicButton.setImageResource(if (music) R.drawable.home_music else R.drawable.home_music_off)
        binding.soundButton.setImageResource(if (sounds) R.drawable.home_volume_on else R.drawable.home_volume_off)
    }
}
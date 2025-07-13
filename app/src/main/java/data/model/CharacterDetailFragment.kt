package com.example.testrickandmorty.ui.characterdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.databinding.FragmentCharacterDetailBinding
import kotlinx.coroutines.launch

class CharacterDetailFragment : Fragment() {

    private var _binding: FragmentCharacterDetailBinding? = null
    private val binding get() = _binding!!


    private val args by navArgs<CharacterDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = args.characterId
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Rick and Morty"


        lifecycleScope.launch {
            try {
                val character = RetrofitInstance.api.getCharacterById(id)

                // Фото
                Glide.with(requireContext())
                    .load(character.image)
                    .into(binding.imageView)

                // Имя
                binding.nameTextView.text = character.name

                // Основная инфа
                binding.statusTextView.text = "Status: ${character.status}"
                binding.speciesTextView.text = "Species: ${character.species}"
                binding.typeTextView.text = "Type: ${character.type.ifBlank { "N/A" }}"
                binding.genderTextView.text = "Gender: ${character.gender}"

                // Локации
                binding.originTextView.text = "Origin: ${character.origin.name}"
                binding.locationTextView.text = "Location: ${character.location.name}"

                // Эпизоды
                binding.episodeCountTextView.text = "Episodes: ${character.episode.size}"

                // Ссылка
                //binding.urlTextView.text = "API URL: ${character.url}"

                // Дата создания
                val formattedDate = character.created.substringBefore("T")
                binding.createdTextView.text = "Created: $formattedDate"

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

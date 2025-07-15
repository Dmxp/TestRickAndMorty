package com.example.testrickandmorty.ui.characterdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.model.Episode
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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Rick and Morty"

        val characterId = args.characterId
        binding.detailProgressBar.visibility = View.VISIBLE
        binding.contentScroll.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val character = RetrofitInstance.api.getCharacterById(characterId)

                // Основная информация
                Glide.with(requireContext())
                    .load(character.image)
                    .into(binding.imageView)

                binding.nameTextView.text = character.name
                binding.statusTextView.text = "Status: ${character.status}"
                binding.speciesTextView.text = "Species: ${character.species}"
                binding.typeTextView.text = "Type: ${character.type.ifBlank { "N/A" }}"
                binding.genderTextView.text = "Gender: ${character.gender}"
                binding.originTextView.text = "Origin: ${character.origin.name}"
                binding.locationTextView.text = "Location: ${character.location.name}"
                binding.episodeCountTextView.text = "Episodes: ${character.episode.size}"

                val formattedDate = character.created.substringBefore("T")
                binding.createdTextView.text = "Created: $formattedDate"

                // Подгрузка эпизодов
                val episodeIds = character.episode.mapNotNull {
                    it.substringAfterLast("/").toIntOrNull()
                }

                if (episodeIds.isNotEmpty()) {
                    val idsParam = episodeIds.joinToString(",")
                    val episodes: List<Episode> = if (episodeIds.size == 1) {
                        // Один эпизод
                        listOf(RetrofitInstance.api.getSingleEpisode(episodeIds.first()))
                    } else {
                        // Несколько эпизодов
                        RetrofitInstance.api.getMultipleEpisodes(episodeIds.joinToString(","))
                    }
                    var isExpanded = false
                    binding.showEpisodesButton.setOnClickListener {
                        isExpanded = !isExpanded
                        binding.episodeListLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                        binding.showEpisodesButton.text = if (isExpanded) "Hide episodes" else "Show episodes"
                    }

                    episodes.forEach { episode ->
                        val tv = TextView(requireContext()).apply {
                            text = "${episode.episode}: ${episode.name}"
                            setPadding(0, 8, 0, 8)
                            setTextColor(resources.getColor(android.R.color.black, null))
                        }
                        binding.episodeListLayout.addView(tv)
                    }
                }

            } catch (e: Exception) {
                if (isAdded && _binding != null) {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (isAdded && _binding != null) {
                    binding.detailProgressBar.visibility = View.GONE
                    binding.contentScroll.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

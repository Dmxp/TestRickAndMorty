package com.example.testrickandmorty.ui.characterlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.databinding.FragmentCharacterListBinding
import kotlinx.coroutines.launch

class CharacterListFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!

    private val characters = mutableListOf<CharacterModel>()
    private val allCharacters = mutableListOf<CharacterModel>()
    private lateinit var adapter: CharacterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CharacterAdapter(characters) { character ->
            Toast.makeText(requireContext(), "Вы выбрали: ${character.name}", Toast.LENGTH_SHORT).show()
            val action = CharacterListFragmentDirections
                .actionCharacterListFragmentToCharacterDetailFragment(character.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadCharacters()
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCharacters(newText)
                return true
            }
        })

        loadCharacters()
    }

    private fun loadCharacters() {
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getCharacters(1)
                allCharacters.clear()
                allCharacters.addAll(response.results)

                characters.clear()
                characters.addAll(response.results)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun filterCharacters(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allCharacters
        } else {
            allCharacters.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }
        characters.clear()
        characters.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
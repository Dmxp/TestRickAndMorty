package com.example.testrickandmorty.ui.characterlist

import CharacterListViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.databinding.FragmentCharacterListBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CharacterListFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharacterAdapter

    // ViewModel с контекстом Application
    private val viewModel: CharacterListViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CharacterAdapter(mutableListOf()) { character ->
            val action = CharacterListFragmentDirections
                .actionCharacterListFragmentToCharacterDetailFragment(character.id)
            findNavController().navigate(action)
        }
//        viewModel.initialLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.swipeRefreshLayout.isRefreshing = isLoading
//        }

        viewModel.initialLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.initialProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        // Пагинация
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                val isBottom = firstVisibleItem + visibleItemCount >= totalItemCount
                if (isBottom) viewModel.loadNextPage()
            }
        })

        // Swipe-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadInitialData()
        }

        // Поиск с debounce
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = MainScope().launch {
                    delay(300)
                    viewModel.applyFilter(newText ?: "")
                }
                return true
            }
        })

        // Восстановить текст поиска
        binding.searchView.setQuery(viewModel.searchQuery.value ?: "", false)

        // Наблюдение за данными
        viewModel.characters.observe(viewLifecycleOwner, Observer { list ->
            adapter.updateData(list)
        })

        // Показывать / скрывать индикатор
        viewModel.characters.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

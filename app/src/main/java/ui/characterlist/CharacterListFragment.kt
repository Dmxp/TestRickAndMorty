package com.example.testrickandmorty.ui.characterlist

import CharacterListViewModel
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickandmorty.data.model.CharacterFilter
import com.example.testrickandmorty.databinding.FragmentCharacterListBinding
import data.model.CharacterFilterBottomSheet

class CharacterListFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!

    private var recyclerViewState: Parcelable? = null

    private lateinit var adapter: CharacterAdapter

    private val viewModel: CharacterListViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Адаптер
        adapter = CharacterAdapter(mutableListOf()) { character ->
            val action = CharacterListFragmentDirections
                .actionCharacterListFragmentToCharacterDetailFragment(character.id)
            findNavController().navigate(action)
        }
        // RecyclerView
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        // Наблюдение над действиями
        viewModel.showEmptyState.observe(viewLifecycleOwner) { show ->
            binding.emptyTextView.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Прогрессбар при первом запуске
        viewModel.initialLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.initialProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Пагинация
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    viewModel.loadNextPage()
                }
            }
        })

        // Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadInitialData()
        }

        // Поиск
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.applyFilter(newText ?: "")
                return true
            }
        })

        // Восстановление текста поиска
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            binding.searchView.setQuery(query, false)
        }

        // Ожидание списка персонажей
        viewModel.characters.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.swipeRefreshLayout.isRefreshing = false

            recyclerViewState?.let {
                binding.recyclerView.layoutManager?.onRestoreInstanceState(it)
                recyclerViewState = null
            }
        }


        // Кнопка фильтра
        binding.filterButton.setOnClickListener {
            CharacterFilterBottomSheet(
                initialName = viewModel.lastFilterName,
                initialStatus = viewModel.lastFilterStatus,
                initialGender = viewModel.lastFilterGender,
                initialSpecies = viewModel.lastFilterSpecies,
                initialType = viewModel.lastFilterType
            ) { name, status, gender, species, type ->
                viewModel.applyFilterWithApi(
                    CharacterFilter(name, status, gender, species, type)
                )
            }.show(parentFragmentManager, "CharacterFilter")
        }

        viewModel.showEmptyState.observe(viewLifecycleOwner) { showEmpty ->
            binding.emptyTextView.visibility = if (showEmpty) View.VISIBLE else View.GONE
        }

        // Загрузка данных при первом запуске
        viewModel.loadInitialData()
    }
    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

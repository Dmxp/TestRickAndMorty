import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.testrickandmorty.data.CharacterRepository
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.model.CharacterFilter
import com.example.testrickandmorty.data.model.CharacterModel
import kotlinx.coroutines.launch

class CharacterListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CharacterRepository(application)

    private val _characters = MutableLiveData<List<CharacterModel>>(emptyList())
    val characters: LiveData<List<CharacterModel>> = _characters

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _initialLoading = MutableLiveData<Boolean>()
    val initialLoading: LiveData<Boolean> = _initialLoading

    private var allCharacters = mutableListOf<CharacterModel>()
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false

    private var currentFilter: CharacterFilter? = null

    private val _showEmptyState = MutableLiveData(false)
    val showEmptyState: LiveData<Boolean> = _showEmptyState

    private var isFilterOrSearchActive = false

    var allowPagination: Boolean = true

    private var isDataLoaded = false




    // Сохранение состояния фильтров
    var lastFilterName: String = ""
    var lastFilterStatus: String = ""
    var lastFilterGender: String = ""
    var lastFilterSpecies: String = ""
    var lastFilterType: String = ""

    init {
        loadInitialData()
    }

    fun loadNextPage() {
        if (!allowPagination || isLoading || isLastPage) return
        isLoading = true

        viewModelScope.launch {
            try {
                val newData = repository.getCharactersFromApi(currentPage, currentFilter)
                allCharacters.addAll(newData)
                currentPage++
                isLastPage = newData.isEmpty()

                val result = if (_searchQuery.value.isNullOrBlank()) {
                    allCharacters
                } else {
                    allCharacters.filter {
                        it.name.contains(_searchQuery.value!!.trim(), ignoreCase = true)
                    }
                }
                _characters.postValue(result)
            } catch (_: Exception) {
                // можно оставить кэш
            } finally {
                isLoading = false
            }
        }
    }


    fun applyFilter(query: String) {
        _searchQuery.value = query
        isFilterOrSearchActive = query.isNotBlank() || currentFilter != null

        currentPage = 1
        isLastPage = false

        // Подготовка фильтра, объединяющего текущее состояние
        val updatedFilter = CharacterFilter(
            name = query.ifBlank { null },
            status = currentFilter?.status,
            gender = currentFilter?.gender,
            species = currentFilter?.species,
            type = currentFilter?.type
        )

        currentFilter = updatedFilter // сохраняем как основной фильтр

        allCharacters.clear() // очищаем перед новым запросом

        viewModelScope.launch {
            _initialLoading.postValue(true)
            try {
                val result = repository.getCharactersFromApi(currentPage, updatedFilter)

                // Удаляем дубликаты по ID (можно по name, но id точнее)
                val unique = result.distinctBy { it.id }

                allCharacters.addAll(unique)
                _characters.postValue(unique)
                _showEmptyState.postValue(unique.isEmpty() && isFilterOrSearchActive)

                currentPage++
            } catch (e: Exception) {
                val cached = repository.getCharactersFromCache()

                val filtered = cached.filter {
                    (updatedFilter.name == null || it.name.contains(updatedFilter.name, ignoreCase = true)) &&
                            (updatedFilter.status == null || it.status.equals(updatedFilter.status, ignoreCase = true)) &&
                            (updatedFilter.gender == null || it.gender.equals(updatedFilter.gender, ignoreCase = true)) &&
                            (updatedFilter.species == null || it.species.contains(updatedFilter.species, ignoreCase = true)) &&
                            (updatedFilter.type == null || it.type.contains(updatedFilter.type, ignoreCase = true))
                }

                val uniqueCached = filtered.distinctBy { it.id }

                allCharacters.addAll(uniqueCached)
                _characters.postValue(uniqueCached)
                _showEmptyState.postValue(uniqueCached.isEmpty() && isFilterOrSearchActive)
            } finally {
                _initialLoading.postValue(false)
            }
        }
    }




    fun loadInitialData() {
        if (isDataLoaded) return

        _initialLoading.postValue(true)
        viewModelScope.launch {
            val cached = repository.getCharactersFromCache()
            if (cached.isNotEmpty()) {
                allCharacters.clear()
                allCharacters.addAll(cached)
                applyFilter(_searchQuery.value ?: "")
            }
            loadNextPage()
            _initialLoading.postValue(false)
            isDataLoaded = true
        }
    }


    fun applyFilterWithApi(filter: CharacterFilter?) {
        currentFilter = filter
        isFilterOrSearchActive = filter != null && listOf(
            filter.name, filter.status, filter.gender, filter.species, filter.type
        ).any { !it.isNullOrBlank() }

        // сохранение состояния фильтра (как было у тебя ранее)
        lastFilterName = filter?.name ?: ""
        lastFilterStatus = filter?.status ?: ""
        lastFilterGender = filter?.gender ?: ""
        lastFilterSpecies = filter?.species ?: ""
        lastFilterType = filter?.type ?: ""

        currentPage = 1
        isLastPage = false
        allCharacters.clear()

        viewModelScope.launch {
            _initialLoading.postValue(true)
            try {
                val result = repository.getCharactersFromApi(currentPage, filter)
                allCharacters.addAll(result)
                _characters.postValue(result)
                _showEmptyState.postValue(result.isEmpty() && isFilterOrSearchActive)
                currentPage++
            } catch (e: Exception) {
                val cached = repository.getCharactersFromCache()

                val filtered = cached.filter {
                    (filter?.name == null || it.name.contains(filter.name, ignoreCase = true)) &&
                            (filter?.status == null || it.status.equals(filter.status, ignoreCase = true)) &&
                            (filter?.gender == null || it.gender.equals(filter.gender, ignoreCase = true)) &&
                            (filter?.species == null || it.species.contains(filter.species, ignoreCase = true)) &&
                            (filter?.type == null || it.type.contains(filter.type, ignoreCase = true))
                }

                allCharacters.addAll(filtered)
                _characters.postValue(filtered)
                _showEmptyState.postValue(filtered.isEmpty() && isFilterOrSearchActive)
            } finally {
                _initialLoading.postValue(false)
            }
        }
    }
}

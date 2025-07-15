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
        isFilterOrSearchActive = query.isNotBlank()

        val result = if (query.isBlank()) {
            allCharacters
        } else {
            allCharacters.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }

        _characters.value = result
        _showEmptyState.value = result.isEmpty() && isFilterOrSearchActive
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

        // сохранение состояния фильтра
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

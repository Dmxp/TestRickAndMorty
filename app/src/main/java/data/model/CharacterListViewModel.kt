import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickandmorty.data.CharacterRepository
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.model.CharacterModel
import kotlinx.coroutines.launch

class CharacterListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CharacterRepository(application)

    private val _characters = MutableLiveData<List<CharacterModel>>(emptyList())
    val characters: LiveData<List<CharacterModel>> = _characters

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private var allCharacters = mutableListOf<CharacterModel>()
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false

    private val _initialLoading = MutableLiveData<Boolean>()
    val initialLoading: LiveData<Boolean> = _initialLoading

    init {
        loadInitialData()
    }



    fun loadNextPage() {
        if (isLoading || isLastPage) return
        isLoading = true

        viewModelScope.launch {
            try {
                val newData = repository.getCharactersFromApi(currentPage)
                allCharacters.addAll(newData)
                currentPage++
                isLastPage = newData.isEmpty()

                // Вместо прямого _characters.postValue:
                val query = _searchQuery.value ?: ""
                val result = if (query.isBlank()) {
                    allCharacters
                } else {
                    allCharacters.filter {
                        it.name.contains(query.trim(), ignoreCase = true)
                    }
                }
                _characters.postValue(result)

            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }


    fun applyFilter(query: String) {
        _searchQuery.value = query
        val result = if (query.isBlank()) {
            allCharacters
        } else {
            allCharacters.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }
        _characters.value = result
    }
    fun loadInitialData() {
        _initialLoading.postValue(true)

        viewModelScope.launch {
            val cached = repository.getCharactersFromCache()
            if (cached.isNotEmpty()) {
                allCharacters.clear()
                allCharacters.addAll(cached)
                applyFilter(_searchQuery.value ?: "")
            }

            loadNextPage() // загрузим 1-ю партию

            _initialLoading.postValue(false)
        }
    }
}


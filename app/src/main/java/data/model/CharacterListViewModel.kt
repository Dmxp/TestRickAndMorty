import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.model.CharacterModel
import kotlinx.coroutines.launch

class CharacterListViewModel : ViewModel() {

    // Оригинальный список всех персонажей
    private var allCharacters: List<CharacterModel> = emptyList()

    // Отображаемый список (с учётом фильтрации)
    private val _characters = MutableLiveData<List<CharacterModel>>()
    val characters: LiveData<List<CharacterModel>> = _characters

    // Текущий поисковый запрос
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    // Загрузка списка персонажей (один раз)
    fun loadCharactersOnce() {
        if (allCharacters.isNotEmpty()) {
            applyFilter(_searchQuery.value ?: "")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCharacters(1)
                allCharacters = response.results
                applyFilter(_searchQuery.value ?: "")
            } catch (_: Exception) {
                // Ошибка загрузки — можно добавить LiveData для ошибок
            }
        }
    }

    // Применить фильтр к списку по имени
    fun applyFilter(query: String) {
        _searchQuery.value = query
        _characters.value = if (query.isBlank()) {
            allCharacters
        } else {
            allCharacters.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }
    }
}
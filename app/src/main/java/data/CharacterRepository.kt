package com.example.testrickandmorty.data

import android.content.Context
import com.example.testrickandmorty.data.api.RetrofitInstance
import com.example.testrickandmorty.data.local.AppDatabase
import com.example.testrickandmorty.data.local.toEntity
import com.example.testrickandmorty.data.local.toModel
import com.example.testrickandmorty.data.model.CharacterFilter
import com.example.testrickandmorty.data.model.CharacterModel

class CharacterRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.characterDao()

    suspend fun getCharactersFromCache(): List<CharacterModel> {
        return dao.getAllCharacters().map { it.toModel() }
    }

    suspend fun getCharactersFromApi(page: Int, filter: CharacterFilter?): List<CharacterModel> {
        val response = RetrofitInstance.api.getCharactersFiltered(
            name = filter?.name,
            status = filter?.status,
            gender = filter?.gender,
            species = filter?.species,
            type = filter?.type,
            page = page
        )
        val models = response.results

        if (page == 1) {
            dao.clearAll()
            dao.insertAll(models.map { it.toEntity() })
        }
        return models
    }


}

package com.example.testrickandmorty.data.api

import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.data.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponse
    @GET("character/{id}")
    suspend fun getCharacterById(@Path("id") id: Int): CharacterModel
}
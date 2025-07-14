package com.example.testrickandmorty.data.api

import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.data.model.CharacterResponse
import com.example.testrickandmorty.data.model.Episode
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponse
    @GET("character/{id}")
    suspend fun getCharacterById(@Path("id") id: Int): CharacterModel
    @GET("episode/{id}")
    suspend fun getSingleEpisode(@Path("id") id: Int): Episode
    @GET("episode/{ids}")
    suspend fun getMultipleEpisodes(@Path("ids") ids: String): List<Episode>
    @GET("character")
    suspend fun getCharactersFiltered(
        @Query("name") name: String?,
        @Query("status") status: String?,
        @Query("gender") gender: String?,
        @Query("species") species: String?,
        @Query("type") type: String?,
        @Query("page") page: Int
    ): CharacterResponse


}
package com.example.testrickandmorty.data.model
//Класс-фильтр
data class CharacterFilter(
    val name: String? = null,
    val status: String? = null,
    val gender: String? = null,
    val species: String? = null,
    val type: String? = null
)

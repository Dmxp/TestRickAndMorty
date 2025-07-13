package com.example.testrickandmorty.data.model
//весь ответ от API содерж. метаинформацию, и список
data class CharacterResponse(
    val info: PageInfo,
    val results: List<CharacterModel>
)
data class PageInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

package com.example.testrickandmorty.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
//модель для получения полная информация
@Parcelize
data class CharacterModel(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: LocationInfo,
    val location: LocationInfo,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
): Parcelable
@Parcelize
data class LocationInfo(
    val name: String,
    val url: String
): Parcelable

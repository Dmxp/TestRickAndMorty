package com.example.testrickandmorty.data.mapper

import com.example.testrickandmorty.data.model.CharacterModel
import data.model.CharacterEntity

fun CharacterModel.toEntity(): CharacterEntity =
    CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image
    )

fun CharacterEntity.toModel(): CharacterModel =
    CharacterModel(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        created = "", // данных нет в Entity
        url = "",
        origin = com.example.testrickandmorty.data.model.LocationInfo("Unknown", ""),
        location = com.example.testrickandmorty.data.model.LocationInfo("Unknown", ""),
        episode = emptyList() // кэш не хранит эпизоды
    )

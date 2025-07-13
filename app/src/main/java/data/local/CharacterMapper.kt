package com.example.testrickandmorty.data.local

import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.data.model.LocationInfo

fun CharacterModel.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        originName = origin.name,
        locationName = location.name,
        image = image,
        created = created
    )
}

fun CharacterEntity.toModel(): CharacterModel {
    return CharacterModel(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = LocationInfo(originName, ""),
        location = LocationInfo(locationName, ""),
        image = image,
        episode = emptyList(), // эпизоды не сохраняем
        url = "",
        created = created
    )
}

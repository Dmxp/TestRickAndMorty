package com.example.testrickandmorty.ui.characterlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testrickandmorty.data.model.CharacterModel
import com.example.testrickandmorty.databinding.ItemCharacterBinding

class CharacterAdapter(
    private val characters: List<CharacterModel>,
    private val onClick: (CharacterModel) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CharacterModel) {
            binding.nameTextView.text = character.name
            binding.statusSpeciesGenderTextView.text =
                "${character.status} • ${character.species} • ${character.gender}"

            Glide.with(binding.imageView.context)
                .load(character.image)
                .into(binding.imageView)

            binding.root.setOnClickListener {
                onClick(character)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(characters[position])
    }

    override fun getItemCount(): Int = characters.size
}

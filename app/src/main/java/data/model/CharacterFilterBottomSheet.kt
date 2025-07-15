package data.model
//диалог с фильтрами
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.testrickandmorty.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CharacterFilterBottomSheet(
    private val initialName: String?,
    private val initialStatus: String?,
    private val initialGender: String?,
    private val initialSpecies: String?,
    private val initialType: String?,
    private val onApply: (name: String?, status: String?, gender: String?, species: String?, type: String?) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var statusSpinner: Spinner
    private lateinit var genderSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_character_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val speciesEditText = view.findViewById<EditText>(R.id.speciesEditText)
        val typeEditText = view.findViewById<EditText>(R.id.typeEditText)
        statusSpinner = view.findViewById(R.id.statusSpinner)
        genderSpinner = view.findViewById(R.id.genderSpinner)

        val statusOptions = arrayOf("Любой", "Alive", "Dead", "unknown")
        val genderOptions = arrayOf("Любой", "Male", "Female", "Genderless", "unknown")

        statusSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        genderSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)

        // Установка значений
        nameEditText.setText(initialName ?: "")
        speciesEditText.setText(initialSpecies ?: "")
        typeEditText.setText(initialType ?: "")

        statusSpinner.setSelection(
            statusOptions.indexOfFirst { it.equals(initialStatus, ignoreCase = true) }.takeIf { it != -1 } ?: 0
        )
        genderSpinner.setSelection(
            genderOptions.indexOfFirst { it.equals(initialGender, ignoreCase = true) }.takeIf { it != -1 } ?: 0
        )

        // Применить фильтр
        view.findViewById<Button>(R.id.applyButton).setOnClickListener {
            val name = nameEditText.text.toString().ifBlank { null }
            val species = speciesEditText.text.toString().ifBlank { null }
            val type = typeEditText.text.toString().ifBlank { null }

            val status = statusSpinner.selectedItem.toString().takeIf { it != "Любой" }
            val gender = genderSpinner.selectedItem.toString().takeIf { it != "Любой" }

            onApply(name, status, gender, species, type)
            dismiss()
        }

        // Сбросить фильтр
        view.findViewById<Button>(R.id.resetButton).setOnClickListener {
            onApply(null, null, null, null, null)
            dismiss()
        }
    }
}

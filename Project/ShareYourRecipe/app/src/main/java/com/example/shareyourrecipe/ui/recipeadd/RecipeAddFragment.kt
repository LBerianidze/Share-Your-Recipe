package com.example.shareyourrecipe.ui.recipeadd

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.shareyourrecipe.databinding.FragmentRecipeAddBinding
import com.example.shareyourrecipe.model.Category
import com.example.shareyourrecipe.model.Recipe
import com.example.shareyourrecipe.ui.multispinner.MultiSpinner
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class RecipeAddFragment : Fragment(), MultiSpinner.MultiSpinnerListener {
    private var _binding: FragmentRecipeAddBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    val map: MutableMap<Int, String> = mutableMapOf()
    private var booleanArray: BooleanArray? = null;
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    selectedImageUri = data?.data!!
                    binding.imageView.setImageURI(selectedImageUri)
                    binding.imageView.visibility = View.VISIBLE
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeAddBinding.inflate(inflater, container, false)
        val view = binding.root
        setupSaveButtonListener()


        binding.btnSelectImage.setOnClickListener {
            ImagePicker.with(this).galleryOnly()
                .createIntent { intent -> startForProfileImageResult.launch(intent) }
        }

        val categoriesDbReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("categories")
        val categoriesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (categorySnapshot in dataSnapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)

                    map[category!!.id] = category.name;
                }
                val multiSpinner = binding.categoriesSelector
                multiSpinner.setItems(
                    map.values.toList(),
                    "აირჩიეთ კატეგორიები",
                    this@RecipeAddFragment
                )


            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        categoriesDbReference.addValueEventListener(categoriesListener)

        return view
    }

    private fun setupSaveButtonListener() {
        binding.btnSaveRecipe.setOnClickListener {

            if (booleanArray == null || booleanArray!!.all { x -> !x }) {
                Toast.makeText(this.requireContext(), "აირჩიეთ კატეგორიები", Toast.LENGTH_LONG)
                    .show()
            } else {

                saveRecipeToDatabase()
            }
        }
    }

    private fun saveRecipeToDatabase() {
        val recipeId = FirebaseDatabase.getInstance().reference.child("recipes").push().key

        val imageFileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = FirebaseStorage.getInstance().reference.child("images").child(imageFileName)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (selectedImageUri != null) {
            imageRef.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val recipeName = binding.etRecipeName.text.toString()
                    val preparationTime = binding.etPreparationTime.text.toString()
                    val recipeDescription = binding.etRecipeDescription.text.toString()

                    val selectedKeys = StringBuilder()

                    for (i in booleanArray!!.indices) {
                        if (booleanArray!![i]) {
                            val key = map.keys.elementAtOrNull(i)
                            key?.let {
                                selectedKeys.append(it)
                                selectedKeys.append(",")
                            }
                        }
                    }
                    val recipe = Recipe(
                        userId = currentUser?.uid ?:"",
                        name = recipeName,
                        preparationTime = preparationTime,
                        imageUrl = uri.toString(),
                        description = recipeDescription,
                        category = selectedKeys.dropLast(1).toString()
                    )
                    saveRecipeData(recipe, recipeId.toString())
                }
            }.addOnFailureListener { exception ->
            }
        } else {
            Toast.makeText(this.requireContext(), "გთხოვთ, ატვირთოთ ფოტო", Toast.LENGTH_LONG).show()

        }
    }

    private fun saveRecipeData(recipe: Recipe, recipeId: String) {
        FirebaseDatabase.getInstance().reference.child("recipes").child(recipeId).setValue(recipe)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    parentFragmentManager.popBackStack()
                } else {
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemsSelected(selected: BooleanArray?) {
        booleanArray = selected
    }
}
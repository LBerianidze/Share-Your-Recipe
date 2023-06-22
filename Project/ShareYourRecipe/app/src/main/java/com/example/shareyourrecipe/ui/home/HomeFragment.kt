package com.example.shareyourrecipe.ui.home

import RecipeAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shareyourrecipe.LoginActivity
import com.example.shareyourrecipe.R
import com.example.shareyourrecipe.databinding.FragmentHomeBinding
import com.example.shareyourrecipe.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeListener: ValueEventListener
    private lateinit var databaseReference: DatabaseReference
    private var categoryId: Int = -1
    private var showOnlyCurrentUserRecipe: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        arguments?.let {
            categoryId = it.getInt("categoryId", -1)
            showOnlyCurrentUserRecipe = it.getBoolean("currentUser", false)
        }
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.addNew.setOnClickListener {
            val navController =
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_recipeadd)
        }
        textView.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        recipeAdapter = RecipeAdapter { x ->

            val bundle = Bundle().apply {
                putString("foodName", x.name)
                putString("preparationTime", x.preparationTime)
                putString("photoUrl", x.imageUrl)
                putString("description", x.description)
            }

            findNavController().navigate(R.id.nav_recipedetail, bundle)
        }
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recipeRecyclerView.adapter = recipeAdapter


        databaseReference = FirebaseDatabase.getInstance().getReference("recipes")
        val currentUser = FirebaseAuth.getInstance().currentUser

        recipeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val recipeList = mutableListOf<Recipe>()
                for (recipeSnapshot in dataSnapshot.children) {
                    val recipe = recipeSnapshot.getValue(Recipe::class.java)
                    if (categoryId != -1 && recipe?.category!!.split(",")
                            .all { it != categoryId.toString() }
                    ) continue;
                    if (showOnlyCurrentUserRecipe && currentUser != null && currentUser.uid != recipe?.userId) continue;
                    recipe?.let {
                        recipeList.add(it)
                    }
                }
                recipeAdapter.setRecipes(recipeList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        databaseReference.addValueEventListener(recipeListener)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
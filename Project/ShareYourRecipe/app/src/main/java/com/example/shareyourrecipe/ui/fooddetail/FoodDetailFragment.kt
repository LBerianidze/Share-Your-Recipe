package com.example.shareyourrecipe.ui.fooddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shareyourrecipe.R
import com.example.shareyourrecipe.databinding.FragmentFoodDetailBinding
import com.example.shareyourrecipe.databinding.FragmentHomeBinding
import com.squareup.picasso.Picasso

class FoodDetailFragment : Fragment() {
    private var _binding: FragmentFoodDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foodName = arguments?.getString("foodName")
        val preparationTime = arguments?.getString("preparationTime")
        val photoUrl = arguments?.getString("photoUrl")
        val description = arguments?.getString("description")


        binding.textViewFoodName.text = foodName
        binding.textViewPreparationTime.text = preparationTime

        Picasso.get().load(photoUrl).into(binding.imageViewFood)


        binding.textViewDescription.text = description
    }

}

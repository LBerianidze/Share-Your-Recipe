package com.example.shareyourrecipe.model

data class Recipe(
    val userId: String = "",
    val name: String = "",
    val preparationTime: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val category: String = ""
)
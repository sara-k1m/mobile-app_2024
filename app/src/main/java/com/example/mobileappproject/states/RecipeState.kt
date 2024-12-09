package com.example.mobileappproject.states

import java.util.UUID

data class RecipeState(
    val id: String = UUID.randomUUID().toString(),
    val userNickname: String = "",  // User ID to associate recipes with the logged-in user
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    val method: List<String> = emptyList(),
    var category: List<String> = emptyList(),
    val bookMarked: Boolean = false,
)

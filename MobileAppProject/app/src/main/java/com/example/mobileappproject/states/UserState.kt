package com.example.mobileappproject.states

import java.time.LocalDate

data class UserState(
    var nickname: String = "",
    var password: String ="",
    var recipeList: MutableList<RecipeState> = mutableListOf(), // 레시피 리스트
    var shoppingToDoMap: MutableMap<LocalDate, MutableList<RecipeState>> = mutableMapOf() // 쇼핑 리스트
)
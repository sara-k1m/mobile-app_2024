package com.example.mobileappproject.states

import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

object SharedState {
    val recipeAlarmState = MutableStateFlow(RecipeAlarmState())
}

data class RecipeAlarmState(
    val recipeName: String="",
    val localDate: LocalDate=LocalDate.now(),
    val nickname: String=""
)
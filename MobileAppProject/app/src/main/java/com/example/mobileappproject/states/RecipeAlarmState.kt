package com.example.mobileappproject.states

import java.time.LocalDate

data class RecipeAlarmState(
    val recipeName: String="",
    val localDate: LocalDate=LocalDate.now(),
)
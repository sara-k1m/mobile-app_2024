package com.example.mobileappproject.ui

import java.util.Calendar

data class AlarmUiState(
    val alarmId: Int,
    val hour: Int = Calendar.HOUR_OF_DAY,
    val minute: Int = Calendar.MINUTE,
    val activate: Boolean = true,
)
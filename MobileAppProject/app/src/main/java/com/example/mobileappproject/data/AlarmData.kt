package com.example.mobileappproject.data

import java.time.LocalDate

// 알람에 관련된거만 나머지는 상위에서 받아옴.
data class AlarmData(
    // 알람id는 그냥 index느낌으로 정한다, 파이어베이스 저장용 Id는 따로 둘것임.
    val alarmId: Int,
    val hour: Int,
    val minute: Int,
    val activate: Boolean,
    val recipeName: String = "",
    val localDate: LocalDate
)
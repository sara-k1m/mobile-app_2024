package com.example.mobileappproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 알람 발생 시 실행할 작업
        Toast.makeText(context, "알람이 울립니다!", Toast.LENGTH_LONG).show()
        // 추가 작업: 알림(Notification) 표시 등
    }
}
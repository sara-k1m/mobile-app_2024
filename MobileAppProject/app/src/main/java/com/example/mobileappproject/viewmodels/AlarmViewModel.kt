package com.example.mobileappproject.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.mobileappproject.AlarmReceiver
import com.example.mobileappproject.data.AlarmData
import com.example.mobileappproject.ui.AlarmUiState
import com.example.mobileappproject.states.RecipeAlarmState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import android.app.AlarmManager as AlarmManager

// 알람의 저장, 수정, 삭제를 담당하는 뷰모델. 현재는 alarmId를 받는다기보단 index가 그 역할을 대체한다.
// AlarmUiState는 AlarmSetting 할 때 띄움. AlarmData로 바꿔 사용함.
// 내가 AlarmSettingPage에서 시간 바꿀때 현재 시간이 바뀌는등의 로직 필요.
// AlarmSettingPage에서 바꾼다: uiState를 이용할 필요가있음, AlarmPage에서 바꾼다: uiState이용x
class AlarmViewModel: ViewModel() {
    private var _alarmList = mutableStateListOf<AlarmData>()
    val alarmList: List<AlarmData> = _alarmList

    private var _uiState = MutableStateFlow(AlarmUiState(alarmId = _alarmList.size))
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private var _recipeUiState = MutableStateFlow(RecipeAlarmState())
    val recipeUiState: StateFlow<RecipeAlarmState> = _recipeUiState.asStateFlow()

    private var _isModifyMode = MutableStateFlow(false)
    val isModifyMode: StateFlow<Boolean> = _isModifyMode.asStateFlow()

    fun saveAlarm(context: Context) {
        _alarmList.add(stateToAlarmData())
        setAlarm(context, _alarmList[_alarmList.size-1].hour, _alarmList[_alarmList.size-1].minute,
            _alarmList.size-1)

        _uiState.update { currentState ->
            currentState.copy(
                alarmId = _alarmList.size
            )
        }
    }

    fun updateAlarm(context: Context){
        cancelAlarm(context, _uiState.value.alarmId)
        _alarmList.removeAt(_uiState.value.alarmId)
        _alarmList.add(_uiState.value.alarmId, stateToAlarmData())
        setAlarm(context, _uiState.value.hour, _uiState.value.minute, _uiState.value.alarmId)
        resetState()
    }

    // 알람리스트에서 볼 수 있는건 인덱스로 지정해놓음. 나중에 알람id로 바꿔야할 수도 있음
    // 편의상 alarmId에 음수를 붙이고, 음수가 붙은 알람id는 컬럼으로 안만들어지게함.
    fun removeAlarm(context: Context, index: Int){
        cancelAlarm(context, index)
        _alarmList[index]=_alarmList[index].copy(
            alarmId = (-1) * _alarmList[index].alarmId + (-1)
        )
    }

    fun alarmSwitch(context: Context, index: Int){
        _alarmList[index]=_alarmList[index].copy(
            activate = !_alarmList[index].activate
        )

        if (_alarmList[index].activate) {
            setAlarm(context, _alarmList[index].hour, _alarmList[index].minute, index)
        } else {
            cancelAlarm(context, index) // 알람 취소
        }
    }


    fun stateToAlarmData(): AlarmData {
        return AlarmData(
            alarmId = _uiState.value.alarmId,
            hour = _uiState.value.hour,
            minute = _uiState.value.minute,
            activate = _uiState.value.activate,
            recipeName = _recipeUiState.value.recipeName,
            localDate = _recipeUiState.value.localDate
        )
    }

    // 이게 알람을 선택하는거임. 알람을 선택한다: 현재 상태를 선택한 알람으로 한다.
    // 알람 세팅 페이지에 가기 직전에 한다. 만약 isModifyMode가 true면 수정(update)하는 거고 false면 새로운거 저장(save)하는 거다.
    fun alarmDataToState(index: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                alarmId = _alarmList.get(index).alarmId,
                hour = _alarmList.get(index).hour,
                minute = _alarmList.get(index).minute,
                activate = _alarmList.get(index).activate
            )
        }

        _isModifyMode.update { true }
    }

    fun resetState(){
        _uiState.update { currentState->
            currentState.copy(
                alarmId = _alarmList.size,
                hour = Calendar.HOUR_OF_DAY,
                minute = Calendar.MINUTE,
                activate = true
            )
        }
        _isModifyMode.update { false }
    }

    fun checkSwitch(): Boolean{
        _alarmList.forEach {
                alarmData ->
            if(alarmData.activate)
                return true
        }
        return false
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                hour = hour,
                minute = minute
            )
        }
    }

    private fun setAlarm(context: Context, hour: Int, minute: Int, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent) // 알람 취소
        pendingIntent.cancel()
    }

    fun ensureExactAlarmPermission(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
    }
}
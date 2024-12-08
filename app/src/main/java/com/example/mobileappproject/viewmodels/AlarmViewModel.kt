package com.example.mobileappproject.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappproject.AlarmReceiver
import com.example.mobileappproject.data.AlarmData
import com.example.mobileappproject.ui.AlarmUiState
import com.example.mobileappproject.states.RecipeAlarmState
import com.example.mobileappproject.states.SharedState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import android.app.AlarmManager as AlarmManager

// 알람의 저장, 수정, 삭제를 담당하는 뷰모델. 현재는 alarmId를 받는다기보단 index가 그 역할을 대체한다.
// AlarmUiState는 AlarmSetting 할 때 띄움. AlarmData로 바꿔 사용함.
// 내가 AlarmSettingPage에서 시간 바꿀때 현재 시간이 바뀌는등의 로직 필요.
// AlarmSettingPage에서 바꾼다: uiState를 이용할 필요가있음, AlarmPage에서 바꾼다: uiState이용x
class AlarmViewModel: ViewModel(
) {
    private var _alarmList = mutableStateListOf<AlarmData>()
    val alarmList: List<AlarmData> = _alarmList

    private var _uiState = MutableStateFlow(AlarmUiState(alarmId = _alarmList.size))
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private var _isModifyMode = MutableStateFlow(false)
    val isModifyMode: StateFlow<Boolean> = _isModifyMode.asStateFlow()

    private val _nextAlarmText = MutableStateFlow("")
    val nextAlarmText: StateFlow<String> = _nextAlarmText

    val recipeUiState = SharedState.recipeAlarmState

    var _isGet = false

    init {
        observeRecipeUiState()
    }

    private fun observeRecipeUiState() {
        viewModelScope.launch {
            recipeUiState.collectLatest { updatedState ->
                // recipeUiState 변경 시 호출
                recalculateNextAlarm()
            }
        }
    }

    private var _database = FirebaseDatabase.getInstance()

    private fun getAlarmsRef(nickname: String): DatabaseReference {
        return _database.getReference("alarms").child(nickname)
    }


    fun getAlarm(){
        if(_isGet)
            return

        // db의 알람들을 가져오는데 alarmList에 넣고 size-1의 id를 부여해줌
        if(_alarmList.size == 0){
            val alarmsRef = getAlarmsRef(recipeUiState.value.nickname)
            alarmsRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    snapshot.children.forEach { childSnapshot ->
                        val alarm = childSnapshot.getValue(AlarmData::class.java)
                        val key = childSnapshot.key

                        if (alarm != null && key != null) {
                            if(recipeUiState.value.recipeName == alarm.recipeName
                                && recipeUiState.value.localDate.toString() == alarm.localDate){
                                val updatedAlarm = alarm.copy(alarmId = _alarmList.size)
                                _alarmList.add(updatedAlarm)
                                println("Add Alarm ${updatedAlarm.alarmId}")
                            }
                        }
                    }

                    // UI 상태 업데이트
                    _uiState.update { currentState ->
                        currentState.copy(
                            alarmId = _alarmList.size
                        )
                    }

                    recalculateNextAlarm()

                    println("alarm set _alarmList")
                } else {
                    println("no alarm ${recipeUiState.value.nickname}")
                }
                _isGet = true
            }.addOnFailureListener { exception ->
                println("get failed ${exception.message}")
            }
        }
    }


    fun saveAlarm(context: Context) {
        val alarmsRef = getAlarmsRef(recipeUiState.value.nickname)
        val newKey = alarmsRef.push().key
        if (newKey != null) {
            val alarmData = stateToAlarmData().copy(id = newKey)

            alarmsRef.child(newKey).setValue(alarmData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _alarmList.add(alarmData)

                    val localDate = LocalDate.parse(_alarmList[_alarmList.size-1].localDate)
                    val year = localDate.year
                    val month = localDate.monthValue
                    val day = localDate.dayOfMonth

                    setAlarm(context, year, month, day, _alarmList[_alarmList.size-1].hour, _alarmList[_alarmList.size-1].minute,
                        _alarmList.size-1)

                    _uiState.update { currentState ->
                        currentState.copy(
                            alarmId = _alarmList.size
                        )
                    }
                } else {
                    println("save failed ${task.exception}")
                }
                recalculateNextAlarm()
            }
        }
    }

    fun updateAlarm(context: Context){
        val alarmsRef = getAlarmsRef(recipeUiState.value.nickname)
        cancelAlarm(context, _uiState.value.alarmId)
        _alarmList.removeAt(_uiState.value.alarmId)
        val alarmData = stateToAlarmData()
        _alarmList.add(_uiState.value.alarmId, alarmData)

        val localDate = LocalDate.parse(_alarmList[_uiState.value.alarmId].localDate)
        val year = localDate.year
        val month = localDate.monthValue
        val day = localDate.dayOfMonth

        setAlarm(context, year, month, day, _uiState.value.hour, _uiState.value.minute, _uiState.value.alarmId)

        alarmsRef.child(alarmData.id).setValue(alarmData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("alarm updated {$alarmData.id}")

            } else {
                println("Failed to update alarm: ${task.exception}")
            }
        }

        resetState()
        recalculateNextAlarm()
    }

    // 알람리스트에서 볼 수 있는건 인덱스로 지정해놓음. 나중에 알람id로 바꿔야할 수도 있음
    // 편의상 alarmId에 음수를 붙이고, 음수가 붙은 알람id는 컬럼으로 안만들어지게함.
    fun removeAlarm(context: Context, index: Int){
        val alarmsRef = getAlarmsRef(recipeUiState.value.nickname)
        cancelAlarm(context, index)

        val newId = (-1) * _alarmList[index].alarmId + (-1)
        _alarmList[index]=_alarmList[index].copy(
            alarmId = newId
        )

        alarmsRef.child(_alarmList[index].id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("alarm updated id")

            } else {
                println("Failed to update alarm: ${task.exception}")
            }
        }

        recalculateNextAlarm()
    }

    fun alarmSwitch(context: Context, index: Int){
        _alarmList[index]=_alarmList[index].copy(
            activate = !_alarmList[index].activate
        )

        if (_alarmList[index].activate) {
            val localDate = LocalDate.parse(_alarmList[index].localDate)
            val year = localDate.year
            val month = localDate.monthValue
            val day = localDate.dayOfMonth
            setAlarm(context, year, month, day, _alarmList[index].hour, _alarmList[index].minute, index)
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
            recipeName = recipeUiState.value.recipeName,
            localDate = recipeUiState.value.localDate.toString()
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
                activate = _alarmList.get(index).activate,
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

    private fun setAlarm(context: Context, year: Int, month: Int, day: Int, hour: Int, minute: Int, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Log.d("AlarmViewModel", "알람이 과거 시간으로 설정됨: 설정 취소")
            return // 과거 시간이면 알람 설정하지 않음
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

    // 지금 시간보다 일찍 설정된 알람은 알람으로 취급안함.
    private fun recalculateNextAlarm() {
        val now = Calendar.getInstance()
        val nextAlarm = _alarmList
            .filter {
                it.activate
                        && it.alarmId >= 0
                        && it.recipeName == recipeUiState.value.recipeName
                        && it.localDate == recipeUiState.value.localDate.toString()
            }
            .filter { alarm ->
                val localDate = LocalDate.parse(alarm.localDate)
                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, localDate.year)
                    set(Calendar.MONTH, localDate.monthValue - 1) // MONTH는 0부터 시작
                    set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 현재 시간 이후의 알람만 포함
                alarmTime.timeInMillis > now.timeInMillis
            }
            .minByOrNull { alarm ->
                val localDate = LocalDate.parse(alarm.localDate)
                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, localDate.year)
                    set(Calendar.MONTH, localDate.monthValue - 1)
                    set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                // 가장 가까운 알람 시간으로 정렬
                alarmTime.timeInMillis - now.timeInMillis
            }

        _nextAlarmText.update {
            nextAlarm?.let {
                String.format("다음 시간은")
                if(it.hour >= 12){
                    String.format("오후 %02d : %02d에 울립니다", it.hour-12, it.minute)
                }else{
                    String.format("오전 %02d : %02d에 울립니다", it.hour, it.minute)
                }
            } ?: "모든 알람이 꺼진 상태입니다."
        }
    }
}
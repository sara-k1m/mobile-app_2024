package com.example.mobileappproject.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileappproject.R
import com.example.mobileappproject.viewmodels.AlarmViewModel

// 초기 시간 설정을 받아야 한다.
@Composable
fun AlarmSettingPage(
    context: Context,
    alarmViewModel: AlarmViewModel = viewModel(),
    returnToAlarmPage: () -> Unit,
    saveAlarm: (Context) -> Unit,
    updateAlarm: (Context) -> Unit,
    resetState: () -> Unit,
) {

    val isModifyMode by alarmViewModel.isModifyMode.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputTimePickerDialog(
                alarmViewModel,
                onConfirm = {
                    hour, minute ->
                    alarmViewModel.updateTime(hour, minute)
                    if(isModifyMode){
                        updateAlarm(context)
                    }else{
                        saveAlarm(context)
                    }
                    resetState()
                    returnToAlarmPage()
                },
                onDismiss = {
                    resetState()
                    returnToAlarmPage()
                }
            )

            // 여기다가 메모를 적으면 ingredient에 적히게 함.
            // 아직 따로 안함.
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTimePickerDialog(
    alarmViewModel: AlarmViewModel,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val alarmUiState by alarmViewModel.uiState.collectAsState()

    val timePickerState = rememberTimePickerState(
        initialHour = alarmUiState.hour,
        initialMinute = alarmUiState.minute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 입력 방식 TimeInput 표시
                TimeInput(
                    state = timePickerState
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Button(onClick = {
                        onConfirm(timePickerState.hour, timePickerState.minute)
                    }) {
                        Text(stringResource(id =R.string.confirm))
                    }
                }
            }
        }
    }
}
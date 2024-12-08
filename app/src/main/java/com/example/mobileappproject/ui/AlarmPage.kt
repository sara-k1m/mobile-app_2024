package com.example.mobileappproject.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileappproject.R
import com.example.mobileappproject.data.AlarmData
import com.example.mobileappproject.viewmodels.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmPage(
    alarmViewModel: AlarmViewModel = viewModel(),
    context: Context,
    alarmDataToState: (Int) -> Unit,
    removeAlarm: (Context, Int) -> Unit,
    alarmSwitch: (Context, Int) -> Unit,
    returnToMain: () -> Unit,
    goToAlarmSettingPage: () -> Unit,
){

    val recipeUiState by alarmViewModel.recipeUiState.collectAsState()
    val nextAlarmText by alarmViewModel.nextAlarmText.collectAsState()

    LaunchedEffect(Unit) {
        alarmViewModel.getAlarm()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar ={
            TopAppBar(
                title = {Text(text = "back")}
                ,navigationIcon = {
                    IconButton(onClick = returnToMain) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                })
        }
    ){innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
        ){
            // 고정된 크기 차지하면서 가장 가까운 알람이 몇시간 남았는지 알려주는 역할
            // 알람이 없으면 없다고 표시됨
            Card(
                modifier = Modifier
                    .padding(40.dp)
                    .padding(top = 80.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),

            ) {
                if(alarmViewModel.checkSwitch()){
                    // 가장 가까운 알람을 자동으로 메세지 해줘야함.
                    // 아직 구현 안함.
                    Text(
                        text =nextAlarmText,
                        style = typography.displayLarge,
                        fontSize = 30.sp
                    )
                }else{
                    Text(
                        text = stringResource(id =R.string.all_stop),
                        style = typography.displayLarge,
                        fontSize = 30.sp
                        )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // 알람 추가 아이콘, 필요하지 않을 수도 있음.
                FloatingActionButton(
                    onClick = {
                    goToAlarmSettingPage()
                    }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "알람 추가")
                }
            }

            // lazyColumn을 쓸 예정
            // items안에는 AlarmItem안에 들어갈 상태 같은걸 넣어줌
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(alarmViewModel.alarmList.filter {
                    it.alarmId >= 0
                            && it.recipeName == recipeUiState.recipeName
                            && it.localDate == recipeUiState.localDate.toString()
                }) { alarmData ->
                    AlarmItem(
                        alarmViewModel, context, alarmData,
                        alarmDataToState, removeAlarm, alarmSwitch, goToAlarmSettingPage
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarmViewModel: AlarmViewModel,
    context: Context,
    alarmData: AlarmData,
    alarmDataToState: (Int) -> Unit,
    removeAlarm: (Context, Int) -> Unit,
    alarmSwitch: (Context, Int) -> Unit,
    goToAlarmSettingPage: () -> Unit
) {

    // 이 카드의 onClick을 누르면 알람 시간을 설정할 수 있는 페이지로 넘어갈거임.
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            alarmDataToState(alarmData.alarmId)
            goToAlarmSettingPage()
                  },
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(

                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = if(alarmData.hour >= 12){
                                stringResource(id =R.string.pm)
                            }else{
                                stringResource(id =R.string.am)
                            },
                            style= typography.labelLarge,)
                        Text(
                            text = if(alarmData.hour >= 12){
                               String.format("%02d:%02d", alarmData.hour-12, alarmData.minute)
                            }else{
                                String.format("%02d:%02d", alarmData.hour, alarmData.minute)
                            },
                            style = typography.displayMedium
                        )
                    }
                    // 레시피에서 받아 처리하는부분 구현안했고, alarmSettingPage가 전달받을 필요없음.
                    // 여기 해야함.
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 40.dp),
                        text= alarmData.recipeName,
                        fontSize=40.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Switch(checked = alarmViewModel.alarmList[alarmData.alarmId].activate,
                        onCheckedChange = {
                        alarmSwitch(context, alarmData.alarmId)
                    })
                    IconButton(onClick = { removeAlarm(context, alarmData.alarmId) }) {
                        Icon(Icons.Filled.Close, contentDescription = "remove Alarm")
                    }
                }
            }

        }

}

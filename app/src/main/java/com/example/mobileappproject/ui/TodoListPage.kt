package com.example.mobileappproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileappproject.ui.theme.p1
import com.example.mobileappproject.ui.theme.p2
import com.example.mobileappproject.ui.theme.p3
import com.example.mobileappproject.ui.theme.p4
import com.example.mobileappproject.ui.theme.p5


// 날짜 변수
@Composable
fun TodoListPage(
    goToAlarmPage: () -> Unit
){
    Scaffold(
        modifier= Modifier
            .fillMaxSize(),
    ) {
        innerPadding ->
        Column(
            modifier= Modifier
                .padding(innerPadding)
        ) {
            // 실제 구현할 때 없어질 부분(현재는 빈공간) , 이칸을 좀 넓히라고 했음
            Spacer(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .padding(innerPadding)
            )


            TodoListBox(
                goToAlarmPage = goToAlarmPage
            )
        }
    }
}

// 요리 제목, 투두리스트, 요리재료가 적힌 박스
@Composable
fun TodoListBox(
    goToAlarmPage: () -> Unit
){
    Column(

    ) {
        Card(
            modifier = Modifier,
            onClick = {}
        ) {

            Box(
                modifier= Modifier
                    .background(
                        color= p2,
                        shape = RoundedCornerShape(16.dp)
                        )
                    .border(
                        width = 4.dp,
                        color = p4,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp)
                    )
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        modifier=Modifier,
                        text= "  "+"카레",
                        fontSize = 25.sp,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        modifier= Modifier,
                        onClick = goToAlarmPage
                    ) { Icon(Icons.Filled.Notifications, contentDescription = "to AlarmPage") }
                }
            }

            Column(
                modifier = Modifier
                    .background(
                        color=p3,
                    )
                    .weight(5f)
            ) {
                ingredient()
            }
        }
    }
}

//요리 재료
@Composable
fun ingredient(

){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = p1,
                shape = RoundedCornerShape(
                    16.dp
                )
            )
            .border(
                width = 2.dp,
                color = p5,
                shape = RoundedCornerShape(
                    16.dp
                )
            )
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "  · 감자: 1/2개",
            fontSize = 30.sp,

        )

        Checkbox(
            checked = true,
            onCheckedChange = {}
        )

    }
}
package com.example.mobileappproject.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mobileappproject.states.RecipeState
import com.example.mobileappproject.ui.theme.Purple80
import com.example.mobileappproject.ui.theme.SkyBlue
import com.example.mobileappproject.viewmodels.AlarmViewModel
import com.example.mobileappproject.viewmodels.UserViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    goToAlarmPage: () -> Unit,
    setRecipeState: (RecipeState, LocalDate) -> Unit
) {
    var currentDateTime by rememberSaveable { mutableStateOf(LocalDateTime.now()) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    // 현재 로그인한 유저 정보를 가져옴
    val loggedInUserState by userViewModel.userStateFlow.collectAsState()
    val shoppingList = loggedInUserState.shoppingToDoMap[selectedDate]

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    IconButton(
                        onClick = {
                            if (selectedDate != null) {
                                // Compose Navigation은 selectedDate의 toString()을 호출하여 자동으로 LocalDate를 String으로 처리
                                navController.navigate("recipe/${selectedDate}")
                            } else {
                                Toast.makeText(context, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "to-do 리스트 추가"
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedDate = null
                            navController.navigate("recipe")
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "레시피 관리 화면으로 이동"
                        )
                    }

                    IconButton(
                        onClick = {
                            userViewModel.logoutUser()
                            navController.navigateUp()
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "로그아웃"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarApp(
                currentDateTime = currentDateTime,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onDateTimeChange = { currentDateTime = it }
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 1.dp, color = Color.Red),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (selectedDate != null && !shoppingList.isNullOrEmpty()) {
                    items(shoppingList) { recipe ->
                        ToDoList(
                            recipe,
                            onRemoveRecipe = {
                                // 쇼핑 리스트에서 레시피 삭제
                                userViewModel.removeItemFromShoppingList(selectedDate!!, it)
                            }
                            ,selectedDate!!,
                            goToAlarmPage,
                            setRecipeState
                        )
                        Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    item {
                        Text(
                            text = "쇼핑리스트가 비어있습니다.",
                            modifier = Modifier.padding(top = 150.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoList(
    recipe: RecipeState,
    onRemoveRecipe: (RecipeState) -> Unit,
    selectedDate: LocalDate,
    goToAlarmPage: () -> Unit,
    setRecipeState: (RecipeState, LocalDate) -> Unit
) {
    ToDoHeader(
        recipe,
        onRemoveRecipe,
        selectedDate,
        setRecipeState,
        goToAlarmPage
    )

    recipe.ingredients.forEach { ingredient ->
        TodoItem(ingredient)
    }
}

@Composable
fun ToDoHeader(
    recipe: RecipeState,
    onRemoveRecipe: (RecipeState) -> Unit,
    selectedDate: LocalDate,
    setRecipeState: (RecipeState, LocalDate) -> Unit,
    goToAlarmPage: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().background(color = Purple80).padding(start = 4.dp, end = 4.dp)
    ) {
        IconButton(
            onClick = { onRemoveRecipe(recipe) }
        ) {
            Icon(Icons.Default.Close, contentDescription = "to-do 리스트 삭제")
        }

        Text(text = recipe.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

        IconButton(
            onClick = {
                setRecipeState(recipe, selectedDate)
                goToAlarmPage()
            }
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = "알람 페이지로 이동")
        }
    }
}

@Composable
fun TodoItem(ingredient: String) {
    var checked by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().background(color = SkyBlue).padding(start = 16.dp, end = 4.dp)
    ) {
        Text(
            text = ingredient, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            color = if (checked) Color.Gray else Color.Black,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
        )

        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////

@Composable
fun CalendarApp(
    currentDateTime: LocalDateTime,
    selectedDate: LocalDate?, // The currently selected date
    onDateSelected: (LocalDate?) -> Unit, // Callback for when a date is selected
    onDateTimeChange: (LocalDateTime) -> Unit // Callback function to handle date changes
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarHeader(currentDateTime = currentDateTime)
        CalendarHeaderBtn(
            currentDateTime = currentDateTime,
            onDateTimeChange = onDateTimeChange
        )
        CalendarDayOfTheWeek()
        Spacer(modifier = Modifier.padding(8.dp))
        CalendarDayList(
            currentDateTime = currentDateTime,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
fun CalendarHeader(
    currentDateTime: LocalDateTime
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
    val resultTime = currentDateTime.format(formatter)

    Text(
        text = resultTime,
        fontSize = 25.sp
    )
}

@Composable
fun CalendarHeaderBtn(
    currentDateTime: LocalDateTime,
    onDateTimeChange: (LocalDateTime) -> Unit // Callback for changing date
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                // Move to the previous month
                onDateTimeChange(currentDateTime.minusMonths(1))
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달로 이동"
            )
        }

        IconButton(
            onClick = {
                // Move to the next month
                onDateTimeChange(currentDateTime.plusMonths(1))
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달로 이동"
            )
        }
    }
}

@Composable
fun CalendarDayOfTheWeek() {
    val nameList = listOf("일", "월", "화", "수", "목", "금", "토")

    Row {
        nameList.forEach {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
fun CalendarDayList(
    currentDateTime: LocalDateTime,
    selectedDate: LocalDate?, // Pass the selected date
    onDateSelected: (LocalDate?) -> Unit
) {
    val today = LocalDate.now()

    // Separate logic: Calculate the calendar data
    val calendarData = calculateCalendarData(currentDateTime)

    Column {
        calendarData.weeks.forEach { week ->
            Row {
                week.forEach { day ->
                    if (day != null) {
                        val currentDate = currentDateTime.withDayOfMonth(day).toLocalDate()

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$day",
                                color = if (currentDate == selectedDate) Color.Red else Color.Black, // Red if selected, otherwise black
                                fontWeight = if (currentDate == selectedDate) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .background(
                                        color = if (currentDate == today) Color.Yellow else Color.Transparent,
                                        shape = CircleShape // Circular background for today's date
                                    )
                                    .padding(12.dp)
                                    .clickable {
                                        onDateSelected(currentDate) // Notify MainScreen of the new selection
                                    }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Empty space for days outside the current month
                    }
                }
            }
        }
    }
}

data class CalendarData(
    val weeks: List<List<Int?>> // A list of weeks, where each week is a list of 7 days (nullable Int for blank days)
)

fun calculateCalendarData(dateTime: LocalDateTime): CalendarData {
    val firstDayInMonth = dateTime.withDayOfMonth(1) // First day of the current month
    val totalDaysInMonth = dateTime.toLocalDate().lengthOfMonth() // Total number of days in the month
    val firstDayOfWeek = firstDayInMonth.dayOfWeek.ordinal + 1 // Day of the week for the first day (1 = Monday, ..., 7 = Sunday)
    val totalWeeksInMonth = (totalDaysInMonth + firstDayOfWeek + 6) / 7 // Total number of weeks needed

    val weeks = mutableListOf<List<Int?>>()

    for (week in 0 until totalWeeksInMonth) {
        val days = mutableListOf<Int?>()
        for (day in 0..6) {
            val resultDay = week * 7 + day - firstDayOfWeek + 1
            if (resultDay in 1..totalDaysInMonth) {
                days.add(resultDay) // Add valid days of the month
            } else {
                days.add(null) // Add null for blank spaces
            }
        }
        weeks.add(days)
    }

    return CalendarData(weeks)
}
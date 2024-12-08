package com.example.mobileappproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobileappproject.ui.AlarmPage
import com.example.mobileappproject.ui.AlarmSettingPage
import com.example.mobileappproject.ui.TodoListPage
import com.example.mobileappproject.ui.theme.MobileAppProjectTheme
import androidx.navigation.compose.rememberNavController
import com.example.mobileappproject.viewmodels.AlarmViewModel
import com.example.mobileappproject.ui.MainScreen
import com.example.mobileappproject.viewmodels.UserViewModel
import com.example.myrecipeplanner.screens.RecipeManagementScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            MobileAppProjectTheme{
                val alarmViewModel = viewModel<AlarmViewModel>()

                val navController = rememberNavController()

                val context = LocalContext.current

                alarmViewModel.ensureExactAlarmPermission(context)

                val userViewModel: UserViewModel = viewModel()

                NavHost(navController = navController, startDestination = "main"){
                    composable(route = "TodoListPage"){
                        // 여기서 위에서 레시피 정보 받아옴
                        TodoListPage(
                            goToAlarmPage = {navController.navigate(route = "AlarmPage")}
                        )

                        // 메인(캘린더) 화면
                        composable("main") {
                            MainScreen(navController, userViewModel,
                                goToAlarmPage = {navController.navigate(route = "AlarmPage")},
                                setRecipeState = {recipeState, selectedDate -> userViewModel.setRecipeState(recipeState, selectedDate)})
                        }
                    }

                    composable(route = "AlarmPage"){
                        AlarmPage(
                            alarmViewModel = alarmViewModel,
                            context = context,
                            alarmDataToState = { alarmId -> alarmViewModel.alarmDataToState(alarmId) },
                            removeAlarm = { context,alarmId -> alarmViewModel.removeAlarm(context, alarmId) },
                            alarmSwitch = { context, alarmId -> alarmViewModel.alarmSwitch(context, alarmId) },
                            returnToTodoListPage = {navController.navigate(route = "TodoListPage")},
                            goToAlarmSettingPage = {navController.navigate(route = "AlarmSettingPage") }
                        )
                    }

                    composable(route = "AlarmSettingPage"){
                        AlarmSettingPage(
                            context= context,
                            alarmViewModel= alarmViewModel,
                            returnToAlarmPage={navController.navigateUp()},
                            saveAlarm={context -> alarmViewModel.saveAlarm(context)},
                            updateAlarm={context -> alarmViewModel.updateAlarm(context)},
                            resetState = {alarmViewModel.resetState()}
                        )
                    }

                    // 레시피 관리 화면
                    composable("recipe") {
                        RecipeManagementScreen(navController, userViewModel, null)
                    }
                    // 레시피 관리 화면 2
                    // LocalDate.parse(it)를 사용하여 수동으로 다시 String에서 LocalDate로 변환
                    composable(
                        "recipe/{selectedDate}",
                    ) { backStackEntry ->
                        val selectedDate = backStackEntry.arguments?.getString("selectedDate")?.let { LocalDate.parse(it) }
                        RecipeManagementScreen(navController, userViewModel, selectedDate)
                    }
                }
            }
        }
    }
}
package com.example.myrecipeplanner.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.mobileappproject.states.RecipeState
import com.example.mobileappproject.viewmodels.UserViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeManagementScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    selectedDate: LocalDate?
) {
    var recipeName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }

    var showDialog by rememberSaveable { mutableStateOf(false) }

    val loggedInUserState by userViewModel.userStateFlow.collectAsState()

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "레시피 관리") },

                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedDate == null) {
                FloatingActionButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "레시피 추가")
                }
            }
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn {
                items(loggedInUserState.recipeList) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        // RecipeItem을 클릭했을 때, 쇼핑 리스트에 추가
                        onClick = {
                            selectedDate?.let {
                                val success = userViewModel.addItemToShoppingList(it, recipe)
                                Toast.makeText(
                                    context,
                                    if (success) "레시피가 쇼핑 리스트에 추가되었습니다." else "이미 추가된 레시피입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            if (showDialog) {
                RecipeInputDialog(
                    recipeName = recipeName,
                    ingredients = ingredients,
                    method = method,
                    onRecipeNameChange = { recipeName = it },
                    onIngredientsChange = { ingredients = it },
                    onMethodChange = { method = it },
                    onSubmit = {
                        val newRecipe = RecipeState(
                            userNickname = loggedInUserState.nickname,
                            name = recipeName,
                            ingredients = ingredients.split("\n").map { it.trim() },
                            method = method.split("\n").map { it.trim() },
                            isBookMarked = false
                        )
                        if (userViewModel.addRecipe(newRecipe)) {
                            recipeName = ""
                            ingredients = ""
                            method = ""
                            showDialog = false
                        } else {
                            Toast.makeText(context, "레시피가 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancel = {
                        recipeName = ""
                        ingredients = ""
                        method = ""
                        showDialog = false
                    },
                    isSubmitEnabled = recipeName.isNotBlank() && ingredients.isNotBlank() && method.isNotBlank()
                )
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: RecipeState, onClick: () -> Unit) {
    Button(
        onClick = onClick
    ) {
        Text(text = recipe.name)
    }
}

@Composable
fun RecipeInputDialog(
    recipeName: String,
    ingredients: String,
    method: String,
    onRecipeNameChange: (String) -> Unit,
    onIngredientsChange: (String) -> Unit,
    onMethodChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    isSubmitEnabled: Boolean
) {
    Dialog(onDismissRequest = { onCancel() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 레시피 이름 입력
                OutlinedTextField(
                    value = recipeName,
                    label = { Text("레시피 이름을 입력하세요") },
                    onValueChange = { onRecipeNameChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // 재료 입력
                OutlinedTextField(
                    value = ingredients,
                    label = { Text("재료를 입력하세요 (줄바꿈으로 구분)") },
                    onValueChange = { onIngredientsChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // 방법 입력
                OutlinedTextField(
                    value = method,
                    label = { Text("조리 방법을 입력하세요 (줄바꿈으로 구분)") },
                    onValueChange = { onMethodChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row {
                    Button(
                        onClick = { onSubmit() },
                        enabled = isSubmitEnabled
                    ) {
                        Text(text = "완료")
                    }

                    Spacer(modifier = Modifier.padding(8.dp))

                    Button(onClick = { onCancel() }) {
                        Text(text = "취소")
                    }
                }
            }
        }
    }
}
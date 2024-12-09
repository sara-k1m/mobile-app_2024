package com.example.recipeapp.ui

import AddRecipeScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileappproject.states.RecipeState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeManagementScreen(
    userNickname: String,
    onBack: () -> Unit
) {
    val database = Firebase.database.reference
    val categoriesState = remember { mutableStateListOf<String>() }
    val recipesState = remember { mutableStateListOf<RecipeState>() }
    var filteredRecipes by remember { mutableStateOf<List<RecipeState>>(emptyList()) }
    var currentPage by remember { mutableStateOf("menu") }
    val pageStack = remember { mutableStateListOf<String>() } // 페이지 스택
    var isSearchDialogOpen by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<RecipeState?>(null) }

    // Load categories
    LaunchedEffect(userNickname) {
        database.child("users").child(userNickname).child("categories")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesState.clear()
                    snapshot.children.forEach { categorySnapshot ->
                        val categoryName = categorySnapshot.key ?: return@forEach
                        categoriesState.add(categoryName)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
    }

    // Load recipes
    LaunchedEffect(userNickname) {
        database.child("users").child(userNickname).child("categories")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipesState.clear()
                    snapshot.children.forEach { categorySnapshot ->
                        categorySnapshot.children.forEach { recipeSnapshot ->
                            try {
                                if (recipeSnapshot.value !is Boolean) {
                                    val recipe = recipeSnapshot.getValue(RecipeState::class.java)
                                    recipe?.let { recipesState.add(it) }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
    }

    // 페이지 이동 함수
    fun navigateTo(page: String) {
        pageStack.add(currentPage) // 현재 페이지를 스택에 저장
        currentPage = page
    }

    // "돌아가기" 처리 함수
    fun navigateBack() {
        if (pageStack.isNotEmpty()) {
            currentPage = pageStack.removeAt(pageStack.size - 1) // 스택에서 이전 페이지로 이동
        } else {
            onBack() // 스택이 비어 있으면 앱 종료
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (currentPage) {
            "menu" -> RecipeMenuScreen(
                userNickname = userNickname,
                categories = categoriesState,
                onNavigateToAdd = { navigateTo("Add") },
                onNavigateToBookMark = { navigateTo("bookmarks") },
                onCategorySelected = { category ->
                    selectedCategory = category
                    filteredRecipes = recipesState.filter { it.category.contains(selectedCategory) }
                    navigateTo("category")
                },
                onSearch = { navigateTo("search") },
                modifier = Modifier.padding(innerPadding)
            )

            "Add" -> AddRecipeScreen(
                userNickname = userNickname,
                categories = categoriesState,
                returnToHome = { navigateBack() },
                modifier = Modifier.padding(innerPadding)
            )

            "category" -> RecipeListScreenByCategory(
                category = selectedCategory,
                recipes = filteredRecipes,
                onRecipeClick = { recipe ->
                    selectedRecipe = recipe
                    navigateTo("details")
                },
                onBack = { navigateBack() },
                modifier = Modifier.padding(innerPadding)
            )

            "details" -> selectedRecipe?.let { recipe ->
                ShowRecipe(
                    recipe = recipe,
                    onBack = { navigateBack() }
                )
            }

            "bookmarks" -> FavoritesScreen(
                favoriteRecipes = recipesState.filter { it.bookMarked },
                onRecipeClick = { recipe ->
                    selectedRecipe = recipe
                    navigateTo("details")
                },
                returnToHome = { navigateBack() }
            )

            "search" -> SearchRecipeDialog(
                isDialogOpen = true,
                onDismiss = { navigateBack() },
                recipes = recipesState,
                onShowRecipe = { recipe ->
                    selectedRecipe = recipe
                    navigateTo("details")
                }
            )
        }

        if (isSearchDialogOpen) {
            SearchRecipeDialog(
                isDialogOpen = isSearchDialogOpen,
                onDismiss = { isSearchDialogOpen = false },
                recipes = recipesState,
                onShowRecipe = { recipe ->
                    selectedRecipe = recipe
                    isSearchDialogOpen = false // 다이얼로그 닫기
                    navigateTo("details") // 세부 페이지로 이동
                }
            )
        }
    }
}

@Composable
fun RecipeMenuScreen(
    userNickname: String,
    categories: List<String>,
    onNavigateToAdd: () -> Unit,
    onNavigateToBookMark: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val database = Firebase.database.reference
    var isDialogOpen by remember { mutableStateOf(false) }
    var newCategory by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 카테고리 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("카테고리", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // 카테고리 버튼 (한 줄에 3개씩 표시)
            categories.chunked(3).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { category ->
                        Button(
                            onClick = { onCategorySelected(category) },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(text = category)
                        }
                    }
                }
            }
        }

        // 추가 버튼 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSearch,
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray, // 배경색: 다크 그레이
                        contentColor = Color.White // 텍스트 색상: 흰색
                    )
                ) {
                    Text("검색")
                }
                Button(
                    onClick = onNavigateToBookMark,
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray, // 배경색: 다크 그레이
                        contentColor = Color.White // 텍스트 색상: 흰색
                    )
                ) {
                    Text("즐겨찾기")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isDialogOpen = true },
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray, // 배경색: 다크 그레이
                        contentColor = Color.White // 텍스트 색상: 흰색
                    )
                ) {
                    Text("카테고리 추가")
                }
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier.weight(1f).height(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray, // 배경색: 다크 그레이
                        contentColor = Color.White // 텍스트 색상: 흰색
                    )
                ) {
                    Text("레시피 추가")
                }
            }
        }
    }

    // 카테고리 추가 다이얼로그
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text("카테고리 추가") },
            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("카테고리 이름") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategory.isNotBlank() && newCategory !in categories) {
                        database.child("users").child(userNickname).child("categories")
                            .child(newCategory).setValue(true).addOnSuccessListener {
                                newCategory = ""
                                isDialogOpen = false
                            }
                    }
                }) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun SearchRecipeDialog(
    isDialogOpen: Boolean,
    onDismiss: () -> Unit,
    recipes: List<RecipeState>,
    onShowRecipe: (RecipeState) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RecipeState>>(emptyList()) }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("레시피 검색") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchResults = if (searchQuery.isBlank()) {
                                emptyList() // 검색어가 비어 있으면 결과 없음
                            } else {
                                recipes.filter { recipe ->
                                    recipe.name.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        },
                        label = { Text("레시피 이름 입력") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                        Column {
                            searchResults.forEach { recipe ->
                                OutlinedButton(
                                    onClick = {
                                        onShowRecipe(recipe) // 세부 정보 표시
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(recipe.name.ifEmpty { "이름 없음" })
                                }
                            }
                        }
                    } else if (searchQuery.isNotBlank()) {
                        Text(
                            text = "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
            }
        )
    }
}

@Composable
fun ShowRecipe(recipe: RecipeState, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp) ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = recipe.name.ifEmpty { "이름 없음" },
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "재료: ${
                if (recipe.ingredients.isNotEmpty())
                    recipe.ingredients.joinToString(", ")
                else
                    "재료 없음"
            }"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "방법:\n${
                if (recipe.method.isNotEmpty())
                    recipe.method.joinToString("\n")
                else
                    "조리 방법 없음"
            }"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("돌아가기") }
    }
}


@Composable
fun FavoritesScreen(
    favoriteRecipes: List<RecipeState>,
    onRecipeClick: (RecipeState) -> Unit,
    returnToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp) // 전체 화면에 적용될 여백
    ) {
        Spacer(modifier = Modifier.height(64.dp)) // 텍스트 위쪽에 여백 추가

        Text(
            text = "즐겨찾기 레시피",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (favoriteRecipes.isNotEmpty()) {
            favoriteRecipes.forEach { recipe ->
                OutlinedButton( // OutlinedButton으로 테두리 스타일 적용
                    onClick = { onRecipeClick(recipe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface, // 버튼 채우기 색상: 흰색
                        contentColor = MaterialTheme.colorScheme.primary // 텍스트 색상: 기본 색상
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // 테두리 색상
                ) {
                    Text(recipe.name.takeIf { it.isNotBlank() } ?: "이름 없음")
                }
            }
        } else {
            Text(
                text = "즐겨찾기된 레시피가 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = returnToHome,
        ) {
            Text("돌아가기")
        }
    }
}




@Composable
fun RecipeListScreenByCategory(
    category: String,
    recipes: List<RecipeState>,
    onRecipeClick: (RecipeState) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("${category}의 레시피 목록", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (recipes.isNotEmpty()) {
            recipes.forEach { recipe ->
                Button(
                    onClick = { onRecipeClick(recipe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(recipe.name.ifEmpty { "이름 없음" })
                }
            }
        } else {
            Text(
                text = "이 카테고리에 레시피가 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("돌아가기") }
    }
}
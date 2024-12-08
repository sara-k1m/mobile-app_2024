package com.example.recipeapp.ui

import AddRecipeScreen
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
    var isSearchDialogOpen by remember { mutableStateOf(false) }

    // Load categories and recipes from Firebase Realtime Database
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

        database.child("users").child(userNickname).child("recipes")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipesState.clear()
                    snapshot.children.forEach { recipeSnapshot ->
                        val recipe = recipeSnapshot.getValue(RecipeState::class.java)
                        recipe?.let { recipesState.add(it) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (currentPage) {
            "menu" -> RecipeMenuScreen(
                userNickname = userNickname,
                categories = categoriesState,
                onNavigateToAdd = { currentPage = "Add" },
                onNavigateToBookMark = { currentPage = "bookmarks" }, // 즐겨찾기 이동 처리
                onCategorySelected = { category ->
                    filteredRecipes = recipesState.filter { it.category.contains(category) }
                    currentPage = "category"
                },
                onSearch = { isSearchDialogOpen = true },
                modifier = Modifier.padding(innerPadding)
            )

            "Add" -> AddRecipeScreen(
                userNickname = userNickname,
                categories = categoriesState,
                returnToHome = { currentPage = "menu" },
                modifier = Modifier.padding(innerPadding)
            )

            "category" -> RecipeListScreenByCategory(
                recipes = filteredRecipes,
                onRecipeClick = { recipe ->
                    // Handle recipe click (optional, navigate or show details)
                },
                onBack = { currentPage = "menu" },
                onNavigateToAdd = { currentPage = "Add" } // 수정: onNavigateToAdd 전달
            )

            "bookmarks" -> FavoritesScreen(
                favoriteRecipes = recipesState.filter { it.isBookMarked }, // isBookmarked 필터링
                onRecipeClick = { recipe ->
                    // 클릭 시 상세 화면으로 이동하거나 다른 동작 처리 가능
                },
                returnToHome = { currentPage = "menu" }
            )
        }

        if (isSearchDialogOpen) {
            SearchRecipeDialog(
                isDialogOpen = isSearchDialogOpen,
                onDismiss = { isSearchDialogOpen = false },
                recipes = recipesState,
                onShowRecipe = {
                    filteredRecipes = listOf(it)
                    currentPage = "category"
                },
                onShowResults = { results ->
                    filteredRecipes = results
                    currentPage = "category"
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSearch,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Text("검색")
                }
                Button(
                    onClick = onNavigateToBookMark,
                    modifier = Modifier.weight(1f).height(100.dp)
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
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Text("카테고리 추가")
                }
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier.weight(1f).height(100.dp)
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
    onShowRecipe: (RecipeState) -> Unit,
    onShowResults: (List<RecipeState>) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RecipeState>?>(null) }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("레시피 검색") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("레시피 이름") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    searchResults?.let { results ->
                        if (results.isNotEmpty()) {
                            Text(
                                text = "결과: ${results.size}개 찾았습니다.",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            Text(
                                text = "결과가 없습니다.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val results = recipes.filter { recipe ->
                            recipe.name.contains(searchQuery, ignoreCase = true)
                        }
                        searchResults = results
                        when {
                            results.isEmpty() -> { /* 검색 결과 없음 처리 */ }
                            results.size == 1 -> {
                                onDismiss()
                                onShowRecipe(results.first())
                            }
                            results.size > 1 -> {
                                onDismiss()
                                onShowResults(results)
                            }
                        }
                    }
                ) {
                    Text("검색")
                }
            },
            dismissButton = {
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
            .padding(16.dp)
            .padding(top = 96.dp)
    ) {
        Text(text = recipe.name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "재료: ${recipe.ingredients.joinToString(", ")}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "방법: ${recipe.method.joinToString(", ")}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("돌아가기")
        }
    }
}

@Composable
fun RecipeListScreenByCategory(
    recipes: List<RecipeState>,
    onRecipeClick: (RecipeState) -> Unit,
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 96.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "레시피 목록", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onNavigateToAdd ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Recipe to Category")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        recipes.forEach { recipe ->
            Button(
                onClick = { onRecipeClick(recipe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(recipe.name)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("돌아가기")
        }
    }
}

@Composable
fun RecipeListScreen(
    recipes: List<RecipeState>,
    onRecipeClick: (RecipeState) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 96.dp)
    ) {
        Text(text = "레시피 목록", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        recipes.forEach { recipe ->
            Button(
                onClick = { onRecipeClick(recipe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(recipe.name)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("돌아가기")
        }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 64.dp)
    ) {
        Text(
            text = "즐겨찾기 레시피",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favoriteRecipes.isEmpty()) {
            Text("즐겨찾기한 레시피가 없습니다.", style = MaterialTheme.typography.bodyLarge)
        } else {
            favoriteRecipes.forEach { recipe ->
                RecipeItem(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = returnToHome, modifier = Modifier.align(Alignment.End)) {
            Text("홈으로 돌아가기")
        }
    }
}

@Composable
fun RecipeItem(
    recipe: RecipeState,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
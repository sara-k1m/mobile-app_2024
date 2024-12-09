import androidx.compose.foundation.clickable
import com.example.mobileappproject.states.RecipeState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun AddRecipeScreen(
    userNickname: String,
    categories: List<String>,
    returnToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val database = Firebase.database.reference
    var recipeName by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var bookMarked by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("레시피 이름", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = recipeName,
            onValueChange = { recipeName = it },
            label = { Text("레시피 이름") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("재료 (쉼표로 구분)", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("예: 밀가루, 설탕, 달걀") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("조리 방법 (줄바꿈으로 구분)", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = method,
            onValueChange = { method = it },
            label = { Text("예: 1. 재료 섞기\n2. 반죽하기\n3. 굽기") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 카테고리 선택 버튼
        Button(
            onClick = { isDialogOpen = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // 옅은 회색 배경색
                contentColor = MaterialTheme.colorScheme.primary // 텍스트 색상: 파랑
            )
        ) {
            Text(text = selectedCategory ?: "카테고리 선택")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 즐겨찾기 체크박스
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = bookMarked,
                onCheckedChange = { bookMarked = it }
            )
            Text("즐겨찾기에 추가", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 저장 버튼
        Button(
            onClick = {
                if (recipeName.isNotBlank() && selectedCategory != null) {
                    val recipe = RecipeState(
                        userNickname = userNickname,
                        name = recipeName,
                        ingredients = ingredients.split(",").map { it.trim() },
                        method = method.split("\n").map { it.trim() },
                        category = listOf(selectedCategory!!),
                        bookMarked = bookMarked // 즐겨찾기 여부 저장
                    )
                    saveRecipeToFirebaseDatabase(
                        userNickname = userNickname,
                        category = selectedCategory!!,
                        recipe = recipe,
                        onComplete = returnToHome
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }

    // 다이얼로그: 카테고리 선택
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text("카테고리를 선택하세요") },
            text = {
                Column {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = category
                                    isDialogOpen = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    isDialogOpen = false
                                }
                            )
                            Text(
                                text = category,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text("Close")
                }
            }
        )
    }
}

fun saveRecipeToFirebaseDatabase(
    userNickname: String,
    category: String,
    recipe: RecipeState,
    onComplete: () -> Unit
) {
    val database = Firebase.database
    val recipesRef = database.reference
        .child("users")
        .child(userNickname)
        .child("categories")
        .child(category)

    recipesRef.push().setValue(recipe)
        .addOnSuccessListener {
            onComplete() // 저장 성공 시 호출
        }
        .addOnFailureListener { e ->
            e.printStackTrace() // 오류 로그 출력
        }
}

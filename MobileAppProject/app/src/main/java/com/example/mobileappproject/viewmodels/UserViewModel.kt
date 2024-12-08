package com.example.mobileappproject.viewmodels


import androidx.lifecycle.ViewModel
import com.example.mobileappproject.states.RecipeAlarmState
import com.example.mobileappproject.states.RecipeState
import com.example.mobileappproject.states.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class UserViewModel: ViewModel() {
    // 현재 로그인한 유저를 추적
    private val _userStateFlow = MutableStateFlow(UserState())
    val userStateFlow = _userStateFlow.asStateFlow()

    // 회원가입한 유저들을 저장해둔 임시 저장소
    private val _userMapStateFlow = MutableStateFlow<Map<String, UserState>>(emptyMap())
    val userMapStateFlow = _userMapStateFlow.asStateFlow()

    private var _recipeAlarmState = MutableStateFlow(RecipeAlarmState())
    val recipeAlarmState = _recipeAlarmState.asStateFlow()

    init {
        initializeTestUsers()
    }

    // 테스트용 유저를 미리 회원가입시킨다.
    private fun initializeTestUsers() {
        val initialUsers = mapOf(
            "test" to UserState(nickname = "test", password = "test", shoppingToDoMap = mutableMapOf()),
            "test2" to UserState(nickname = "test2", password = "test2", shoppingToDoMap = mutableMapOf())
        )
        _userMapStateFlow.value = initialUsers
    }

    // 회원가입
    fun registerUser(nickname: String, password: String): Boolean {
        if (!_userMapStateFlow.value.containsKey(nickname)) {
            val newUser = UserState(nickname = nickname, password = password, shoppingToDoMap = mutableMapOf())
            _userMapStateFlow.value += (nickname to newUser)
            return true
        }
        return false
    }

    // 로그인
    fun loginUser(nickname: String, password: String): Boolean {
        val user = _userMapStateFlow.value[nickname]
        if (user != null && user.password == password) {
            _userStateFlow.update { user.copy(password = password) }
            return true
        }
        return false
    }

    // 로그아웃
    fun logoutUser() {
        _userStateFlow.update { UserState() }
    }

    // 레시피 추가
    fun addRecipe(recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedList = currentState.recipeList
        // 이미 추가된 레시피인지 체크 (이름이 같은 레시피가 있는지)
        if (updatedList.any { it.name == recipe.name }) {
            return false
        }

        updatedList.add(recipe)
        _userStateFlow.update { currentState.copy(recipeList = updatedList) }
        return true
    }

    // 지정한 날짜에 쇼핑 리스트 추가 (레시피 목록에 있는 레시피만 추가 가능)
    fun addItemToShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        // recipeList에 있는 레시피만 추가할 수 있도록 체크
        if (!currentState.recipeList.any { it.name == recipe.name }) {
            // 레시피 목록에 없는 레시피라면 추가할 수 없음
            return false
        }

        val currentList = updatedMap[date] ?: mutableListOf()

        // 이미 추가된 레시피인지 체크 (이름이 같은 레시피가 있는지)
        if (currentList.any { it.name == recipe.name }) {
            return false
        }

        // 레시피가 없다면 추가
        currentList.add(recipe)
        updatedMap[date] = currentList // 수정된 리스트 업데이트
        _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

        return true
    }

    // 쇼핑 리스트에서 레시피 제거
    fun removeItemFromShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        val currentList = updatedMap[date]
        if (currentList != null && currentList.any { it.name == recipe.name }) {
            val updatedList = currentList.filter { it.name != recipe.name }
            updatedMap[date] = updatedList.toMutableList()

            _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

            return true
        }
        return false
    }

    fun setRecipeState(recipe: RecipeState, selectedDate: LocalDate){
        _recipeAlarmState.update { currentState ->
            currentState.copy(
                recipeName = recipe.name,
                localDate = selectedDate
            )

        }
    }
}
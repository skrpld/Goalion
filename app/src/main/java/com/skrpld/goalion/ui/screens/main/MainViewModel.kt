package com.skrpld.goalion.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.data.database.AppDao
import com.skrpld.goalion.data.database.TaskPriority
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val profile: Profile = Profile(name = "Loading..."),
    val goals: List<GoalWithTasks> = emptyList(),
    // ID элементов, которые нужно перевести в режим редактирования сразу после загрузки списка
    val goalIdToEdit: Int? = null,
    val taskIdToEdit: Int? = null
)

class MainViewModel(
    private val dao: AppDao // Передаем DAO в конструктор
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Текущий ID профиля (в реальном приложении берется из Datastore или Auth)
    private var currentProfileId: Int = 0

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            var profile = dao.getAnyProfile()
            if (profile == null) {
                val newId = dao.insertProfile(Profile(name = "User"))
                profile = Profile(newId.toInt(), "User")
            }
            currentProfileId = profile.id

            _uiState.update { it.copy(profile = profile) }

            dao.getGoalsWithTasksList(currentProfileId).collect { goalsList ->
                _uiState.update { it.copy(goals = goalsList) }
            }
        }
    }

    fun onAddGoalClick() {
        viewModelScope.launch {
            // Создаем пустую цель
            val newGoal = Goal(title = "", profileId = currentProfileId)
            val newId = dao.insertGoal(newGoal).toInt()

            // Указываем UI, что эту цель нужно сразу начать редактировать
            _uiState.update { it.copy(goalIdToEdit = newId) }
        }
    }

    fun onGoalTitleChanged(goal: Goal, newTitle: String) {
        if (goal.title == newTitle) return // Избегаем лишних записей
        viewModelScope.launch {
            dao.updateGoal(goal.copy(title = newTitle))
        }
    }

    // Сбрасываем флаг редактирования, когда UI "подхватил" фокус
    fun onGoalEditStarted() {
        _uiState.update { it.copy(goalIdToEdit = null) }
    }

    fun onAddTaskClick(goalId: Int) {
        viewModelScope.launch {
            val newTask = Task(
                title = "",
                description = "",
                status = TaskStatus.TODO,
                priority = TaskPriority.MEDIUM,
                goalId = goalId
            )
            val newId = dao.insertTask(newTask).toInt()

            // Указываем UI, что эту задачу нужно редактировать, и нужно раскрыть список (если свернут)
            // Логику раскрытия списка лучше делать в UI, но здесь мы триггерим эдит
            _uiState.update { it.copy(taskIdToEdit = newId) }
        }
    }

    fun onTaskTitleChanged(task: Task, newTitle: String) {
        if (task.title == newTitle) return
        viewModelScope.launch {
            dao.updateTask(task.copy(title = newTitle))
        }
    }

    fun onTaskEditStarted() {
        _uiState.update { it.copy(taskIdToEdit = null) }
    }

    fun onTaskStatusChange(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            dao.updateTaskStatus(task.id, newStatus)
        }
    }

    fun onProfileClick() {
        // Логика профиля
    }
}

class MainViewModelFactory(private val dao: AppDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
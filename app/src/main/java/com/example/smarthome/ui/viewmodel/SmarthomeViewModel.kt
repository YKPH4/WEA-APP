package com.example.smarthome.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.models.RegistroActividad
import com.example.smarthome.data.models.UsuarioApp
import com.example.smarthome.data.repository.SmarthomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmarthomeViewModel(
    private val repository: SmarthomeRepository = SmarthomeRepository()
) : ViewModel() {

    private val _isDoorLocked = MutableStateFlow(true)
    val isDoorLocked: StateFlow<Boolean> = _isDoorLocked.asStateFlow()

    private val _usersList = MutableStateFlow<List<UsuarioApp>>(emptyList())
    val usersList: StateFlow<List<UsuarioApp>> = _usersList.asStateFlow()

    private val _activitiesList = MutableStateFlow<List<RegistroActividad>>(emptyList())
    val activitiesList: StateFlow<List<RegistroActividad>> = _activitiesList.asStateFlow()

    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            // Restore User Preferences from Supabase
            val prefs = repository.getUserPreferences()
            if (prefs != null) {
                _isDoorLocked.value = prefs.lastLockStatus
            } else {
                val dispositivo = repository.getDispositivo("Puerta principal")
                if (dispositivo != null) {
                    _isDoorLocked.value = dispositivo.isLocked
                }
            }

            // Fetch User's Isolated Data from Supabase
            fetchUsers()
            fetchActivities()
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            val remoteUsers = repository.getUsuarios()
            _usersList.value = remoteUsers
        }
    }

    fun fetchActivities() {
        viewModelScope.launch {
            val remoteActivities = repository.getActividades()
            _activitiesList.value = remoteActivities
        }
    }

    fun toggleLock() {
        val previousState = _isDoorLocked.value
        val newState = !previousState
        _isDoorLocked.value = newState

        viewModelScope.launch {
            repository.toggleLockStatus("Puerta principal", previousState)
            fetchActivities()
        }
    }

    fun updatePasscode(newPasscode: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePasscode("Puerta principal", newPasscode)
            onComplete()
        }
    }

    fun addUser(nombre: String, email: String, rol: String = "Invitado", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.addUsuario(nombre, email, rol)
            if (result.isSuccess) {
                fetchUsers()
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Error al conectar con Supabase"
                errorMessage = "Error de Supabase al agregar usuario:\n\n$err"
            }
        }
    }

    fun deleteUser(user: UsuarioApp) {
        viewModelScope.launch {
            val result = repository.deleteUsuario(user)
            if (result.isSuccess) {
                fetchUsers()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Error al eliminar en Supabase"
                errorMessage = "Error de Supabase al eliminar usuario:\n\n$err"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}

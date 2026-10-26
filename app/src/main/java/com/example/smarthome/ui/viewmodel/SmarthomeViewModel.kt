package com.example.smarthome.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.models.RegistroActividad
import com.example.smarthome.data.models.PerfilRostro
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

    private val _perfilesList = MutableStateFlow<List<PerfilRostro>>(emptyList())
    val perfilesList: StateFlow<List<PerfilRostro>> = _perfilesList.asStateFlow()

    private val _activitiesList = MutableStateFlow<List<RegistroActividad>>(emptyList())
    val activitiesList: StateFlow<List<RegistroActividad>> = _activitiesList.asStateFlow()

    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            // Se toma el estado solo desde getDispositivo
            val dispositivo = repository.getDispositivo("Puerta principal")
            if (dispositivo != null) {
                _isDoorLocked.value = dispositivo.isLocked
            }

            fetchPerfiles()
            fetchActivities()
        }
    }

    fun fetchPerfiles() {
        viewModelScope.launch {
            val remotePerfiles = repository.getPerfiles()
            _perfilesList.value = remotePerfiles
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

        viewModelScope.launch {
            // El estado final de la UI lo dicta la respuesta del repositorio
            val newState = repository.toggleLockStatus("Puerta principal", previousState)
            _isDoorLocked.value = newState
            fetchActivities()
        }
    }

    fun updatePasscode(newPasscode: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePasscode("Puerta principal", newPasscode)
            onComplete()
        }
    }

    fun crearPerfil(nombre: String, consentimiento: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.crearPerfil(nombre, consentimiento)
            if (result.isSuccess) {
                fetchPerfiles()
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Error al conectar con Supabase"
                errorMessage = "Error de Supabase al agregar perfil:\n\n$err"
            }
        }
    }

    fun eliminarPerfil(perfil: PerfilRostro) {
        viewModelScope.launch {
            val result = repository.eliminarPerfil(perfil)
            if (result.isSuccess) {
                fetchPerfiles()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Error al eliminar en Supabase"
                errorMessage = "Error de Supabase al eliminar perfil:\n\n$err"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
package com.example.smarthome.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dispositivo(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val nombre: String,
    val estado: String,
    @SerialName("is_locked")
    val isLocked: Boolean = true,
    @SerialName("clave_acceso")
    val claveAcceso: String = "1234"
)

@Serializable
data class InsertDispositivoDto(
    val nombre: String,
    val estado: String,
    @SerialName("is_locked")
    val isLocked: Boolean = true,
    @SerialName("clave_acceso")
    val claveAcceso: String = "1234"
)

@Serializable
data class RegistroActividad(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val titulo: String,
    val estado: String,
    val hora: String,
    val fecha: String
)

@Serializable
data class InsertRegistroActividadDto(
    val titulo: String,
    val estado: String,
    val hora: String,
    val fecha: String
)

@Serializable
data class UsuarioApp(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val nombre: String,
    val email: String,
    val rol: String = "Invitado"
)

@Serializable
data class InsertUsuarioDto(
    val nombre: String,
    val email: String,
    val rol: String = "Invitado"
)

@Serializable
data class UserPreferences(
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("last_lock_status")
    val lastLockStatus: Boolean = true,
    val passcode: String = "1234",
    @SerialName("selected_tab")
    val selectedTab: Int = 0
)

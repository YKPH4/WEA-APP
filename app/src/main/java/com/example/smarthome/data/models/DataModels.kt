package com.example.smarthome.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dispositivo(
    val id: String? = null,
    val nombre: String,
    val estado: String,
    @SerialName("is_locked")
    val isLocked: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class InsertDispositivoDto(
    val nombre: String,
    val estado: String,
    @SerialName("is_locked")
    val isLocked: Boolean = true
)

@Serializable
data class RegistroActividad(
    val id: String? = null,
    @SerialName("dispositivo_id")
    val dispositivoId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("perfil_id")
    val perfilId: String? = null,
    val titulo: String,
    val estado: String,
    val resultado: String? = null,
    val confianza: Float? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    // La UI usaba hora y fecha por separado. Se derivan de created_at (UTC).
    val hora: String
        get() = createdAt?.takeIf { it.length >= 16 }?.substring(11, 16) ?: ""

    val fecha: String
        get() = createdAt?.takeIf { it.length >= 10 }?.substring(0, 10) ?: ""
}

@Serializable
data class InsertRegistroActividadDto(
    @SerialName("dispositivo_id")
    val dispositivoId: String,
    @SerialName("user_id")
    val userId: String?,
    val titulo: String,
    val estado: String,
    val resultado: String = "manual"
)

@Serializable
data class PerfilRostro(
    val id: String? = null,
    val nombre: String,
    val etiqueta: Int? = null,
    val estado: String,
    @SerialName("consentimiento_at")
    val consentimientoAt: String? = null,
    @SerialName("fotos_path")
    val fotosPath: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class InsertPerfilRostroDto(
    val nombre: String,
    val estado: String = "pendiente_captura",
    @SerialName("consentimiento_at")
    val consentimientoAt: String
)
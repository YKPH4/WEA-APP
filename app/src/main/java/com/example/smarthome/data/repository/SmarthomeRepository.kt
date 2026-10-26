package com.example.smarthome.data.repository

import android.util.Log
import com.example.smarthome.data.models.Dispositivo
import com.example.smarthome.data.models.InsertDispositivoDto
import com.example.smarthome.data.models.InsertPerfilRostroDto
import com.example.smarthome.data.models.InsertRegistroActividadDto
import com.example.smarthome.data.models.PerfilRostro
import com.example.smarthome.data.models.RegistroActividad
import com.example.smarthome.data.supabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SmarthomeRepository {

    private val TAG = "SmarthomeRepository"
    private val NOMBRE_CERRADURA = "Puerta principal"

    fun getCurrentUserId(): String? = try {
        supabaseClient.auth.currentUserOrNull()?.id
    } catch (e: Exception) {
        null
    }

    // Solo cuentas con app_metadata.role = "admin" pueden gestionar el sistema.
    fun esAdmin(): Boolean {
        val user = supabaseClient.auth.currentUserOrNull() ?: return false
        return user.appMetadata?.get("role")?.jsonPrimitive?.contentOrNull == "admin"
    }

    private fun ahoraIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

    // AUTENTICACIÓN

    // El parámetro nombre ya no se guarda aquí. Puedes quitarlo cuando actualices RegisterScreen.
    suspend fun registerUser(nombre: String, email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            Log.d(TAG, "Cuenta creada: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear cuenta: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    // DISPOSITIVO / CERRADURA

    suspend fun seedInitialDispositivo(): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("dispositivos").insert(
                InsertDispositivoDto(
                    nombre = NOMBRE_CERRADURA,
                    estado = "Cerrada/Bloqueada",
                    isLocked = true
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear dispositivo: ${e.message}", e)
            false
        }
    }

    suspend fun getDispositivo(nombre: String = NOMBRE_CERRADURA): Dispositivo? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("dispositivos")
                .select { filter { eq("nombre", nombre) } }
                .decodeList<Dispositivo>()
                .firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivo: ${e.message}", e)
            null
        }
    }

    // Devuelve el estado que realmente quedó en la base.
    // Si la escritura falla, devuelve el estado anterior para que la pantalla no mienta.
    suspend fun toggleLockStatus(nombre: String, currentlyLocked: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!esAdmin()) {
            Log.w(TAG, "Intento de cambiar la cerradura sin permisos de administrador")
            return@withContext currentlyLocked
        }

        val newLocked = !currentlyLocked
        val newEstado = if (newLocked) "Cerrada/Bloqueada" else "Abierta/Desbloqueada"

        try {
            val dispositivo = getDispositivo(nombre)
                ?: throw IllegalStateException("No existe el dispositivo '$nombre'")
            val dispositivoId = dispositivo.id
                ?: throw IllegalStateException("El dispositivo no tiene id")

            supabaseClient.from("dispositivos").update({
                set("is_locked", newLocked)
                set("estado", newEstado)
            }) {
                filter { eq("id", dispositivoId) }
            }

            supabaseClient.from("actividades").insert(
                InsertRegistroActividadDto(
                    dispositivoId = dispositivoId,
                    userId = getCurrentUserId(),
                    titulo = nombre,
                    estado = "Estado:$newEstado",
                    resultado = "manual"
                )
            )

            Log.d(TAG, "Cerradura actualizada: isLocked=$newLocked")
            newLocked
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar la cerradura en Supabase: ${e.message}", e)
            currentlyLocked
        }
    }

    // PIN: se guarda como hash en la base mediante la función set_pin()
    suspend fun updatePasscode(nombre: String, newPasscode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!esAdmin()) return@withContext false
            val dispositivoId = getDispositivo(nombre)?.id ?: return@withContext false

            supabaseClient.postgrest.rpc("set_pin", buildJsonObject {
                put("p_dispositivo", dispositivoId)
                put("p_pin", newPasscode)
            })

            Log.d(TAG, "PIN actualizado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar el PIN: ${e.message}", e)
            false
        }
    }

    // PERFILES DE ROSTRO (reemplaza a usuarios)

    suspend fun getPerfiles(): List<PerfilRostro> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("perfiles_rostro")
                .select { order("created_at", order = Order.DESCENDING) }
                .decodeList<PerfilRostro>()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener perfiles: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun crearPerfil(nombre: String, consentimiento: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(esAdmin()) { "Solo el administrador puede crear perfiles" }
            require(consentimiento) { "Falta el consentimiento de la persona" }
            require(nombre.isNotBlank()) { "El nombre es obligatorio" }

            supabaseClient.from("perfiles_rostro").insert(
                InsertPerfilRostroDto(
                    nombre = nombre.trim(),
                    consentimientoAt = ahoraIso()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Borra la fila. Las fotos del bucket las borra el servidor.
    suspend fun eliminarPerfil(perfil: PerfilRostro): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(esAdmin()) { "Solo el administrador puede eliminar perfiles" }
            val id = perfil.id ?: throw IllegalArgumentException("El perfil no tiene id")

            supabaseClient.from("perfiles_rostro").delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ACTIVIDADES

    suspend fun getActividades(): List<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("actividades")
                .select { order("created_at", order = Order.DESCENDING) }
                .decodeList<RegistroActividad>()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener actividades: ${e.message}", e)
            emptyList()
        }
    }
}
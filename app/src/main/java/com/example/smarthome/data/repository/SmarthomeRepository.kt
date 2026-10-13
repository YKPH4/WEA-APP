package com.example.smarthome.data.repository

import android.util.Log
import com.example.smarthome.data.models.Dispositivo
import com.example.smarthome.data.models.InsertDispositivoDto
import com.example.smarthome.data.models.InsertRegistroActividadDto
import com.example.smarthome.data.models.InsertUsuarioDto
import com.example.smarthome.data.models.RegistroActividad
import com.example.smarthome.data.models.UserPreferences
import com.example.smarthome.data.models.UsuarioApp
import com.example.smarthome.data.supabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmarthomeRepository {

    private val TAG = "SmarthomeRepository"

    fun getCurrentUserId(): String? {
        return try {
            supabaseClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun seedInitialDispositivo(): Boolean = withContext(Dispatchers.IO) {
        try {
            val disp = InsertDispositivoDto(
                nombre = "Puerta principal",
                estado = "Cerrada/Bloqueada",
                isLocked = true,
                claveAcceso = "1234"
            )
            supabaseClient.from("dispositivos").insert(disp)
            Log.d(TAG, "Dispositivo sembrado exitosamente en Supabase")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al sembrar dispositivo en Supabase: ${e.message}", e)
            false
        }
    }

    // AUTHENTICATION
    suspend fun registerUser(nombre: String, email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
            } catch (e: Exception) {
                Log.w(TAG, "Auth signUp omitido o ya existente: ${e.message}")
            }

            val currentId = getCurrentUserId()
            val dto = InsertUsuarioDto(nombre = nombre, email = email, rol = "Dueño")
            supabaseClient.from("usuarios").insert(dto)

            val initialPrefs = UserPreferences(userId = currentId, lastLockStatus = true, passcode = "1234", selectedTab = 0)
            saveUserPreferences(initialPrefs)

            Log.d(TAG, "Usuario registrado en Supabase: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar usuario en Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
            } catch (e: Exception) {
                Log.w(TAG, "Auth signIn omitido: ${e.message}")
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

    // USER PREFERENCES / STATE PERSISTENCE
    suspend fun getUserPreferences(): UserPreferences? = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.from("user_preferences").select().decodeList<UserPreferences>()
            list.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener preferencias de usuario: ${e.message}", e)
            null
        }
    }

    suspend fun saveUserPreferences(prefs: UserPreferences): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("user_preferences").upsert(prefs)
            Log.d(TAG, "Preferencias de usuario guardadas en Supabase: $prefs")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar preferencias de usuario: ${e.message}", e)
            false
        }
    }

    // DISPOSITIVOS / LOCK CONTROL
    suspend fun getDispositivo(nombre: String = "Puerta principal"): Dispositivo? = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.from("dispositivos")
                .select {
                    filter { eq("nombre", nombre) }
                }.decodeList<Dispositivo>()

            Log.d(TAG, "Dispositivo obtenido de Supabase: $list")
            list.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivo de Supabase: ${e.message}", e)
            null
        }
    }

    suspend fun toggleLockStatus(nombre: String, currentlyLocked: Boolean): Boolean = withContext(Dispatchers.IO) {
        val newLocked = !currentlyLocked
        val newEstado = if (newLocked) "Cerrada/Bloqueada" else "Abierta/Desbloqueada"

        try {
            supabaseClient.from("dispositivos").update({
                set("is_locked", newLocked)
                set("estado", newEstado)
            }) {
                filter { eq("nombre", nombre) }
            }

            // Record activity log event
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.ENGLISH)
            val now = Date()

            val logDto = InsertRegistroActividadDto(
                titulo = nombre,
                estado = "Estado:$newEstado",
                hora = timeFormat.format(now),
                fecha = dateFormat.format(now).uppercase()
            )
            supabaseClient.from("actividades").insert(logDto)
            Log.d(TAG, "Estado de cerradura actualizado en Supabase: isLocked=$newLocked")

            val currentPrefs = getUserPreferences() ?: UserPreferences(userId = getCurrentUserId())
            saveUserPreferences(currentPrefs.copy(lastLockStatus = newLocked))

            newLocked
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar estado de cerradura en Supabase: ${e.message}", e)
            newLocked
        }
    }

    suspend fun updatePasscode(nombre: String, newPasscode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("dispositivos").update({
                set("clave_acceso", newPasscode)
            }) {
                filter { eq("nombre", nombre) }
            }

            val currentPrefs = getUserPreferences() ?: UserPreferences(userId = getCurrentUserId())
            saveUserPreferences(currentPrefs.copy(passcode = newPasscode))

            Log.d(TAG, "Clave de acceso actualizada en Supabase")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar clave en Supabase: ${e.message}", e)
            false
        }
    }

    // USERS
    suspend fun getUsuarios(): List<UsuarioApp> = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.from("usuarios").select().decodeList<UsuarioApp>()
            Log.d(TAG, "Usuarios obtenidos de Supabase: count=${list.size}, list=$list")
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuarios de Supabase: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUsuario(nombre: String, email: String, rol: String = "Invitado"): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = InsertUsuarioDto(nombre = nombre, email = email, rol = rol)
            supabaseClient.from("usuarios").insert(dto)
            Log.d(TAG, "Usuario insertado exitosamente en Supabase: $nombre ($email)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al insertar usuario en Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUsuario(user: UsuarioApp): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("usuarios").delete {
                filter {
                    if (!user.id.isNullOrBlank()) {
                        eq("id", user.id)
                    } else if (user.email.isNotBlank()) {
                        eq("email", user.email)
                    } else {
                        eq("nombre", user.nombre)
                    }
                }
            }
            Log.d(TAG, "Usuario eliminado de Supabase: ${user.nombre}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar usuario en Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ACTIVITIES
    suspend fun getActividades(): List<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.from("actividades").select().decodeList<RegistroActividad>()
            Log.d(TAG, "Actividades obtenidas de Supabase: count=${list.size}")
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener actividades de Supabase: ${e.message}", e)
            emptyList()
        }
    }
}

package com.example.smarthome.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.android.Android

object SupabaseConfig {
    const val SUPABASE_URL = "https://nbumuudjzbtwujdxclex.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_HyvYV0qAD4Ny3v18t8n25w_RCjkVaGx"
}

val supabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.SUPABASE_URL,
    supabaseKey = SupabaseConfig.SUPABASE_KEY
) {
    httpEngine = Android.create()
    install(Postgrest)
    install(Auth)
    install(Realtime)
}

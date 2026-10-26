package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.repository.SmarthomeRepository
import com.example.smarthome.ui.LoginScreen
import com.example.smarthome.ui.RegisterScreen
import com.example.smarthome.ui.SmartHomeScreen
import com.example.smarthome.ui.theme.SmarthomeTheme
import com.example.smarthome.ui.viewmodel.SmarthomeViewModel

enum class Screen {
    Login,
    Register,
    Home
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarthomeTheme {
                val repository = remember { SmarthomeRepository() }

                // Si ya hay una sesión guardada, entra directo al panel.
                // Si no, muestra el login.
                var currentScreen by remember {
                    mutableStateOf(
                        if (repository.getCurrentUserId() != null) Screen.Home else Screen.Login
                    )
                }

                val smarthomeViewModel: SmarthomeViewModel = viewModel()

                Crossfade(
                    targetState = currentScreen,
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.Login -> {
                            LoginScreen(
                                onNavigateToRegister = { currentScreen = Screen.Register },
                                onLoginSuccess = { currentScreen = Screen.Home }
                            )
                        }
                        Screen.Register -> {
                            RegisterScreen(
                                onNavigateToLogin = { currentScreen = Screen.Login },
                                onRegisterSuccess = { currentScreen = Screen.Login }
                            )
                        }
                        Screen.Home -> {
                            SmartHomeScreen(
                                viewModel = smarthomeViewModel,
                                onLogout = { currentScreen = Screen.Login }
                            )
                        }
                    }
                }
            }
        }
    }
}
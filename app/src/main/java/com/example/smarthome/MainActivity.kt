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
import com.example.smarthome.ui.LoginScreen
import com.example.smarthome.ui.RegisterScreen
import com.example.smarthome.ui.SmartHomeScreen
import com.example.smarthome.ui.WelcomeScreen
import com.example.smarthome.ui.theme.SmarthomeTheme
import com.example.smarthome.ui.viewmodel.SmarthomeViewModel

enum class Screen {
    Welcome,
    Register,
    Login,
    Home
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarthomeTheme {
                var currentScreen by remember { mutableStateOf(Screen.Welcome) }
                val smarthomeViewModel: SmarthomeViewModel = viewModel()

                Crossfade(
                    targetState = currentScreen,
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.Welcome -> {
                            WelcomeScreen(
                                onNavigateToRegister = { currentScreen = Screen.Register },
                                onNavigateToLogin = { currentScreen = Screen.Login }
                            )
                        }
                        Screen.Register -> {
                            RegisterScreen(
                                onNavigateToLogin = { currentScreen = Screen.Login },
                                onRegisterSuccess = { currentScreen = Screen.Home },
                                onNavigateToWelcome = { currentScreen = Screen.Welcome }
                            )
                        }
                        Screen.Login -> {
                            LoginScreen(
                                onNavigateToRegister = { currentScreen = Screen.Register },
                                onLoginSuccess = { currentScreen = Screen.Home },
                                onNavigateToWelcome = { currentScreen = Screen.Welcome }
                            )
                        }
                        Screen.Home -> {
                            SmartHomeScreen(
                                viewModel = smarthomeViewModel,
                                onLogout = { currentScreen = Screen.Welcome }
                            )
                        }
                    }
                }
            }
        }
    }
}
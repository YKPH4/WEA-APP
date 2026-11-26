package com.example.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// App colors
val Burgundy: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF98353A) else Color(0xFF7A2024)

val ScreenBackground: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF121212) else Color(0xFFF4F4F5)

val CardBackground: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color(0xFFE8E8EA)

val TextDark: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF000000)

val TextGray: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFAAAAAA) else Color(0xFF7D7D82)

val TextSubDark: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFD3D3D3) else Color(0xFF2C2C2C)

val Subtext: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFD64449) else Color(0xFF7A2528)
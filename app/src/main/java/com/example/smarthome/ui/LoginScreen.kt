package com.example.smarthome.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.smarthome.data.repository.SmarthomeRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmarthomeTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header with Back Arrow and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = BackArrowIcon,
                        contentDescription = "Volver",
                        tint = Burgundy,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Iniciar sesión",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(end = 36.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Form Fields
            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(18.dp))

            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Links below fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Olvidaste la contraseña?",
                    fontSize = 13.sp,
                    color = TextSubDark,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* Handle forgot password */ }
                )

                Text(
                    text = "No tengo cuenta",
                    fontSize = 13.sp,
                    color = TextSubDark,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Terms notice at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aceptaste los ",
                    fontSize = 13.sp,
                    color = TextSubDark
                )
                Text(
                    text = "Politicas de Privacidad",
                    fontSize = 13.sp,
                    color = TextSubDark,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = " y ",
                    fontSize = 13.sp,
                    color = TextSubDark
                )
                Text(
                    text = "Terminos de Uso",
                    fontSize = 13.sp,
                    color = TextSubDark,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Iniciar" Button
            val scope = rememberCoroutineScope()
            val repository = remember { SmarthomeRepository() }

            Button(
                onClick = {
                    scope.launch {
                        if (email.isNotBlank()) {
                            repository.loginUser(email, password)
                        }
                        onLoginSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Iniciar",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    SmarthomeTheme {
        LoginScreen(
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}

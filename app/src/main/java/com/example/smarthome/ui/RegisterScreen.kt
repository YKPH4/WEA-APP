package com.example.smarthome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.painterResource
import com.example.smarthome.R
import com.example.smarthome.data.repository.SmarthomeRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmarthomeTheme
import com.example.smarthome.ui.theme.Subtext

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(true) }

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
                    onClick = onNavigateToWelcome,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevrolet),
                        contentDescription = "Volver",
                        tint = Burgundy,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Crear cuenta",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(end = 36.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Text Fields
            CustomOutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Nombre"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Crear contraseña",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                placeholder = "Repetir contraseña",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Escanear rostro Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* Handle Face Scan */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Escanear rostro",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Checkbox and Terms
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { acceptedTerms = !acceptedTerms }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Aceptar",
                    tint = Burgundy,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row {
                    Text(
                        text = "Aceptar ",
                        fontSize = 10.sp,
                        color = Subtext
                    )
                    Text(
                        text = "Politicas de Privacidad",
                        fontSize = 10.sp,
                        color = Subtext,
                        textDecoration = TextDecoration.Underline
                    )
                    Text(
                        text = " y ",
                        fontSize = 10.sp,
                        color = TextSubDark
                    )
                    Text(
                        text = "Terminos de Uso",
                        fontSize = 10.sp,
                        color = TextSubDark,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.height(180.dp))

            // "Ya tengo cuenta" Link
            Text(
                text = "Ya tengo cuenta",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "Crear" Button
            val scope = rememberCoroutineScope()
            val repository = remember { SmarthomeRepository() }

            Button(
                onClick = {
                    scope.launch {
                        if (nombre.isNotBlank() && email.isNotBlank()) {
                            repository.registerUser(nombre, email, password)
                        }
                        onRegisterSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Crear",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PasswordRequirementItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp, horizontal = 12.dp)
    ) {
        Text(
            text = "• ",
            fontSize = 13.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextGray
        )
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFFEBEBEB), RoundedCornerShape(16.dp))
            .border(1.dp, Burgundy, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Normal
                    ),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    cursorBrush = SolidColor(Burgundy),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPassword) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = EyeOffIcon,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

val BackArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BackArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Burgundy)
        ) {
            moveTo(15.41f, 16.59f)
            lineTo(10.83f, 12f)
            lineTo(15.41f, 7.41f)
            lineTo(14f, 6f)
            lineTo(8f, 12f)
            lineTo(14f, 18f)
            close()
        }
    }.build()

val EyeOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "EyeOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 3f)
            lineTo(21f, 21f)

            moveTo(10.5f, 10.5f)
            curveTo(10.19f, 10.95f, 10f, 11.45f, 10f, 12f)
            curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f)
            curveTo(12.55f, 14f, 13.05f, 13.81f, 13.5f, 13.5f)

            moveTo(6.71f, 6.71f)
            curveTo(4.78f, 8.16f, 3.23f, 10.02f, 2f, 12f)
            curveTo(4f, 16.5f, 8f, 19f, 12f, 19f)
            curveTo(13.68f, 19f, 15.28f, 18.5f, 16.71f, 17.71f)

            moveTo(10f, 5.29f)
            curveTo(10.65f, 5.1f, 11.32f, 5f, 12f, 5f)
            curveTo(16f, 5f, 20f, 7.5f, 22f, 12f)
            curveTo(21.19f, 13.81f, 19.98f, 15.36f, 18.52f, 16.52f)
        }
    }.build()

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    SmarthomeTheme {
        RegisterScreen(
            onNavigateToWelcome = {},
            onNavigateToLogin = {},
            onRegisterSuccess = {}
        )
    }
}

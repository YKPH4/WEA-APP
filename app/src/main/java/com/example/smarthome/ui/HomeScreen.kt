package com.example.smarthome.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.ui.theme.SmarthomeTheme
import com.example.smarthome.ui.viewmodel.SmarthomeViewModel
import com.example.smarthome.config.CameraConfig
import com.example.smarthome.ui.components.RtspPlayer

// Colors
val Burgundy = Color(0xFF7A2024)
val ScreenBackground = Color(0xFFF4F4F5)
val CardBackground = Color(0xFFE8E8EA)
val TextDark = Color(0xFF000000)
val TextGray = Color(0xFF7D7D82)
val TextSubDark = Color(0xFF2C2C2C)

@Composable
fun SmartHomeScreen(
    viewModel: SmarthomeViewModel = viewModel(),
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLockDetailOpen by remember { mutableStateOf(false) }
    var showChangePasscodeDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val isDoorLocked by viewModel.isDoorLocked.collectAsState()
    val perfilesList by viewModel.perfilesList.collectAsState()
    val activitiesList by viewModel.activitiesList.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Header Bar
            TopBarSection(
                selectedNavIndex = selectedBottomNav,
                isLockDetailOpen = isLockDetailOpen,
                onLogout = onLogout,
                onBackToInicio = {
                    if (isLockDetailOpen) {
                        isLockDetailOpen = false
                    } else {
                        selectedBottomNav = 0
                    }
                }
            )

            // Header Title Section (for Inicio main screen)
            if (selectedBottomNav == 0 && !isLockDetailOpen) {
                HeaderTitleSection()
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main Content Container (Light Gray Card)
            MainContentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                selectedNavIndex = selectedBottomNav,
                isLockDetailOpen = isLockDetailOpen,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isDoorLocked = isDoorLocked,
                onToggleDoorLock = { viewModel.toggleLock() },
                onOpenLockDetail = { isLockDetailOpen = true },
                onChangePasscode = { showChangePasscodeDialog = true },
                onDownloadActivity = {
                    Toast.makeText(
                        context,
                        "Descargando historial de actividad (JUL 03/2026)...",
                        Toast.LENGTH_LONG
                    ).show()
                },
                perfilesList = perfilesList,
                activitiesList = activitiesList,
                onAddUser = { name, consentimiento -> viewModel.crearPerfil(name, consentimiento) },
                onDeleteUser = { perfilToDelete -> viewModel.eliminarPerfil(perfilToDelete) }
            )

            // Bottom Navigation Bar
            BottomNavigationBar(
                selectedIndex = selectedBottomNav,
                onItemSelected = {
                    selectedBottomNav = it
                    isLockDetailOpen = false
                }
            )
        }

        // Change Passcode Dialog
        if (showChangePasscodeDialog) {
            ChangePasscodeDialog(
                onDismiss = { showChangePasscodeDialog = false },
                onSave = { newPasscode ->
                    showChangePasscodeDialog = false
                    viewModel.updatePasscode(newPasscode) {
                        Toast.makeText(
                            context,
                            "Clave de acceso actualizada exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun TopBarSection(
    selectedNavIndex: Int,
    isLockDetailOpen: Boolean,
    onLogout: () -> Unit,
    onBackToInicio: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedNavIndex == 0 && !isLockDetailOpen) {
            // Exit/Door icon (Left)
            IconButton(
                onClick = onLogout,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = ExitDoorIcon,
                    contentDescription = "Salir",
                    tint = Burgundy,
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            // Back Arrow + Screen Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToInicio,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = BackArrowIcon,
                        contentDescription = "Volver",
                        tint = Burgundy,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isLockDetailOpen -> "Acceso puerta principal"
                        selectedNavIndex == 1 -> "Cámaras"
                        selectedNavIndex == 2 -> "Perfiles"
                        selectedNavIndex == 3 -> "Actividad"
                        else -> ""
                    },
                    fontSize = if (isLockDetailOpen) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        // Chat bubble icon (Right)
        IconButton(
            onClick = { /* Handle chat/notifications action */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = ChatBubbleIcon,
                contentDescription = "Mensajes",
                tint = Burgundy,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun HeaderTitleSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Casa de usuario",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Controla el acceso",
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            color = TextSubDark
        )
    }
}

@Composable
private fun MainContentCard(
    modifier: Modifier = Modifier,
    selectedNavIndex: Int,
    isLockDetailOpen: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDoorLocked: Boolean,
    onToggleDoorLock: () -> Unit,
    onOpenLockDetail: () -> Unit,
    onChangePasscode: () -> Unit,
    onDownloadActivity: () -> Unit,
    perfilesList: List<com.example.smarthome.data.models.PerfilRostro>,
    activitiesList: List<com.example.smarthome.data.models.RegistroActividad>,
    onAddUser: (String, Boolean) -> Unit,
    onDeleteUser: (com.example.smarthome.data.models.PerfilRostro) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = CardBackground
    ) {
        when {
            selectedNavIndex == 0 && isLockDetailOpen -> {
                DoorLockDetailContent(
                    isLocked = isDoorLocked,
                    onToggleLock = onToggleDoorLock,
                    onChangePasscode = onChangePasscode
                )
            }
            selectedNavIndex == 0 -> {
                InicioContent(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    isDoorLocked = isDoorLocked,
                    onOpenLockDetail = onOpenLockDetail
                )
            }
            selectedNavIndex == 1 -> CamerasContent()
            selectedNavIndex == 2 -> UsersContent(
                perfilesList = perfilesList,
                onAddUser = onAddUser,
                onDeleteUser = onDeleteUser
            )
            selectedNavIndex == 3 -> ActivityContent(
                activitiesList = activitiesList,
                onDownloadActivity = onDownloadActivity
            )
        }
    }
}

// ACCESO PUERTA PRINCIPAL DETAIL SCREEN
@Composable
private fun DoorLockDetailContent(
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onChangePasscode: () -> Unit
) {
    val outerRingColor by animateColorAsState(
        targetValue = if (isLocked) Color(0xFFE2A5A6) else Color(0xFFA5D6A7),
        label = "outerRing"
    )
    val innerCircleColor by animateColorAsState(
        targetValue = if (isLocked) Burgundy else Color(0xFF2E7D32),
        label = "innerCircle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.4f))

        // Big Lock Circle Graphic
        Box(
            modifier = Modifier
                .size(210.dp)
                .background(outerRingColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .background(innerCircleColor, CircleShape)
                    .clickable { onToggleLock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isLocked) "Bloqueado" else "Desbloqueado",
                    tint = Color.White,
                    modifier = Modifier.size(68.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status title
        Text(
            text = "Estado",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isLocked) "Cerrada/Bloqueada" else "Abierta/Desbloqueada",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = TextSubDark
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Main Action Button
        Button(
            onClick = onToggleLock,
            colors = ButtonDefaults.buttonColors(containerColor = innerCircleColor),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp)
        ) {
            Text(
                text = if (isLocked) "Abrir/Desbloquear" else "Cerrar/Bloquear",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        // "Cambiar clave de acceso" Link
        Text(
            text = "Cambiar clave de acceso",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Burgundy,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { onChangePasscode() }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// CHANGE PASSCODE DIALOG
@Composable
private fun ChangePasscodeDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newCode by remember { mutableStateOf("") }
    var confirmCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cambiar clave de acceso",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                CustomOutlinedTextField(
                    value = newCode,
                    onValueChange = { newCode = it },
                    placeholder = "Nueva clave (4 a 8 dígitos)",
                    isPassword = true,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomOutlinedTextField(
                    value = confirmCode,
                    onValueChange = { confirmCode = it },
                    placeholder = "Confirmar nueva clave",
                    isPassword = true,
                    keyboardType = KeyboardType.Number
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newCode.length !in 4..8) {
                        errorMessage = "La clave debe tener entre 4 y 8 dígitos"
                    } else if (newCode != confirmCode) {
                        errorMessage = "Las claves no coinciden"
                    } else {
                        onSave(newCode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = TextGray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

// INICIO TAB CONTENT
@Composable
private fun InicioContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDoorLocked: Boolean,
    onOpenLockDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        TabRowSection(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            DoorLockCard(
                isLocked = isDoorLocked,
                onOpenDetail = onOpenLockDetail
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedTab) {
                        1 -> "Dispositivos no configurados"
                        2 -> "Sin accesos pendientes"
                        else -> "Más opciones"
                    },
                    color = TextGray,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// CAMERAS CONTENT
@Composable
private fun CamerasContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cámaras en vivo",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        CameraCard(
            title = "Cámara Wansview",
            subtitle = "Transmisión en vivo",
            thumbnailContent = {
                RtspPlayer(
                    url = CameraConfig.RTSP_URL,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CameraCard(
            title = "Sala de estar",
            subtitle = "0 personas detectadas",
            thumbnailContent = { LivingRoomThumbnail() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CameraCard(
            title = "Jardín posterior",
            subtitle = "Sin movimiento",
            thumbnailContent = { GardenThumbnail() }
        )
    }
}

@Composable
private fun CameraCard(
    title: String,
    subtitle: String,
    thumbnailContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                thumbnailContent()

                // "EN VIVO" Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EN VIVO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }

                Icon(
                    imageVector = ApertureCameraIcon,
                    contentDescription = "Ver transmisión",
                    tint = Burgundy,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// USERS CONTENT
@Composable
private fun UsersContent(
    perfilesList: List<com.example.smarthome.data.models.PerfilRostro>,
    onAddUser: (String, Boolean) -> Unit,
    onDeleteUser: (com.example.smarthome.data.models.PerfilRostro) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var perfilToDelete by remember { mutableStateOf<com.example.smarthome.data.models.PerfilRostro?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Agregar+",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { /* Handle settings */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = WrenchIcon,
                    contentDescription = "Ajustes",
                    tint = Burgundy,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Perfiles Registrados Section
        Text(
            text = "Perfiles registrados",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (perfilesList.isNotEmpty()) {
            perfilesList.forEach { perfil ->
                UserRowItem(
                    name = perfil.nombre,
                    estado = perfil.estado,
                    onDelete = { perfilToDelete = perfil }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(
                text = "Sin perfiles registrados",
                fontSize = 14.sp,
                color = TextGray,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }

    // Add User Dialog
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onAddUser = { newName, consentimiento ->
                showAddDialog = false
                onAddUser(newName, consentimiento)
                Toast.makeText(context, "Perfil agregado exitosamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete User Confirmation Dialog
    if (perfilToDelete != null) {
        DeleteUserConfirmationDialog(
            userName = perfilToDelete!!.nombre,
            onDismiss = { perfilToDelete = null },
            onConfirmDelete = {
                val target = perfilToDelete!!
                perfilToDelete = null
                onDeleteUser(target)
                Toast.makeText(context, "Perfil eliminado de los accesos", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var consentimiento by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar nuevo perfil",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                CustomOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = consentimiento,
                        onCheckedChange = { consentimiento = it },
                        colors = CheckboxDefaults.colors(checkedColor = Burgundy)
                    )
                    Text(
                        text = "Autorizo el escaneo y almacenamiento de datos faciales para el control de acceso.",
                        fontSize = 12.sp,
                        color = TextSubDark,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Escanear rostro iniciado", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(46.dp)
                ) {
                    Text(
                        text = "Subir rostro para escanear",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Ingresa el nombre"
                        } else if (!consentimiento) {
                            errorMessage = "Debes aceptar el consentimiento"
                        } else {
                            onAddUser(name, consentimiento)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Agregar",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DeleteUserConfirmationDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar acceso",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar a $userName de la lista de accesos?",
                fontSize = 15.sp,
                color = TextSubDark
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = TextGray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun UserRowItem(
    name: String,
    estado: String,
    onDelete: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gray square avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFD0D0D0), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = estado,
                fontSize = 13.sp,
                color = if (estado.lowercase() == "activo") Color(0xFF2E7D32) else TextGray
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = SlidersIcon,
                contentDescription = "Eliminar o configurar acceso",
                tint = Burgundy,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ACTIVITY CONTENT
@Composable
private fun ActivityContent(
    activitiesList: List<com.example.smarthome.data.models.RegistroActividad>,
    onDownloadActivity: () -> Unit
) {
    var currentDateIndex by remember { mutableIntStateOf(0) }
    val dates = listOf("JUL 03/2026", "JUL 02/2026", "JUL 01/2026")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Reporte de seguridad + Calendar icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reporte de seguridad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Icon(
                    imageVector = CalendarIcon,
                    contentDescription = "Calendario",
                    tint = Burgundy,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selector: < JUL 03/2026 >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentDateIndex < dates.size - 1) currentDateIndex++
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = BackArrowIcon,
                        contentDescription = "Anterior",
                        tint = Burgundy,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = dates[currentDateIndex],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (currentDateIndex > 0) currentDateIndex--
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = ForwardArrowIcon,
                        contentDescription = "Siguiente",
                        tint = Burgundy,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (activitiesList.isNotEmpty()) {
                activitiesList.forEach { log ->
                    ActivityCard(
                        title = log.titulo,
                        subtitle = log.estado,
                        time = log.hora,
                        thumbnailContent = { FrontDoorThumbnail() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Activity Item 1
                ActivityCard(
                    title = "Puerta frontal",
                    subtitle = "Estado:Cerrada/Bloqueada",
                    time = "12:03",
                    thumbnailContent = { FrontDoorThumbnail() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Activity Item 2
                ActivityCard(
                    title = "Sala de estar",
                    subtitle = "0 personas detectadas",
                    time = "15:30",
                    thumbnailContent = { LivingRoomThumbnail() }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Floating Download Button at Bottom Right
        Surface(
            onClick = onDownloadActivity,
            shape = RoundedCornerShape(14.dp),
            color = Burgundy,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(52.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = DownloadIcon,
                    contentDescription = "Descargar historial",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    title: String,
    subtitle: String,
    time: String,
    thumbnailContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 75.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                thumbnailContent()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 1
                )
            }

            Text(
                text = time,
                fontSize = 14.sp,
                color = TextDark,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

// CANVAS THUMBNAILS
@Composable
fun FrontDoorThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(color = Color(0xFFE2E8F0))

        drawRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(0f, h * 0.7f),
            size = Size(w, h * 0.3f)
        )

        drawRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(w * 0.08f, h * 0.1f),
            size = Size(w * 0.35f, h * 0.75f)
        )
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(w * 0.11f, h * 0.15f),
            size = Size(w * 0.29f, h * 0.7f)
        )

        drawCircle(
            color = Color(0xFF8D6E63),
            radius = w * 0.08f,
            center = Offset(w * 0.65f, h * 0.35f)
        )
        drawRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(w * 0.58f, h * 0.43f),
            size = Size(w * 0.14f, h * 0.3f)
        )
        drawRect(
            color = Color(0xFFD7CCC8),
            topLeft = Offset(w * 0.52f, h * 0.48f),
            size = Size(w * 0.25f, h * 0.22f)
        )
    }
}

@Composable
fun LivingRoomThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(color = Color(0xFF78909C))

        drawRect(
            color = Color(0xFFD7CCC8),
            topLeft = Offset(0f, h * 0.65f),
            size = Size(w, h * 0.35f)
        )

        drawRoundRect(
            color = Color(0xFFCFD8DC),
            topLeft = Offset(w * 0.1f, h * 0.48f),
            size = Size(w * 0.8f, h * 0.3f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        drawRect(
            color = Color(0xFFB0BEC5),
            topLeft = Offset(w * 0.2f, h * 0.15f),
            size = Size(w * 0.25f, h * 0.25f)
        )
        drawRect(
            color = Color(0xFFB0BEC5),
            topLeft = Offset(w * 0.55f, h * 0.15f),
            size = Size(w * 0.25f, h * 0.25f)
        )
    }
}

@Composable
fun GardenThumbnail() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(color = Color(0xFFE0F7FA))

        drawRect(
            color = Color(0xFFA5D6A7),
            topLeft = Offset(0f, h * 0.5f),
            size = Size(w, h * 0.5f)
        )

        drawCircle(
            color = Color(0xFF66BB6A),
            radius = w * 0.18f,
            center = Offset(w * 0.25f, h * 0.35f)
        )
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = w * 0.22f,
            center = Offset(w * 0.75f, h * 0.3f)
        )
    }
}

@Composable
private fun TabRowSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Home", "Dispositivos", "Accesos", "...")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Burgundy else TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(2.5.dp)
                        .width(if (isSelected && title != "...") 32.dp else 0.dp)
                        .background(
                            color = if (isSelected) Burgundy else Color.Transparent,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun DoorLockCard(
    isLocked: Boolean,
    onOpenDetail: () -> Unit
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isLocked) Burgundy else Color(0xFF2E7D32),
        label = "cardBg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() },
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Puerta principal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isLocked) "Cerrada/Bloqueada" else "Desbloqueada/Abierta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = if (isLocked) "Bloqueada" else "Desbloqueada",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        NavDestination("Inicio", HomeIcon),
        NavDestination("Camaras", ApertureCameraIcon),
        NavDestination("Perfiles", UsersIcon),
        NavDestination("Actividad", CubeActivityIcon)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, destination ->
            val isSelected = selectedIndex == index

            Surface(
                onClick = { onItemSelected(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Burgundy else Color.Transparent,
                modifier = Modifier
                    .width(80.dp)
                    .height(68.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (isSelected) Color.White else TextSubDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = destination.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSubDark
                    )
                }
            }
        }
    }
}

private data class NavDestination(
    val label: String,
    val icon: ImageVector
)

// Vector Icons
val ExitDoorIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ExitDoor",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14f, 4f)
            lineTo(19f, 4f)
            lineTo(19f, 20f)
            lineTo(14f, 20f)

            moveTo(5f, 12f)
            lineTo(15f, 12f)
            moveTo(11f, 8f)
            lineTo(15f, 12f)
            lineTo(11f, 16f)
        }
    }.build()

val ChatBubbleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ChatBubble",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20f, 4f)
            lineTo(4f, 4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            lineTo(2f, 16f)
            curveTo(2f, 17.1f, 2.9f, 18f, 4f, 18f)
            lineTo(16f, 18f)
            lineTo(20f, 22f)
            lineTo(20f, 6f)
            curveTo(20f, 4.9f, 19.1f, 4f, 18f, 4f)
            close()
        }
    }.build()

val HomeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Home",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 10.5f)
            lineTo(12f, 3f)
            lineTo(21f, 10.5f)
            lineTo(21f, 20f)
            curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            lineTo(4f, 21f)
            curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
            close()

            moveTo(9.5f, 21f)
            lineTo(9.5f, 14f)
            lineTo(14.5f, 14f)
            lineTo(14.5f, 21f)
        }
    }.build()

val ApertureCameraIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ApertureCamera",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(14.37f, 7.5f)
            lineTo(9.63f, 16.5f)
            moveTo(6.5f, 9.63f)
            lineTo(15.5f, 14.37f)
            moveTo(7.5f, 14.37f)
            lineTo(16.5f, 9.63f)
        }
    }.build()

val UsersIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Users",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f
        ) {
            moveTo(12f, 11f)
            curveTo(13.65f, 11f, 15f, 9.65f, 15f, 8f)
            curveTo(15f, 6.35f, 13.65f, 5f, 12f, 5f)
            curveTo(10.35f, 5f, 9f, 6.35f, 9f, 8f)
            curveTo(9f, 9.65f, 10.35f, 11f, 12f, 11f)
            close()
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(5f, 20f)
            curveTo(5f, 16.68f, 8.13f, 14f, 12f, 14f)
            curveTo(15.87f, 14f, 19f, 16.68f, 19f, 20f)
        }
    }.build()

val CubeActivityIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CubeActivity",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 3f)
            lineTo(20f, 7.5f)
            lineTo(20f, 16.5f)
            lineTo(12f, 21f)
            lineTo(4f, 16.5f)
            lineTo(4f, 7.5f)
            close()

            moveTo(12f, 3f)
            lineTo(12f, 12f)

            moveTo(12f, 12f)
            lineTo(20f, 7.5f)

            moveTo(12f, 12f)
            lineTo(4f, 7.5f)
        }
    }.build()

val CalendarIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Calendar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Burgundy),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 7f)
            lineTo(20f, 7f)
            lineTo(20f, 20f)
            curveTo(20f, 20.55f, 19.55f, 21f, 19f, 21f)
            lineTo(5f, 21f)
            curveTo(4.45f, 21f, 4f, 20.55f, 4f, 20f)
            close()

            moveTo(16f, 3f)
            lineTo(16f, 7f)
            moveTo(8f, 3f)
            lineTo(8f, 7f)

            moveTo(4f, 11f)
            lineTo(20f, 11f)
        }
    }.build()

val DownloadIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 4f)
            lineTo(12f, 15f)

            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)

            moveTo(4f, 19f)
            lineTo(20f, 19f)
        }
    }.build()

val WrenchIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Wrench",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Burgundy),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14.7f, 6.3f)
            curveTo(15.3f, 4.3f, 14.2f, 2.3f, 12.3f, 2f)
            curveTo(10.8f, 1.8f, 9.4f, 2.7f, 8.8f, 4.1f)
            lineTo(11f, 6.3f)
            lineTo(9.7f, 7.6f)
            lineTo(7.5f, 5.4f)
            curveTo(6.1f, 6f, 5.2f, 7.4f, 5.4f, 8.9f)
            curveTo(5.7f, 10.8f, 7.7f, 11.9f, 9.7f, 11.3f)
            lineTo(18f, 19.6f)
            curveTo(18.4f, 20f, 19f, 20f, 19.4f, 19.6f)
            lineTo(20.4f, 18.6f)
            curveTo(20.8f, 18.2f, 20.8f, 17.6f, 20.4f, 17.2f)
            lineTo(14.7f, 6.3f)
            close()
        }
    }.build()

val SlidersIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Sliders",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Burgundy),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(4f, 6f)
            lineTo(20f, 6f)
            moveTo(8f, 4f)
            lineTo(8f, 8f)

            moveTo(4f, 12f)
            lineTo(20f, 12f)
            moveTo(16f, 10f)
            lineTo(16f, 14f)

            moveTo(4f, 18f)
            lineTo(20f, 18f)
            moveTo(11f, 16f)
            lineTo(11f, 20f)
        }
    }.build()

val ForwardArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ForwardArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Burgundy)
        ) {
            moveTo(8.59f, 16.59f)
            lineTo(13.17f, 12f)
            lineTo(8.59f, 7.41f)
            lineTo(10f, 6f)
            lineTo(16f, 12f)
            lineTo(10f, 18f)
            close()
        }
    }.build()

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SmartHomeScreenPreview() {
    SmarthomeTheme {
        SmartHomeScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DoorLockDetailPreview() {
    SmarthomeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopBarSection(
                    selectedNavIndex = 0,
                    isLockDetailOpen = true,
                    onLogout = {},
                    onBackToInicio = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainContentCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    selectedNavIndex = 0,
                    isLockDetailOpen = true,
                    selectedTab = 0,
                    onTabSelected = {},
                    isDoorLocked = true,
                    onToggleDoorLock = {},
                    onOpenLockDetail = {},
                    onChangePasscode = {},
                    onDownloadActivity = {},
                    perfilesList = emptyList(),
                    activitiesList = emptyList(),
                    onAddUser = { _, _ -> },
                    onDeleteUser = {}
                )
                BottomNavigationBar(
                    selectedIndex = 0,
                    onItemSelected = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivityScreenPreview() {
    SmarthomeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopBarSection(
                    selectedNavIndex = 3,
                    isLockDetailOpen = false,
                    onLogout = {},
                    onBackToInicio = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainContentCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    selectedNavIndex = 3,
                    isLockDetailOpen = false,
                    selectedTab = 0,
                    onTabSelected = {},
                    isDoorLocked = true,
                    onToggleDoorLock = {},
                    onOpenLockDetail = {},
                    onChangePasscode = {},
                    onDownloadActivity = {},
                    perfilesList = emptyList(),
                    activitiesList = emptyList(),
                    onAddUser = { _, _ -> },
                    onDeleteUser = {}
                )
                BottomNavigationBar(
                    selectedIndex = 3,
                    onItemSelected = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UsersScreenPreview() {
    SmarthomeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopBarSection(
                    selectedNavIndex = 2,
                    isLockDetailOpen = false,
                    onLogout = {},
                    onBackToInicio = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainContentCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    selectedNavIndex = 2,
                    isLockDetailOpen = false,
                    selectedTab = 0,
                    onTabSelected = {},
                    isDoorLocked = true,
                    onToggleDoorLock = {},
                    onOpenLockDetail = {},
                    onChangePasscode = {},
                    onDownloadActivity = {},
                    perfilesList = emptyList(),
                    activitiesList = emptyList(),
                    onAddUser = { _, _ -> },
                    onDeleteUser = {}
                )
                BottomNavigationBar(
                    selectedIndex = 2,
                    onItemSelected = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CamerasScreenPreview() {
    SmarthomeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopBarSection(
                    selectedNavIndex = 1,
                    isLockDetailOpen = false,
                    onLogout = {},
                    onBackToInicio = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainContentCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    selectedNavIndex = 1,
                    isLockDetailOpen = false,
                    selectedTab = 0,
                    onTabSelected = {},
                    isDoorLocked = true,
                    onToggleDoorLock = {},
                    onOpenLockDetail = {},
                    onChangePasscode = {},
                    onDownloadActivity = {},
                    perfilesList = emptyList(),
                    activitiesList = emptyList(),
                    onAddUser = { _, _ -> },
                    onDeleteUser = {}
                )
                BottomNavigationBar(
                    selectedIndex = 1,
                    onItemSelected = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddUserDialogPreview() {
    SmarthomeTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AddUserDialog(
                onDismiss = {},
                onAddUser = { _, _ -> }
            )
        }
    }
}
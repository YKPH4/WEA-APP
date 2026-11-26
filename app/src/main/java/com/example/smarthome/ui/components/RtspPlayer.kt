package com.example.smarthome.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun RtspPlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Estado para controlar si estamos en pantalla completa
    var isFullScreen by remember { mutableStateOf(false) }

    val libVLC = remember {
        LibVLC(context, arrayListOf("--network-caching=300"))
    }
    val mediaPlayer = remember {
        MediaPlayer(libVLC)
    }

    DisposableEffect(url) {
        val media = Media(libVLC, Uri.parse(url))
        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.detachViews()
            mediaPlayer.release()
            libVLC.release()
        }
    }

    // Extraemos la UI del video a una variable para reusarla en modo normal y pantalla completa
    val VideoContent: @Composable (Modifier) -> Unit = { mod ->
        Box(
            modifier = mod
                .background(Color.Black)
                .clickable { isFullScreen = !isFullScreen } // Alternar pantalla completa al tocar
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VLCVideoLayout(ctx).also { layout ->
                        // Desvinculamos cualquier vista previa antes de vincular la nueva
                        mediaPlayer.detachViews()
                        mediaPlayer.attachViews(layout, null, false, false)
                    }
                }
            )
        }
    }

    if (isFullScreen) {
        // Efecto para manejar la rotación y la UI del sistema
        DisposableEffect(Unit) {
            // Guardar la orientación original
            val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // Forzar horizontal
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            // Ocultar barras de sistema (estado y navegación)
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            onDispose {
                // Restaurar orientación y barras al cerrar el Dialog (ej. al presionar Atrás)
                activity?.requestedOrientation = originalOrientation
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }

        // Renderizar sobre toda la app usando un Dialog
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // Permite que el dialog ocupe el 100% del ancho
                decorFitsSystemWindows = false   // Permite dibujar debajo de las barras del sistema
            )
        ) {
            VideoContent(Modifier.fillMaxSize())
        }
    } else {
        // Renderizar en su tamaño normal embebido en tu pantalla
        VideoContent(modifier)
    }
}
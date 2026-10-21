package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.ui.MainViewModel
import com.example.ui.components.HudCard
import com.example.ui.theme.JarvisAccentAmber
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisCyanSecondary
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onRequestMicrophonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val speechTtsEnabled by viewModel.speechTtsEnabled.collectAsState()
    val voicePitch by viewModel.voicePitch.collectAsState()
    val voiceRate by viewModel.voiceRate.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isStandbyActive by viewModel.voiceManager.isStandbyModeActive.collectAsState()
    val isMasculineVoiceSelected by viewModel.voiceManager.isMasculineVoiceSelected.collectAsState()
    val voiceDiagnostics by viewModel.voiceManager.voiceDiagnostics.collectAsState()

    var editName by remember(userName) { mutableStateOf(userName) }
    var editKey by remember(customApiKey) { mutableStateOf(customApiKey) }

    // Check actual runtime permissions
    val micGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val cameraGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val hasBuildConfigKey = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (_: Throwable) {
        false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "CONFIGURACIÓN DEL PROTOCOLO JARVIS",
                color = JarvisCyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "Personalización, permisos y enlace de inteligencia artificial",
                color = JarvisTextSecondary,
                fontSize = 11.sp
            )
        }

        // --- 1. Callsign / Personalization ---
        item {
            HudCard(title = "Identidad del Operador") {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre o distintivo de llamada", color = JarvisTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanPrimary,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("operator_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.updateUserName(editName.trim())
                                viewModel.emitToast("Distintivo actualizado a: $editName")
                            }
                        },
                        border = BorderStroke(1.dp, JarvisCyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("GUARDAR DISTINTIVO", color = JarvisCyanPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 2. Voice Audio Output (TTS) ---
        item {
            HudCard(title = "Síntesis Vocal (Voz de Mayordomo)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Respuestas Habladas",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "JARVIS responderá con voz masculina, elocuente y tono señorial.",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = speechTtsEnabled,
                        onCheckedChange = { viewModel.toggleSpeechTts(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyanPrimary,
                            checkedTrackColor = JarvisCyanDim.copy(alpha = 0.5f),
                            uncheckedThumbColor = JarvisTextMuted,
                            uncheckedTrackColor = JarvisSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "PERFILES ACÚSTICOS DISPONIBLES:",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isButlerActive = voicePitch <= 0.75f
                    OutlinedButton(
                        onClick = { viewModel.applyButlerVoicePreset() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isButlerActive) JarvisCyanPrimary else JarvisBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🎩 MAYORDOMO",
                            fontSize = 10.sp,
                            color = if (isButlerActive) JarvisCyanPrimary else JarvisTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.applyTacticalVoicePreset() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (!isButlerActive) JarvisCyanPrimary else JarvisBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⚡ TÁCTICO",
                            fontSize = 10.sp,
                            color = if (!isButlerActive) JarvisCyanPrimary else JarvisTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = JarvisSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisBorderGlow.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isMasculineVoiceSelected) JarvisAccentGreen else JarvisAccentAmber, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMasculineVoiceSelected) "VOZ MASCULINA MOTOR ACTIVA" else "MODULACIÓN BARÍTONO ACTIVA",
                                color = if (isMasculineVoiceSelected) JarvisAccentGreen else JarvisAccentAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = voiceDiagnostics,
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.testButlerVoice() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisCyanPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("test_butler_voice_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Probar voz",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROBAR VOZ DE MAYORDOMO",
                        fontSize = 11.sp,
                        color = JarvisCyanPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { viewModel.openTtsSettings() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisBorder),
                    modifier = Modifier.fillMaxWidth().testTag("open_tts_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes del sistema",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AJUSTES DE VOZ DE ANDROID (MOTOR TTS)",
                        fontSize = 10.sp,
                        color = JarvisTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- 3. Gemini AI Configuration ---
        item {
            HudCard(title = "Motor de Inteligencia Artificial") {
                val statusTitle = when {
                    hasBuildConfigKey -> "Activo vía AI Studio Secrets"
                    customApiKey.isNotBlank() -> "Activo vía Llave Personal"
                    else -> "Modo Táctico Autónomo (Offline)"
                }
                val statusColor = if (hasBuildConfigKey || customApiKey.isNotBlank()) JarvisAccentGreen else JarvisAccentAmber

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ESTADO: $statusTitle",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "JARVIS puede utilizar Gemini 3.5 Flash para responder preguntas de razonamiento avanzado en español. Si no se ingresa clave, opera en modo táctico local con comandos nativos de dispositivo.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editKey,
                    onValueChange = { editKey = it },
                    label = { Text("Clave API de Gemini (Opcional)", color = JarvisTextSecondary) },
                    placeholder = { Text("AIzaSy...", color = JarvisTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanPrimary,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customApiKey.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateCustomApiKey("")
                                editKey = ""
                                viewModel.emitToast("Clave personalizada removida")
                            },
                            border = BorderStroke(1.dp, JarvisBorder)
                        ) {
                            Text("BORRAR", color = JarvisTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.updateCustomApiKey(editKey.trim())
                            viewModel.emitToast("Configuración de IA actualizada")
                        },
                        border = BorderStroke(1.dp, JarvisCyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("APLICAR LLAVE", color = JarvisCyanPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 4. Permissions Diagnostics & Management ---
        item {
            HudCard(title = "Permisos y Accesos Autorizados") {
                PermissionRow(
                    name = "Micrófono (Entrada de Voz)",
                    granted = micGranted,
                    onRequest = { onRequestMicrophonePermission() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionRow(
                    name = "Cámara / Flash (Linterna)",
                    granted = cameraGranted,
                    onRequest = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    border = BorderStroke(1.dp, JarvisBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ABRIR AJUSTES DE LA APLICACIÓN", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = JarvisCyanSecondary)
                }
            }
        }

        // --- 5. Privacy, Ethics & Architecture Declaration ---
        item {
            HudCard(title = "Arquitectura, Privacidad y Seguridad") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Seguridad",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DECLARACIÓN DE INTEGRIDAD",
                        color = JarvisCyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Operación Real y Legal: JARVIS utiliza exclusivamente APIs oficiales y permisos explícitamente autorizados por Android. No simula accesos ficticios de root.\n" +
                           "• Almacenamiento Local Aislado: El historial de comunicaciones y directivas se archivan en la base de datos local SQLite (Room) dentro del sandbox privado del dispositivo.\n" +
                           "• Confirmación Previa Obligatoria: Toda acción que involucre interactuar con apps externas, programar alarmas o purgar información requiere autorización en pantalla.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = JarvisTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = if (granted) "AUTORIZADO" else "NO CONCEDIDO",
                color = if (granted) JarvisAccentGreen else JarvisAccentAmber,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (!granted) {
            OutlinedButton(
                onClick = onRequest,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, JarvisAccentAmber)
            ) {
                Text("AUTORIZAR", fontSize = 10.sp, color = JarvisAccentAmber, fontFamily = FontFamily.Monospace)
            }
        } else {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Concedido",
                tint = JarvisAccentGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

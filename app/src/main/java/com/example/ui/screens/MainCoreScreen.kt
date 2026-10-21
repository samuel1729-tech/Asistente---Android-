package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisTab
import com.example.ui.MainViewModel
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.CoreState
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderGlow
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisCyanSecondary
import com.example.ui.theme.JarvisDarkObsidian
import com.example.ui.theme.JarvisStatusGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWingBg

@Composable
fun MainCoreScreen(
    viewModel: MainViewModel,
    onRequestMicrophonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coreState by viewModel.coreState.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceAmplitude by viewModel.voiceManager.voiceAmplitude.collectAsState()
    val isStandbyActive by viewModel.voiceManager.isStandbyModeActive.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkObsidian)
            .drawBehind {
                val w = size.width
                val cyanVisor = JarvisCyanPrimary.copy(alpha = 0.40f)
                val stroke = 1.5.dp.toPx()

                // Left Futuristic Visor Bracket
                val leftPath = Path().apply {
                    moveTo(30.dp.toPx(), 45.dp.toPx())
                    lineTo(8.dp.toPx(), 75.dp.toPx())
                    lineTo(8.dp.toPx(), 360.dp.toPx())
                    lineTo(22.dp.toPx(), 390.dp.toPx())
                }
                drawPath(leftPath, color = cyanVisor, style = Stroke(width = stroke))

                // Left Accent tick marks
                drawLine(
                    color = JarvisCyanPrimary,
                    start = Offset(8.dp.toPx(), 180.dp.toPx()),
                    end = Offset(15.dp.toPx(), 180.dp.toPx()),
                    strokeWidth = stroke * 1.5f
                )

                // Right Futuristic Visor Bracket
                val rightPath = Path().apply {
                    moveTo(w - 30.dp.toPx(), 45.dp.toPx())
                    lineTo(w - 8.dp.toPx(), 75.dp.toPx())
                    lineTo(w - 8.dp.toPx(), 360.dp.toPx())
                    lineTo(w - 22.dp.toPx(), 390.dp.toPx())
                }
                drawPath(rightPath, color = cyanVisor, style = Stroke(width = stroke))

                // Right Accent tick marks
                drawLine(
                    color = JarvisCyanPrimary,
                    start = Offset(w - 15.dp.toPx(), 180.dp.toPx()),
                    end = Offset(w - 8.dp.toPx(), 180.dp.toPx()),
                    strokeWidth = stroke * 1.5f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Top HUD Header Bar ---
            JarvisTopBar(
            coreState = coreState,
            isStandbyActive = isStandbyActive,
            onSettingsClick = { viewModel.selectTab(JarvisTab.SETTINGS) },
            onStatusToggle = {
                onRequestMicrophonePermission()
                viewModel.toggleStandbyMode(!isStandbyActive)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. Personal Greeting & Wake-Word Cue ---
        Text(
            text = "Hola, $userName",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Estoy atento a tu llamada.",
            color = JarvisTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "“Oye, JARVIS”",
            color = JarvisCyanPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                onRequestMicrophonePermission()
                viewModel.toggleStandbyMode(!isStandbyActive)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. Holographic Central Core + Side Wing Panels ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Wing: IA Status & Speech Analyzer
            HudSideWing(
                isLeft = true,
                topIcon = Icons.Default.GraphicEq,
                topTitle = "IA",
                topSubtitle = if (viewModel.customApiKey.value.isNotBlank()) "GEMINI" else "ACTIVA",
                bottomIcon = Icons.Default.Psychology,
                bottomTitle = when (coreState) {
                    CoreState.LISTENING -> "ESCUCHANDO"
                    CoreState.PROCESSING -> "ANALIZANDO"
                    CoreState.SPEAKING -> "RESPONDIENDO"
                    else -> "ANALIZANDO"
                },
                bottomSubtitle = "TU VOZ"
            )

            // Central AI Reactor Unit
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                ArcReactorCore(
                    state = coreState,
                    amplitude = voiceAmplitude,
                    onClick = {
                        onRequestMicrophonePermission()
                        viewModel.triggerVoiceInput()
                    },
                    size = 175.dp,
                    modifier = Modifier.testTag("arc_reactor_core")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Audio Waveform Visualizer
                LiveAudioWaveformBar(
                    amplitude = voiceAmplitude,
                    isActive = coreState == CoreState.LISTENING || coreState == CoreState.SPEAKING
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Floating Central Microphone Button
                CentralMicButton(
                    isListening = coreState == CoreState.LISTENING,
                    onClick = {
                        onRequestMicrophonePermission()
                        viewModel.triggerVoiceInput()
                    }
                )
            }

            // Right Wing: Battery Telemetry & Network Telemetry
            HudSideWing(
                isLeft = false,
                topIcon = if (batteryInfo.isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                topTitle = "${batteryInfo.percentage}%",
                topSubtitle = "BATERÍA",
                bottomIcon = Icons.Default.Wifi,
                bottomTitle = networkStatus.label,
                bottomSubtitle = networkStatus.typeName
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Section: COMANDOS RÁPIDOS Header with Tech Vector Line ---
        QuickCommandsHeader()

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. Quick Commands Grid (2 rows x 4 columns) ---
        QuickCommandsGrid(
            isTorchOn = isTorchOn,
            onCommandClick = { commandId ->
                when (commandId) {
                    "diag" -> viewModel.processUserMessage("Diagnóstico")
                    "torch" -> viewModel.toggleFlashlight()
                    "battery" -> viewModel.processUserMessage("Batería")
                    "alarm" -> viewModel.processUserMessage("Alarma 07:00")
                    "apps" -> viewModel.selectTab(JarvisTab.TOOLS)
                    "nav" -> viewModel.processUserMessage("Abrir mapas")
                    "notes" -> viewModel.selectTab(JarvisTab.HISTORY)
                    "settings" -> viewModel.selectTab(JarvisTab.SETTINGS)
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- 6. Bottom Response & Status HUD Card (No typing field!) ---
        val lastJarvisMessage = remember(messages) {
            messages.lastOrNull { it.sender == "JARVIS" }?.content
                ?: "Estoy en modo de espera. Solo di “Oye, JARVIS” y dime qué necesitas."
        }

        BottomJarvisStatusCard(
            message = lastJarvisMessage,
            isSpeaking = coreState == CoreState.SPEAKING,
            onClick = {
                viewModel.voiceManager.speak(lastJarvisMessage)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
}

// ---------------------------------------------------------------------------
// 1. Top HUD Bar
// ---------------------------------------------------------------------------
@Composable
private fun JarvisTopBar(
    coreState: CoreState,
    isStandbyActive: Boolean,
    onSettingsClick: () -> Unit,
    onStatusToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Branding
        Column {
            Text(
                text = "JARVIS",
                color = JarvisCyanPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
            Text(
                text = "ASISTENTE PERSONAL INTELIGENTE",
                color = JarvisCyanSecondary,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Right Settings Button + Status Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Settings Icon in HUD frame
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = JarvisCardBg,
                border = BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxSize().testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Status Pill (Tapping toggles standby sentinel mode)
            val stateTitle = when (coreState) {
                CoreState.IDLE -> if (isStandbyActive) "EN ESPERA" else "EN LÍNEA"
                CoreState.STANDBY -> "EN ESPERA"
                CoreState.LISTENING -> "ESCUCHANDO"
                CoreState.PROCESSING -> "PROCESANDO"
                CoreState.SPEAKING -> "HABLANDO"
            }

            val stateSubtitle = when (coreState) {
                CoreState.LISTENING -> "Escuchando voz..."
                CoreState.PROCESSING -> "Calculando..."
                CoreState.SPEAKING -> "Emitiendo..."
                else -> if (isStandbyActive) "Escuchando..." else "Toca para hablar"
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = JarvisCardBg,
                border = BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier
                    .clickable(onClick = onStatusToggle)
                    .testTag("status_sentinel_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing green/cyan status dot
                    PulsingStatusDot(isActive = isStandbyActive || coreState != CoreState.IDLE)
                    Spacer(modifier = Modifier.width(7.dp))
                    Column {
                        Text(
                            text = stateTitle,
                            color = JarvisCyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = stateSubtitle,
                            color = JarvisTextSecondary,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingStatusDot(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                if (isActive) JarvisStatusGreen.copy(alpha = alpha) else JarvisCyanDim,
                CircleShape
            )
    )
}

// ---------------------------------------------------------------------------
// 2. Cybernetic Side Wing Panels (IA & Batería/Red)
// ---------------------------------------------------------------------------
@Composable
private fun HudSideWing(
    isLeft: Boolean,
    topIcon: ImageVector,
    topTitle: String,
    topSubtitle: String,
    bottomIcon: ImageVector,
    bottomTitle: String,
    bottomSubtitle: String
) {
    val cyanGlow = JarvisCyanPrimary.copy(alpha = 0.55f)
    val cardBgTop = Color(0xFF061426)
    val cardBgBottom = Color(0xFF030914)

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(184.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val bevel = 15.dp.toPx()

                // 1. Draw beveled sci-fi polygonal armor plate
                val platePath = Path().apply {
                    if (isLeft) {
                        moveTo(bevel, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h)
                        lineTo(bevel, h)
                        lineTo(0f, h - bevel)
                        lineTo(0f, bevel)
                        close()
                    } else {
                        moveTo(0f, 0f)
                        lineTo(w - bevel, 0f)
                        lineTo(w, bevel)
                        lineTo(w, h - bevel)
                        lineTo(w - bevel, h)
                        lineTo(0f, h)
                        close()
                    }
                }

                // Fill with subtle holographic vertical gradient
                drawPath(
                    path = platePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(cardBgTop, cardBgBottom)
                    )
                )

                // Stroke beveled plate border
                drawPath(
                    path = platePath,
                    color = JarvisCardBorder,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // 2. Outer floating sci-fi accent bracket
                val bracketGap = 3.dp.toPx()
                val bracketPath = Path().apply {
                    if (isLeft) {
                        moveTo(w - 6.dp.toPx(), -bracketGap)
                        lineTo(bevel + 2.dp.toPx(), -bracketGap)
                        lineTo(-bracketGap, bevel + 2.dp.toPx())
                        lineTo(-bracketGap, h - bevel - 2.dp.toPx())
                        lineTo(bevel + 2.dp.toPx(), h + bracketGap)
                        lineTo(w - 6.dp.toPx(), h + bracketGap)
                    } else {
                        moveTo(6.dp.toPx(), -bracketGap)
                        lineTo(w - bevel - 2.dp.toPx(), -bracketGap)
                        lineTo(w + bracketGap, bevel + 2.dp.toPx())
                        lineTo(w + bracketGap, h - bevel - 2.dp.toPx())
                        lineTo(w - bevel - 2.dp.toPx(), h + bracketGap)
                        lineTo(6.dp.toPx(), h + bracketGap)
                    }
                }

                drawPath(
                    path = bracketPath,
                    color = cyanGlow,
                    style = Stroke(
                        width = 1.4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 8f), 0f)
                    )
                )
            }
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = topIcon,
                    contentDescription = null,
                    tint = JarvisCyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topTitle,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = topSubtitle,
                    color = JarvisCyanPrimary,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // High-Tech Divider with cyan center node
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    JarvisCyanPrimary.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(JarvisCyanPrimary, CircleShape)
                )
            }

            // Bottom Item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = bottomIcon,
                    contentDescription = null,
                    tint = JarvisCyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bottomTitle,
                    color = JarvisCyanPrimary,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = bottomSubtitle,
                    color = JarvisTextSecondary,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Audio Waveform Visualizer & Central Floating Mic Button
// ---------------------------------------------------------------------------
@Composable
private fun LiveAudioWaveformBar(
    amplitude: Float,
    isActive: Boolean
) {
    val barCount = 13
    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Row(
        modifier = Modifier.height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val centerDist = kotlin.math.abs(i - (barCount / 2f)) / (barCount / 2f)
            val baseHeight = (1f - centerDist) * 16f + 4f
            val dynamicBoost = if (isActive) {
                val waveFactor = kotlin.math.sin((waveOffset * kotlin.math.PI * 2f + i * 0.5f).toFloat()).coerceAtLeast(0f)
                (amplitude * 18f) + (waveFactor * 8f)
            } else {
                2f
            }

            val finalHeight = (baseHeight + dynamicBoost).coerceIn(3f, 22f)

            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(finalHeight.dp)
                    .background(
                        if (isActive) JarvisCyanPrimary else JarvisCyanDim.copy(alpha = 0.5f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun CentralMicButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "MicPulse")
    val ringScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
    ) {
        // Outer glowing halo ring
        Box(
            modifier = Modifier
                .size((52 * ringScale).dp)
                .background(
                    if (isListening) Color(0x44EF4444) else Color(0x3300F0FF),
                    CircleShape
                )
        )

        // Main floating button
        Surface(
            shape = CircleShape,
            color = Color(0xFF071526),
            border = BorderStroke(2.dp, if (isListening) Color(0xFFEF4444) else JarvisCyanPrimary),
            modifier = Modifier
                .size(52.dp)
                .clickable(onClick = onClick)
                .testTag("floating_mic_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Activar micrófono",
                    tint = if (isListening) Color(0xFFEF4444) else JarvisCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Quick Commands Header & 2x4 Holographic Grid
// ---------------------------------------------------------------------------
@Composable
private fun QuickCommandsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• COMANDOS RÁPIDOS",
            color = JarvisCyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Cyan Tech Rule Line ending in arrow
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(JarvisCyanPrimary, JarvisCyanDim.copy(alpha = 0.2f))
                    )
                )
        )
        Text(
            text = "►",
            color = JarvisCyanDim,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun QuickCommandsGrid(
    isTorchOn: Boolean,
    onCommandClick: (String) -> Unit
) {
    val commands = listOf(
        Triple("diag", "Diagnóstico", Icons.Default.MonitorHeart),
        Triple("torch", if (isTorchOn) "Linterna [ON]" else "Linterna", Icons.Default.FlashlightOn),
        Triple("battery", "Batería", Icons.Default.BatteryChargingFull),
        Triple("alarm", "Alarma 07:00", Icons.Default.Alarm),
        Triple("apps", "Aplicaciones", Icons.Default.GridView),
        Triple("nav", "Navegación", Icons.Default.LocationOn),
        Triple("notes", "Notas", Icons.Default.Description),
        Triple("settings", "Configuración", Icons.Default.Settings)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0..3) {
                val (id, label, icon) = commands[i]
                QuickCommandCard(
                    id = id,
                    label = label,
                    icon = icon,
                    modifier = Modifier.weight(1f),
                    onClick = { onCommandClick(id) }
                )
            }
        }

        // Row 2 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 4..7) {
                val (id, label, icon) = commands[i]
                QuickCommandCard(
                    id = id,
                    label = label,
                    icon = icon,
                    modifier = Modifier.weight(1f),
                    onClick = { onCommandClick(id) }
                )
            }
        }
    }
}

@Composable
private fun QuickCommandCard(
    id: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(70.dp)
            .clickable(onClick = onClick)
            .testTag("quick_card_$id"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF061325),
        border = BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyanPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = JarvisTextPrimary,
                fontSize = 9.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Bottom Response & Status HUD Card (Replaces text typing bar)
// ---------------------------------------------------------------------------
@Composable
private fun BottomJarvisStatusCard(
    message: String,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bottom_jarvis_status_card"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF041022),
        border = BorderStroke(1.dp, JarvisCyanPrimary.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Speech Icon Badge
            Surface(
                shape = CircleShape,
                color = Color(0xFF020C18),
                border = BorderStroke(1.2.dp, JarvisCyanPrimary),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "JARVIS",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text message body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "JARVIS",
                    color = JarvisCyanPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Mini Waveform on Right
            MiniWaveformBars(isSpeaking = isSpeaking)
        }
    }
}

@Composable
private fun MiniWaveformBars(isSpeaking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "MiniWave")
    val barProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniWave"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(18.dp)
    ) {
        val heights = if (isSpeaking) {
            listOf(6f, 14f, 18f, 10f, 4f)
        } else {
            listOf(3f, 6f, 8f, 5f, 3f)
        }

        heights.forEachIndexed { index, base ->
            val scale = if (isSpeaking) (1f + (barProgress * 0.4f)) else 1f
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((base * scale).dp)
                    .background(JarvisCyanPrimary, RoundedCornerShape(1.dp))
            )
        }
    }
}

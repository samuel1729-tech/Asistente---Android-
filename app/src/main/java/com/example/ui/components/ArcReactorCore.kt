package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisCyanSecondary
import com.example.ui.theme.JarvisSurfaceElevated
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class CoreState {
    IDLE,
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING
}

@Composable
fun ArcReactorCore(
    state: CoreState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    // Slow clockwise rotation for outer ring
    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAngle"
    )

    // Counter-clockwise rotation for inner ring
    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerAngle"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Fast reaction pulse for listening/speaking
    val activeReaction = remember { Animatable(1f) }
    LaunchedEffect(state, amplitude) {
        if (state == CoreState.LISTENING || state == CoreState.SPEAKING) {
            val target = 1f + (amplitude * 0.45f)
            activeReaction.animateTo(target, tween(80))
        } else {
            activeReaction.snapTo(1f)
        }
    }

    val stateColor = when (state) {
        CoreState.IDLE -> JarvisCyanPrimary
        CoreState.STANDBY -> Color(0xFF00E5FF)
        CoreState.LISTENING -> Color(0xFF38BDF8)
        CoreState.PROCESSING -> Color(0xFFF59E0B)
        CoreState.SPEAKING -> Color(0xFF00F0FF)
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.9f
            val dynamicScale = if (state == CoreState.IDLE) breathingPulse else activeReaction.value

            // 1. Outer ambient glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(stateColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = radius * 1.15f * dynamicScale
                ),
                radius = radius * 1.15f * dynamicScale,
                center = center
            )

            // Outermost precision cyan guide ring
            drawCircle(
                color = stateColor.copy(alpha = 0.35f),
                radius = radius * 1.02f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Outermost segmented tactical ring
            rotate(outerAngle, pivot = center) {
                val segments = 8
                val sweep = 360f / segments
                val gap = 12f
                for (i in 0 until segments) {
                    val startAngle = i * sweep
                    drawArc(
                        color = JarvisCyanDim.copy(alpha = 0.6f),
                        startAngle = startAngle,
                        sweepAngle = sweep - gap,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Tick notches
                val notchCount = 24
                for (j in 0 until notchCount) {
                    val angleRad = (j * (360f / notchCount)) * (PI / 180.0)
                    val r1 = radius - 4.dp.toPx()
                    val r2 = radius - 10.dp.toPx()
                    val p1 = Offset((center.x + r1 * cos(angleRad)).toFloat(), (center.y + r1 * sin(angleRad)).toFloat())
                    val p2 = Offset((center.x + r2 * cos(angleRad)).toFloat(), (center.y + r2 * sin(angleRad)).toFloat())
                    drawLine(
                        color = stateColor.copy(alpha = 0.45f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // 3. Middle high-tech dashed orbit ring
            rotate(innerAngle, pivot = center) {
                val midRadius = radius * 0.72f
                drawCircle(
                    color = stateColor.copy(alpha = 0.85f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                    )
                )

                // 4 Tactical chevron nodes
                for (k in 0..3) {
                    val angleRad = (k * 90f) * (PI / 180.0)
                    val nodePos = Offset(
                        (center.x + midRadius * cos(angleRad)).toFloat(),
                        (center.y + midRadius * sin(angleRad)).toFloat()
                    )
                    drawCircle(
                        color = stateColor,
                        radius = 4.dp.toPx(),
                        center = nodePos
                    )
                }
            }

            // 4. Inner core chamber (Arc Reactor Heart)
            val coreRadius = radius * 0.45f * dynamicScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stateColor,
                        stateColor.copy(alpha = 0.8f),
                        JarvisSurfaceElevated
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 5. High-intensity bright center node with cyan lens glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        stateColor,
                        stateColor.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 0.45f
                ),
                radius = coreRadius * 0.45f,
                center = center
            )

            // 6. Crosshair target ticks (Horizontal & Vertical)
            val crosshairLen = radius * 0.25f
            drawLine(
                color = stateColor.copy(alpha = 0.8f),
                start = Offset(center.x - radius * 0.7f, center.y),
                end = Offset(center.x - radius * 0.7f + crosshairLen, center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = stateColor.copy(alpha = 0.8f),
                start = Offset(center.x + radius * 0.7f - crosshairLen, center.y),
                end = Offset(center.x + radius * 0.7f, center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = stateColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - radius * 0.7f),
                end = Offset(center.x, center.y - radius * 0.7f + crosshairLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = stateColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y + radius * 0.7f - crosshairLen),
                end = Offset(center.x, center.y + radius * 0.7f),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Center visual icon overlay for active states
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                CoreState.LISTENING -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Escuchando",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                CoreState.SPEAKING -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Hablando",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                else -> {
                    // Clean glowing center matching Iron Man lens flare (no obstructive text)
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TacticalNoteEntity
import com.example.domain.JarvisToolItem
import com.example.domain.JarvisToolRegistry
import com.example.domain.PendingAction
import com.example.domain.ToolCategory
import com.example.domain.ToolStatus
import com.example.ui.MainViewModel
import com.example.ui.components.HudCard
import com.example.ui.theme.JarvisAccentAmber
import com.example.ui.theme.JarvisAccentBlue
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisCyanSecondary
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tools = remember { JarvisToolRegistry.tools }
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showNoteDialog by remember { mutableStateOf(false) }
    var selectedPlannedTool by remember { mutableStateOf<JarvisToolItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CENTRO DE OPERACIONES Y HERRAMIENTAS",
                        color = JarvisCyanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Capacidades nativas verificadas de Android",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = { showNoteDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JarvisCyanPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = JarvisSurfaceElevated,
                        contentColor = JarvisCyanPrimary
                    ),
                    modifier = Modifier.testTag("add_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Directiva",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DIRECTIVA",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Hardware Tools Group ---
        item {
            CategoryHeader("SUBSISTEMAS DE HARDWARE")
        }

        // Flashlight Tool Card
        item {
            ToolCardItem(
                title = "Linterna Táctica",
                description = if (isTorchOn) "Emisor flash activo a potencia nominal." else "Emisor apagado en reposo.",
                status = ToolStatus.OPERATIONAL,
                actionLabel = if (isTorchOn) "APAGAR LINTERNA" else "ENCENDER LINTERNA",
                onAction = { viewModel.toggleFlashlight() }
            )
        }

        // Battery Tool Card
        item {
            ToolCardItem(
                title = "Celda de Energía",
                description = "Capacidad: ${batteryInfo.percentage}% | ${batteryInfo.statusText} (${batteryInfo.powerSource})",
                status = ToolStatus.OPERATIONAL,
                actionLabel = "ACTUALIZAR TELEMETRÍA",
                onAction = { viewModel.refreshTelemetry() }
            )
        }

        // --- Productivity Tools Group ---
        item {
            CategoryHeader("PRODUCTIVIDAD Y DIRECTIVAS")
        }

        // Alarms Tool Card
        item {
            ToolCardItem(
                title = "Programador de Alarmas / Timer",
                description = "Configuración rápida de alertas y temporizadores mediante el reloj oficial del sistema.",
                status = ToolStatus.OPERATIONAL,
                actionLabel = "PROGRAMAR ALARMA 08:00",
                onAction = {
                    viewModel.executePendingAction(
                        PendingAction.SetAlarm(8, 0, "Alarma JARVIS")
                    )
                }
            )
        }

        // Web Search Tool Card
        item {
            ToolCardItem(
                title = "Búsqueda Satelital Web",
                description = "Consultas e indexación externa a través del navegador predeterminado.",
                status = ToolStatus.OPERATIONAL,
                actionLabel = "CONSULTAR NOTICIAS",
                onAction = {
                    viewModel.executePendingAction(
                        PendingAction.WebSearch("Últimos avances tecnología e inteligencia artificial")
                    )
                }
            )
        }

        // --- System Telemetry Card ---
        item {
            CategoryHeader("DIAGNÓSTICO DEL TERMINAL")
        }

        item {
            HudCard(
                title = "Telemetría del Dispositivo",
                statusText = "VERIFICADO"
            ) {
                Text(
                    text = "Dispositivo: ${telemetry.deviceModel}",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Sistema: ${telemetry.androidVersion}",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Memoria RAM: ${telemetry.availableRamMb} MB disp. de ${telemetry.totalRamMb} MB",
                    color = JarvisCyanSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Almacenamiento libre: ${telemetry.freeStorageGb} GB",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- Tactical Notes Section ---
        if (notes.isNotEmpty()) {
            item {
                CategoryHeader("REGISTRO DE DIRECTIVAS ACTIVAS (${notes.size})")
            }

            items(notes, key = { it.id }) { note ->
                TacticalNoteCard(
                    note = note,
                    onDelete = { viewModel.deleteNote(note) }
                )
            }
        }

        // --- Planned Integrations Group ---
        item {
            CategoryHeader("FUTURAS INTEGRACIONES Y CONEXIONES")
        }

        items(tools.filter { it.status == ToolStatus.PLANNED_INTEGRATION }) { plannedTool ->
            PlannedToolCard(
                tool = plannedTool,
                onClick = { selectedPlannedTool = plannedTool }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal to create new tactical note
    if (showNoteDialog) {
        CreateNoteDialog(
            onDismiss = { showNoteDialog = false },
            onSave = { title, content, priority ->
                viewModel.saveManualNote(title, content, priority)
                showNoteDialog = false
            }
        )
    }

    // Modal explaining planned integration requirements
    selectedPlannedTool?.let { tool ->
        PlannedToolDetailDialog(
            tool = tool,
            onDismiss = { selectedPlannedTool = null }
        )
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(JarvisCyanPrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = JarvisCyanDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ToolCardItem(
    title: String,
    description: String,
    status: ToolStatus,
    actionLabel: String,
    onAction: () -> Unit
) {
    HudCard(
        title = title,
        statusText = "OPERATIVO"
    ) {
        Text(
            text = description,
            color = JarvisTextPrimary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, JarvisCyanPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = JarvisCyanDim.copy(alpha = 0.15f),
                    contentColor = JarvisCyanPrimary
                )
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TacticalNoteCard(
    note: TacticalNoteEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(note.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))
    }

    HudCard(
        title = note.title,
        statusText = dateStr,
        borderColor = JarvisCyanDim.copy(alpha = 0.5f)
    ) {
        Text(
            text = note.content,
            color = JarvisTextPrimary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = JarvisSurfaceVariant,
                border = BorderStroke(1.dp, JarvisBorder)
            ) {
                Text(
                    text = "PRIORIDAD: ${note.priority}",
                    color = if (note.priority == "HIGH" || note.priority == "CRITICAL") JarvisAccentAmber else JarvisCyanSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar Directiva",
                    tint = JarvisTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlannedToolCard(
    tool: JarvisToolItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = JarvisSurfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, JarvisBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = JarvisTextMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tool.description,
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = JarvisSurfaceElevated,
                border = BorderStroke(1.dp, JarvisBorder)
            ) {
                Text(
                    text = "PRÓXIMO",
                    color = JarvisAccentBlue,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = JarvisSurfaceElevated,
            border = BorderStroke(1.dp, JarvisCyanPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "NUEVA DIRECTIVA TÁCTICA",
                    color = JarvisCyanPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la orden", color = JarvisTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanPrimary,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenido o notas de operación", color = JarvisTextSecondary) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanPrimary,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("CANCELAR", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSave(title, content, "NORMAL")
                            }
                        },
                        border = BorderStroke(1.dp, JarvisCyanPrimary)
                    ) {
                        Text("ARCHIVAR", color = JarvisCyanPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannedToolDetailDialog(
    tool: JarvisToolItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = JarvisSurfaceElevated,
            border = BorderStroke(1.dp, JarvisAccentBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = JarvisAccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tool.title.uppercase(),
                        color = JarvisCyanPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = tool.description,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Estado de integración: La arquitectura modular de JARVIS está preparada para recibir esta conexión mediante servicio desacoplado en una futura actualización.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, JarvisBorder)
                    ) {
                        Text("ENTENDIDO", color = JarvisCyanSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

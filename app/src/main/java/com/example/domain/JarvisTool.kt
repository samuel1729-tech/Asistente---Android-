package com.example.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolStatus {
    OPERATIONAL,
    REQUIRES_PERMISSION,
    PLANNED_INTEGRATION
}

enum class ToolCategory(val displayName: String) {
    HARDWARE("Hardware y Dispositivo"),
    PRODUCTIVITY("Productividad y Agenda"),
    SYSTEM("Diagnóstico y Sistema"),
    CONNECTIVITY("Integraciones y Servicios")
}

data class JarvisToolItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val status: ToolStatus,
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String? = null,
    val isExternal: Boolean = false
)

object JarvisToolRegistry {
    val tools: List<JarvisToolItem> = listOf(
        JarvisToolItem(
            id = "flashlight",
            title = "Linterna Táctica",
            description = "Control de encendido y apagado del LED flash mediante CameraManager.",
            category = ToolCategory.HARDWARE,
            icon = Icons.Default.FlashlightOn,
            status = ToolStatus.OPERATIONAL,
            requiresConfirmation = false
        ),
        JarvisToolItem(
            id = "battery",
            title = "Diagnóstico de Energía",
            description = "Lectura en tiempo real del porcentaje, estado de carga y salud de la batería.",
            category = ToolCategory.SYSTEM,
            icon = Icons.Default.BatteryChargingFull,
            status = ToolStatus.OPERATIONAL
        ),
        JarvisToolItem(
            id = "alarm",
            title = "Programador de Alarmas",
            description = "Configuración rápida de alarmas y temporizadores del sistema.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Alarm,
            status = ToolStatus.OPERATIONAL,
            requiresConfirmation = true,
            confirmationPrompt = "¿Desea que JARVIS proceda a programar la alarma en el reloj del sistema?"
        ),
        JarvisToolItem(
            id = "notes",
            title = "Registro de Directivas",
            description = "Almacenamiento local táctico de notas, órdenes y recordatorios cifrados.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.AutoMirrored.Filled.NoteAdd,
            status = ToolStatus.OPERATIONAL
        ),
        JarvisToolItem(
            id = "web_search",
            title = "Búsqueda Satelital / Web",
            description = "Consulta rápida de información global mediante el navegador predeterminado.",
            category = ToolCategory.PRODUCTIVITY,
            icon = Icons.Default.Search,
            status = ToolStatus.OPERATIONAL
        ),
        JarvisToolItem(
            id = "system_info",
            title = "Telemetría del Terminal",
            description = "Especificaciones del procesador, versión de Android, memoria RAM y almacenamiento.",
            category = ToolCategory.SYSTEM,
            icon = Icons.Default.Info,
            status = ToolStatus.OPERATIONAL
        ),
        JarvisToolItem(
            id = "app_launcher",
            title = "Lanzador de Protocolos / Apps",
            description = "Apertura rápida de aplicaciones instaladas (Cámara, Mapas, YouTube, etc.).",
            category = ToolCategory.SYSTEM,
            icon = Icons.Default.Apps,
            status = ToolStatus.OPERATIONAL,
            requiresConfirmation = true,
            confirmationPrompt = "¿Confirmar apertura de la aplicación externa solicitada?"
        ),
        JarvisToolItem(
            id = "volume_control",
            title = "Monitor de Audio Táctico",
            description = "Verificación de volumen multimedia, timbre y modo silencioso.",
            category = ToolCategory.HARDWARE,
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            status = ToolStatus.OPERATIONAL
        ),
        // Planned Integrations
        JarvisToolItem(
            id = "smart_home",
            title = "Domótica / Smart Home (IoT)",
            description = "Conexión futura a Home Assistant o MQTT para controlar iluminación y actuadores.",
            category = ToolCategory.CONNECTIVITY,
            icon = Icons.Default.SmartToy,
            status = ToolStatus.PLANNED_INTEGRATION
        ),
        JarvisToolItem(
            id = "cloud_sync",
            title = "Sincronización en la Nube",
            description = "Respaldo remoto de directivas e historial en servidor seguro.",
            category = ToolCategory.CONNECTIVITY,
            icon = Icons.Default.Public,
            status = ToolStatus.PLANNED_INTEGRATION
        ),
        JarvisToolItem(
            id = "voice_wake",
            title = "Activación por Voz Continua (Hotword)",
            description = "Detección en segundo plano 'Oye JARVIS' (requiere servicio en primer plano).",
            category = ToolCategory.HARDWARE,
            icon = Icons.Default.Mic,
            status = ToolStatus.PLANNED_INTEGRATION
        ),
        JarvisToolItem(
            id = "system_settings",
            title = "Ajustes de Conectividad",
            description = "Acceso directo a configuraciones de Wi-Fi, Bluetooth y Red móvil.",
            category = ToolCategory.SYSTEM,
            icon = Icons.Default.Settings,
            status = ToolStatus.OPERATIONAL
        )
    )
}

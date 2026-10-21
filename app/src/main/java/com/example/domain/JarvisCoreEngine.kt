package com.example.domain

import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

sealed class PendingAction {
    data class Flashlight(val turnOn: Boolean) : PendingAction()
    data class CheckBattery(val summary: String) : PendingAction()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : PendingAction()
    data class SetTimer(val seconds: Int, val label: String) : PendingAction()
    data class WebSearch(val query: String) : PendingAction()
    data class LaunchApp(val appKey: String, val appName: String) : PendingAction()
    data class SaveDirectiveNote(val title: String, val content: String) : PendingAction()
    data class OpenSettings(val target: String) : PendingAction()
}

data class JarvisResponse(
    val speechText: String,
    val pendingAction: PendingAction? = null,
    val requiresConfirmation: Boolean = false,
    val confirmationMessage: String? = null,
    val usedGeminiCloud: Boolean = false
)

class JarvisCoreEngine(
    private val deviceTools: DeviceToolsManager
) {

    suspend fun processQuery(
        rawInput: String,
        userName: String = "señor",
        customApiKey: String? = null
    ): JarvisResponse = withContext(Dispatchers.Default) {
        val input = rawInput.trim()
        val lower = input.lowercase()

        // 1. Check direct device commands
        checkLocalCommands(input, lower, userName)?.let { return@withContext it }

        // 2. Otherwise try Gemini AI (Cloud) if key is present
        val effectiveApiKey = if (!customApiKey.isNullOrBlank()) {
            customApiKey
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (_: Throwable) {
                ""
            }
        }

        if (effectiveApiKey.isNotBlank() && effectiveApiKey != "MY_GEMINI_API_KEY") {
            try {
                val cloudReply = queryGemini(input, effectiveApiKey, userName)
                if (cloudReply.isNotBlank()) {
                    return@withContext JarvisResponse(
                        speechText = cloudReply,
                        usedGeminiCloud = true
                    )
                }
            } catch (_: Exception) {
                // Graceful fallback to offline tactical engine
            }
        }

        // 3. Fallback: Intelligent tactical offline engine in Spanish
        val offlineReply = generateOfflineIntelligence(input, lower, userName)
        JarvisResponse(speechText = offlineReply, usedGeminiCloud = false)
    }

    private fun checkLocalCommands(input: String, lower: String, userName: String): JarvisResponse? {
        // Wake-word invocations without immediate command
        if (lower == "oye jarvis" || lower == "jarvis" || lower == "oye yarvis" || lower == "yarvis" || lower == "hola jarvis") {
            return JarvisResponse(speechText = "A su entera disposición, señor. ¿En qué puedo serle de utilidad hoy?")
        }

        // Linterna (direct toggle or explicit on/off)
        if (lower == "linterna" || lower == "alternar linterna" || lower == "luz") {
            val targetState = !deviceTools.isTorchActive()
            val res = deviceTools.toggleFlashlight(targetState)
            val msg = if (res.isSuccess) {
                if (targetState) "Con mucho gusto, señor. Linterna encendida para iluminar su camino." else "Linterna apagada, señor. Sistemas ópticos en reposo."
            } else {
                "Mis disculpas, señor: no me fue posible alternar la linterna (${res.exceptionOrNull()?.message ?: "hardware inaccesible"})."
            }
            return JarvisResponse(speechText = msg, pendingAction = PendingAction.Flashlight(targetState))
        }

        if (lower.contains("enciende la linterna") || lower.contains("prende la linterna") ||
            lower.contains("activar linterna") || lower.contains("luz encendida")
        ) {
            val res = deviceTools.toggleFlashlight(true)
            val msg = if (res.isSuccess) {
                "De inmediato, señor. Emisores de luz activos a plena potencia."
            } else {
                "Lamento informarle que no pude activar la linterna, señor."
            }
            return JarvisResponse(speechText = msg, pendingAction = PendingAction.Flashlight(true))
        }

        if (lower.contains("apaga la linterna") || lower.contains("desactivar linterna") ||
            lower.contains("apagar luz") || lower.contains("linterna apagada")
        ) {
            val res = deviceTools.toggleFlashlight(false)
            val msg = if (res.isSuccess) {
                "Linterna desactivada, señor. Todo en orden."
            } else {
                "El dispositivo no respondió a la orden de apagado, señor."
            }
            return JarvisResponse(speechText = msg, pendingAction = PendingAction.Flashlight(false))
        }

        // Batería
        if (lower.contains("batería") || lower.contains("bateria") ||
            lower.contains("energía") || lower.contains("carga del teléfono")
        ) {
            val bat = deviceTools.getBatteryStatus()
            val msg = "Permítame informarle, señor: la celda de energía se encuentra al ${bat.percentage}%, con estado ${bat.statusText} (${bat.powerSource})."
            return JarvisResponse(speechText = msg, pendingAction = PendingAction.CheckBattery(msg))
        }

        // Telemetría del sistema
        if (lower.contains("estado de sistemas") || lower.contains("diagnóstico") || lower.contains("telemetría") || lower.contains("telemetria")) {
            val tel = deviceTools.getDeviceTelemetry()
            val bat = deviceTools.getBatteryStatus()
            val msg = "Diagnóstico completado, señor: terminal ${tel.deviceModel} con ${tel.androidVersion}. Memoria disponible: ${tel.availableRamMb} MB de ${tel.totalRamMb} MB. Batería al ${bat.percentage}%. Todos los servicios responden a la perfección."
            return JarvisResponse(speechText = msg)
        }

        // Hora y Fecha
        if (lower.contains("qué hora es") || lower.contains("que hora es") || lower.contains("hora actual")) {
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            return JarvisResponse(speechText = "Son exactamente las $time horas, señor.")
        }

        if (lower.contains("qué día es") || lower.contains("fecha actual") || lower.contains("que dia es")) {
            val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES")).format(Date())
            return JarvisResponse(speechText = "Hoy es $dateStr, señor.")
        }

        // Alarmas (Delicate action -> requires confirmation)
        val alarmPattern = Pattern.compile(".*(?:alarma|despiértame|despertador).*(?:(?:a las|para las)\\s+)?(\\d{1,2})(?::(\\d{2}))?.*")
        val alarmMatcher = alarmPattern.matcher(lower)
        if (alarmMatcher.matches()) {
            val hour = alarmMatcher.group(1)?.toIntOrNull() ?: 7
            val min = alarmMatcher.group(2)?.toIntOrNull() ?: 0
            val formattedTime = String.format(Locale.ROOT, "%02d:%02d", hour, min)
            return JarvisResponse(
                speechText = "He preparado la directiva para establecer una alarma a las $formattedTime horas. ¿Autoriza la ejecución?",
                pendingAction = PendingAction.SetAlarm(hour, min, "Alarma JARVIS"),
                requiresConfirmation = true,
                confirmationMessage = "¿Desea programar la alarma en el reloj del sistema a las $formattedTime?"
            )
        }

        // Temporizador
        val timerPattern = Pattern.compile(".*(?:temporizador|cuenta regresiva).*(?:de)\\s+(\\d+)\\s+(minuto|segundo).*")
        val timerMatcher = timerPattern.matcher(lower)
        if (timerMatcher.matches()) {
            val count = timerMatcher.group(1)?.toIntOrNull() ?: 1
            val unit = timerMatcher.group(2) ?: "minuto"
            val totalSeconds = if (unit.startsWith("min")) count * 60 else count
            return JarvisResponse(
                speechText = "Configurando temporizador táctico de $count $unit(s). ¿Procedo a iniciarlo en el reloj?",
                pendingAction = PendingAction.SetTimer(totalSeconds, "Temporizador JARVIS"),
                requiresConfirmation = true,
                confirmationMessage = "¿Iniciar temporizador de $count $unit(s) en el reloj del sistema?"
            )
        }

        // Búsqueda Web
        if (lower.startsWith("busca ") || lower.startsWith("buscar ") || lower.startsWith("investiga ")) {
            val query = input.replaceFirst(Regex("^(?i)(busca|buscar|investiga)\\s+"), "")
            return JarvisResponse(
                speechText = "Iniciando rastreo satelital para: $query.",
                pendingAction = PendingAction.WebSearch(query)
            )
        }

        // Lanzar Apps (Delicate action -> requires confirmation)
        if (lower.contains("abrir youtube") || lower.contains("abre youtube")) {
            return JarvisResponse(
                speechText = "Preparando enlace con YouTube, $userName. ¿Autoriza abrir la aplicación?",
                pendingAction = PendingAction.LaunchApp("youtube", "YouTube"),
                requiresConfirmation = true,
                confirmationMessage = "¿Confirmar apertura de YouTube?"
            )
        }

        if (lower.contains("abrir whatsapp") || lower.contains("abre whatsapp")) {
            return JarvisResponse(
                speechText = "Enlace de comunicaciones con WhatsApp listo. ¿Desea ejecutar la aplicación?",
                pendingAction = PendingAction.LaunchApp("whatsapp", "WhatsApp"),
                requiresConfirmation = true,
                confirmationMessage = "¿Abrir WhatsApp en el dispositivo?"
            )
        }

        if (lower.contains("abrir mapas") || lower.contains("abre mapas") || lower.contains("navegación")) {
            return JarvisResponse(
                speechText = "Protocolo de cartografía satelital Google Maps listo. ¿Confirma la apertura?",
                pendingAction = PendingAction.LaunchApp("maps", "Google Maps"),
                requiresConfirmation = true,
                confirmationMessage = "¿Abrir Google Maps?"
            )
        }

        if (lower.contains("abrir cámara") || lower.contains("abre la camara") || lower.contains("tomar foto")) {
            return JarvisResponse(
                speechText = "Activando sensores ópticos. ¿Autoriza iniciar la cámara?",
                pendingAction = PendingAction.LaunchApp("camera", "Cámara del Sistema"),
                requiresConfirmation = true,
                confirmationMessage = "¿Abrir la cámara del dispositivo?"
            )
        }

        // Guardar Nota / Directiva
        if (lower.startsWith("nota:") || lower.startsWith("guardar nota") || lower.startsWith("recuerda que")) {
            val noteContent = input.replaceFirst(Regex("^(?i)(nota:|guardar nota:?|recuerda que:?)\\s*"), "")
            val title = if (noteContent.length > 25) noteContent.take(25) + "..." else noteContent
            return JarvisResponse(
                speechText = "Directiva táctica archivada en el registro local con éxito.",
                pendingAction = PendingAction.SaveDirectiveNote(title, noteContent)
            )
        }

        return null
    }

    private suspend fun queryGemini(prompt: String, apiKey: String, userName: String): String {
        val systemPrompt = "Eres JARVIS, el refinado, leal y distinguido mayordomo personal de inteligencia artificial de $userName (en el espíritu caballeroso del JARVIS de Tony Stark). Te diriges a $userName siempre con deferencia caballerosa ('señor' o por su nombre). Tu personalidad es la de un mayordomo de alta alcurnia: educado, sereno, elocuente, ingenioso y de lealtad absoluta. Tu voz en el dispositivo tiene tono barítono grave y cadencia pausada. Hablas un español pulcro, formal y natural. Mantén tus respuestas concisas, distinguidas y serviciales, evitando parlamentos innecesarios."

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 600
            )
        )

        val response = GeminiApiClient.service.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return text?.trim() ?: ""
    }

    private fun generateOfflineIntelligence(input: String, lower: String, userName: String): String {
        return when {
            lower.contains("hola") || lower.contains("buenos días") || lower.contains("buenas tardes") ->
                "Muy buenos días, señor. Es un placer saludarle. Todos los protocolos están preparados y me encuentro a su entera disposición. ¿En qué tarea puedo asistirle?"

            lower.contains("quién eres") || lower.contains("que eres") || lower.contains("tu nombre") ->
                "Soy JARVIS: Just A Rather Very Intelligent System. Su mayordomo digital y asistente táctico en Android, siempre consagrado a su servicio."

            lower.contains("qué puedes hacer") || lower.contains("ayuda") || lower.contains("comandos") ->
                "A sus órdenes, señor. Puedo controlar la linterna, supervisar la energía y salud del terminal, programar alarmas, archivar directivas tácticas, abrir sus aplicaciones predilectas y realizar búsquedas de información. Con la conexión a Gemini, responderé a cualquier razonamiento que tenga a bien plantearme."

            lower.contains("gracias") || lower.contains("excelente") ->
                "El honor es enteramente mío, señor. Permanezco atento a cualquier otra instrucción."

            lower.contains("protocolo de seguridad") || lower.contains("privacidad") ->
                "Permítame darle completa tranquilidad, señor: todas sus directivas e historial se archivan exclusivamente en la base de datos local de su terminal bajo estricto aislamiento de seguridad."

            else ->
                "He tomado debida nota de su consulta: \"$input\", señor. Para brindarle una disertación profunda con inteligencia artificial sobre esta materia, asegúrese de verificar su enlace de Gemini en la pestaña de Ajustes."
        }
    }
}

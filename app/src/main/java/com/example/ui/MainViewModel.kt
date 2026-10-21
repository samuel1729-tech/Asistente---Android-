package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.TacticalNoteEntity
import com.example.domain.BatteryInfo
import com.example.domain.DeviceTelemetry
import com.example.domain.DeviceToolsManager
import com.example.domain.JarvisCoreEngine
import com.example.domain.NetworkStatus
import com.example.domain.PendingAction
import com.example.domain.VoiceManager
import com.example.ui.components.CoreState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JarvisTab {
    CORE,
    TOOLS,
    HISTORY,
    SETTINGS
}

data class ConfirmationRequest(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application)
    private val chatDao = db.chatMessageDao()
    private val notesDao = db.tacticalNoteDao()

    val deviceTools = DeviceToolsManager(application)
    val voiceManager = VoiceManager(application)
    private val coreEngine = JarvisCoreEngine(deviceTools)

    private val prefs = application.getSharedPreferences("jarvis_tactical_prefs", Context.MODE_PRIVATE)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(JarvisTab.CORE)
    val currentTab: StateFlow<JarvisTab> = _currentTab.asStateFlow()

    // Core AI State
    private val _coreState = MutableStateFlow(CoreState.IDLE)
    val coreState: StateFlow<CoreState> = _coreState.asStateFlow()

    // Battery & Device Telemetry
    private val _batteryInfo = MutableStateFlow(deviceTools.getBatteryStatus())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _telemetry = MutableStateFlow(deviceTools.getDeviceTelemetry())
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    // Network Status
    private val _networkStatus = MutableStateFlow(deviceTools.getNetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    // Torch State
    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    // User Settings - default Samu as per Iron Man aesthetic
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Samu") ?: "Samu")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _speechTtsEnabled = MutableStateFlow(prefs.getBoolean("tts_enabled", true))
    val speechTtsEnabled: StateFlow<Boolean> = _speechTtsEnabled.asStateFlow()

    private val _voicePitch = MutableStateFlow(prefs.getFloat("voice_pitch", 0.74f))
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(prefs.getFloat("voice_rate", 0.90f))
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Chat history flow from Room
    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tactical notes flow from Room
    val notes: StateFlow<List<TacticalNoteEntity>> = notesDao.getAllNotesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending confirmation dialog
    private val _confirmationDialog = MutableStateFlow<ConfirmationRequest?>(null)
    val confirmationDialog: StateFlow<ConfirmationRequest?> = _confirmationDialog.asStateFlow()

    // Intent events to trigger in Activity
    private val _intentEvents = MutableSharedFlow<Intent>()
    val intentEvents: SharedFlow<Intent> = _intentEvents.asSharedFlow()

    // Toast/Snackbar notifications
    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        // Collect voice state to update AI Core animation state
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _coreState.value = CoreState.SPEAKING
                } else if (_coreState.value == CoreState.SPEAKING) {
                    _coreState.value = if (voiceManager.isStandbyModeActive.value) CoreState.STANDBY else CoreState.IDLE
                }
            }
        }

        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    _coreState.value = CoreState.LISTENING
                } else if (_coreState.value == CoreState.LISTENING) {
                    _coreState.value = if (voiceManager.isStandbyModeActive.value) CoreState.STANDBY else CoreState.IDLE
                }
            }
        }

        viewModelScope.launch {
            voiceManager.isStandbyModeActive.collect { active ->
                if (active && _coreState.value == CoreState.IDLE) {
                    _coreState.value = CoreState.STANDBY
                } else if (!active && _coreState.value == CoreState.STANDBY) {
                    _coreState.value = CoreState.IDLE
                }
            }
        }

        // Insert initial greeting if history is empty
        viewModelScope.launch {
            val count = chatDao.getRecentMessages(1).size
            if (count == 0) {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "JARVIS",
                        content = "Sistemas en línea, ${_userName.value}. Protocolo táctico iniciado y telemetría al 100%. A la espera de sus órdenes."
                    )
                )
            }
        }
    }

    fun selectTab(tab: JarvisTab) {
        _currentTab.value = tab
    }

    fun updateUserName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
    }

    fun toggleSpeechTts(enabled: Boolean) {
        _speechTtsEnabled.value = enabled
        prefs.edit().putBoolean("tts_enabled", enabled).apply()
        if (!enabled) {
            voiceManager.stopSpeaking()
        }
    }

    fun updateCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
    }

    fun refreshTelemetry() {
        _batteryInfo.value = deviceTools.getBatteryStatus()
        _telemetry.value = deviceTools.getDeviceTelemetry()
        _networkStatus.value = deviceTools.getNetworkStatus()
    }

    fun toggleFlashlight() {
        val res = deviceTools.toggleFlashlight()
        if (res.isSuccess) {
            _isTorchOn.value = res.getOrDefault(false)
            emitToast("Linterna: ${if (_isTorchOn.value) "ENCENDIDA" else "APAGADA"}")
        } else {
            emitToast(res.exceptionOrNull()?.message ?: "Error al controlar linterna")
        }
    }

    fun triggerVoiceInput() {
        if (_coreState.value == CoreState.LISTENING) {
            voiceManager.stopListening()
            _coreState.value = CoreState.IDLE
            return
        }

        voiceManager.startListening(
            onResult = { recognizedText ->
                processUserMessage(recognizedText)
            },
            onError = { errorMsg ->
                emitToast(errorMsg)
                _coreState.value = CoreState.IDLE
            }
        )
    }

    fun toggleStandbyMode(enable: Boolean) {
        if (enable) {
            voiceManager.startStandbyListening(
                onCommandReceived = { command ->
                    processUserMessage(command)
                },
                onError = { err ->
                    emitToast(err)
                }
            )
            _coreState.value = CoreState.STANDBY
            emitToast("Modo Centinela activado: Diga «Oye JARVIS»")
        } else {
            voiceManager.stopStandbyListening()
            _coreState.value = CoreState.IDLE
            emitToast("Modo Centinela desactivado")
        }
    }

    fun openTtsSettings() {
        viewModelScope.launch {
            val intent = voiceManager.createTtsSettingsIntent()
            _intentEvents.emit(intent)
        }
    }

    fun refreshVoiceConfiguration() {
        voiceManager.configureVoiceAndPitch(_voicePitch.value, _voiceRate.value)
        emitToast("Configuración de síntesis vocal actualizada")
    }

    fun applyButlerVoicePreset() {
        _voicePitch.value = 0.74f
        _voiceRate.value = 0.90f
        prefs.edit().putFloat("voice_pitch", 0.74f).putFloat("voice_rate", 0.90f).apply()
        voiceManager.setButlerVoicePreset()
        emitToast("Voz de Mayordomo activada: Barítono 0.74x, Ritmo 0.90x")
    }

    fun applyTacticalVoicePreset() {
        _voicePitch.value = 0.78f
        _voiceRate.value = 0.98f
        prefs.edit().putFloat("voice_pitch", 0.78f).putFloat("voice_rate", 0.98f).apply()
        voiceManager.setTacticalVoicePreset()
        emitToast("Voz Táctica activada: Barítono 0.78x, Ritmo 0.98x")
    }

    fun updateVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        prefs.edit().putFloat("voice_pitch", pitch).apply()
        voiceManager.configureVoiceAndPitch(pitch, _voiceRate.value)
    }

    fun updateVoiceRate(rate: Float) {
        _voiceRate.value = rate
        prefs.edit().putFloat("voice_rate", rate).apply()
        voiceManager.configureVoiceAndPitch(_voicePitch.value, rate)
    }

    fun testButlerVoice() {
        voiceManager.speak("A su entera disposición, ${_userName.value}. Permítame confirmarle que todos los sistemas del terminal operan con absoluta normalidad.")
    }

    fun processUserMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. Save user message to Room
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "USER",
                    content = text
                )
            )

            _coreState.value = CoreState.PROCESSING

            // 2. Process via Jarvis Core Engine
            val response = coreEngine.processQuery(
                rawInput = text,
                userName = _userName.value,
                customApiKey = _customApiKey.value
            )

            // 3. Save JARVIS response
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    content = response.speechText,
                    toolInvoked = response.pendingAction?.let { it::class.simpleName },
                    isDelicateAction = response.requiresConfirmation
                )
            )

            // 4. Speak response if enabled
            if (_speechTtsEnabled.value) {
                voiceManager.speak(response.speechText)
            } else {
                _coreState.value = if (voiceManager.isStandbyModeActive.value) CoreState.STANDBY else CoreState.IDLE
            }

            // 5. Handle pending actions or ask confirmation
            if (response.pendingAction != null) {
                if (response.requiresConfirmation && response.confirmationMessage != null) {
                    _confirmationDialog.value = ConfirmationRequest(
                        title = "AUTORIZACIÓN DE DIRECTIVA",
                        message = response.confirmationMessage,
                        onConfirm = {
                            executePendingAction(response.pendingAction)
                            dismissConfirmationDialog()
                        }
                    )
                } else {
                    executePendingAction(response.pendingAction)
                }
            }
        }
    }

    fun executePendingAction(action: PendingAction) {
        viewModelScope.launch {
            when (action) {
                is PendingAction.Flashlight -> {
                    deviceTools.toggleFlashlight(action.turnOn)
                    _isTorchOn.value = action.turnOn
                }
                is PendingAction.CheckBattery -> {
                    refreshTelemetry()
                }
                is PendingAction.SetAlarm -> {
                    val intent = deviceTools.createAlarmIntent(action.hour, action.minute, action.label)
                    _intentEvents.emit(intent)
                }
                is PendingAction.SetTimer -> {
                    val intent = deviceTools.createTimerIntent(action.seconds, action.label)
                    _intentEvents.emit(intent)
                }
                is PendingAction.WebSearch -> {
                    val intent = deviceTools.createWebSearchIntent(action.query)
                    _intentEvents.emit(intent)
                }
                is PendingAction.LaunchApp -> {
                    val intent = deviceTools.createAppLaunchIntent(action.appKey)
                    if (intent != null) {
                        _intentEvents.emit(intent)
                    } else {
                        emitToast("No se encontró la aplicación para el protocolo ${action.appName}")
                    }
                }
                is PendingAction.SaveDirectiveNote -> {
                    notesDao.insertNote(
                        TacticalNoteEntity(
                            title = action.title,
                            content = action.content,
                            priority = "HIGH"
                        )
                    )
                    emitToast("Directiva táctica guardada localmente")
                }
                is PendingAction.OpenSettings -> {
                    val intent = deviceTools.createAppLaunchIntent("settings")
                    if (intent != null) _intentEvents.emit(intent)
                }
            }
        }
    }

    fun saveManualNote(title: String, content: String, priority: String = "NORMAL") {
        viewModelScope.launch {
            notesDao.insertNote(
                TacticalNoteEntity(
                    title = title,
                    content = content,
                    priority = priority
                )
            )
            emitToast("Directiva añadida")
        }
    }

    fun deleteNote(note: TacticalNoteEntity) {
        viewModelScope.launch {
            notesDao.deleteNote(note)
            emitToast("Directiva eliminada")
        }
    }

    fun requestClearHistory() {
        _confirmationDialog.value = ConfirmationRequest(
            title = "PURGAR REGISTRO",
            message = "¿Está seguro de eliminar todo el historial de conversaciones de JARVIS? Esta acción es irreversible.",
            onConfirm = {
                viewModelScope.launch {
                    chatDao.clearHistory()
                    emitToast("Registro de comunicaciones purgado")
                }
                dismissConfirmationDialog()
            }
        )
    }

    fun dismissConfirmationDialog() {
        _confirmationDialog.value = null
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvents.emit(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}

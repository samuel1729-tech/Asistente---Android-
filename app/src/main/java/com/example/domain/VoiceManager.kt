package com.example.domain

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()

    // --- Standby Wake-Word ("Oye JARVIS") State ---
    private val _isStandbyModeActive = MutableStateFlow(false)
    val isStandbyModeActive: StateFlow<Boolean> = _isStandbyModeActive.asStateFlow()

    private val _standbyStatusText = MutableStateFlow("MODO CENTINELA: INACTIVO")
    val standbyStatusText: StateFlow<String> = _standbyStatusText.asStateFlow()

    // --- Voice Diagnostics State ---
    private val _isMasculineVoiceSelected = MutableStateFlow(false)
    val isMasculineVoiceSelected: StateFlow<Boolean> = _isMasculineVoiceSelected.asStateFlow()

    private val _voiceDiagnostics = MutableStateFlow("Inicializando motor de voz...")
    val voiceDiagnostics: StateFlow<String> = _voiceDiagnostics.asStateFlow()

    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var onSpeechErrorCallback: ((String) -> Unit)? = null
    private var onStandbyCommandCallback: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    private val _voicePitch = MutableStateFlow(0.74f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(0.90f)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureVoiceAndPitch(_voicePitch.value, _voiceRate.value)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    // If standby mode is active, resume listening after JARVIS finishes speaking
                    if (_isStandbyModeActive.value) {
                        mainHandler.postDelayed({
                            if (_isStandbyModeActive.value && !_isSpeaking.value) {
                                restartStandbyRecognizer()
                            }
                        }, 500)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (_isStandbyModeActive.value) {
                        mainHandler.postDelayed({
                            if (_isStandbyModeActive.value && !_isSpeaking.value) {
                                restartStandbyRecognizer()
                            }
                        }, 500)
                    }
                }
            })
            _isTtsReady.value = true
        } else {
            _isTtsReady.value = false
            _voiceDiagnostics.value = "Error al inicializar el motor TextToSpeech."
        }
    }

    /**
     * Configures the butler voice (baritone pitch ~0.74f, calm pace ~0.90f)
     * and inspects the TTS engine for masculine voice profiles in Spanish.
     */
    fun configureVoiceAndPitch(pitch: Float = _voicePitch.value, rate: Float = _voiceRate.value) {
        _voicePitch.value = pitch
        _voiceRate.value = rate

        val esLocale = Locale.forLanguageTag("es-ES")
        val mxLocale = Locale.forLanguageTag("es-MX")
        val usLocale = Locale.forLanguageTag("es-US")
        val genLocale = Locale.forLanguageTag("es")

        var activeLocale = esLocale
        var langResult = tts?.setLanguage(esLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            langResult = tts?.setLanguage(mxLocale)
            if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                activeLocale = mxLocale
            } else {
                langResult = tts?.setLanguage(usLocale)
                if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    activeLocale = usLocale
                } else {
                    tts?.setLanguage(genLocale)
                    activeLocale = genLocale
                }
            }
        }

        var foundMale = false
        var assignedVoiceName = "Predeterminada del sistema"

        try {
            val availableVoices = tts?.voices
            if (!availableVoices.isNullOrEmpty()) {
                // Priority 1: Explicitly masculine voice in Spanish (Castilian or Latin American)
                val maleVoice = availableVoices.firstOrNull { voice ->
                    val isSpanish = voice.locale.language.equals("es", ignoreCase = true)
                    val name = voice.name.lowercase()
                    val isMale = (name.contains("male") ||
                                  name.contains("hombre") ||
                                  name.contains("eea") ||
                                  name.contains("eed") ||
                                  name.contains("sfd") ||
                                  name.contains("esf") ||
                                  voice.features.contains("male")) &&
                                 !name.contains("female") &&
                                 !name.contains("mujer") &&
                                 !name.contains("sfb") &&
                                 !name.contains("eee")
                    isSpanish && isMale && !voice.isNetworkConnectionRequired
                } ?: availableVoices.firstOrNull { voice ->
                    val isSpanish = voice.locale.language.equals("es", ignoreCase = true)
                    val name = voice.name.lowercase()
                    (name.contains("male") || name.contains("eea") || name.contains("sfd")) && !name.contains("female")
                } ?: availableVoices.firstOrNull { voice ->
                    voice.locale.language.equals("es", ignoreCase = true) && !voice.isNetworkConnectionRequired
                }

                if (maleVoice != null) {
                    tts?.voice = maleVoice
                    assignedVoiceName = maleVoice.name
                    val lowerName = maleVoice.name.lowercase()
                    foundMale = lowerName.contains("male") ||
                                lowerName.contains("hombre") ||
                                lowerName.contains("eea") ||
                                lowerName.contains("eed") ||
                                lowerName.contains("sfd") ||
                                lowerName.contains("esf") ||
                                maleVoice.features.contains("male")
                }
            }
        } catch (_: Exception) {
            // Fallback to basic TTS
        }

        // Apply dignified Butler acoustics: Baritone pitch & calm, unhurried cadence
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        _isMasculineVoiceSelected.value = foundMale
        val localeTag = activeLocale.toLanguageTag()
        if (foundMale) {
            _voiceDiagnostics.value = "Mayordomo activo // Voz: $assignedVoiceName [$localeTag] (Tono barítono ${String.format(Locale.ROOT, "%.2f", pitch)}x, Cadencia ${String.format(Locale.ROOT, "%.2f", rate)}x)"
        } else {
            _voiceDiagnostics.value = "Modulación de Mayordomo barítono ${String.format(Locale.ROOT, "%.2f", pitch)}x [$localeTag]. Para voz masculina nativa de Google, configure Ajustes de Voz."
        }
    }

    fun setButlerVoicePreset() {
        configureVoiceAndPitch(pitch = 0.74f, rate = 0.90f)
    }

    fun setTacticalVoicePreset() {
        configureVoiceAndPitch(pitch = 0.78f, rate = 0.98f)
    }

    fun refreshVoiceConfiguration() {
        if (_isTtsReady.value) {
            configureVoiceAndPitch(_voicePitch.value, _voiceRate.value)
        }
    }

    fun speak(text: String, utteranceId: String = "JARVIS_RESPONSE") {
        if (_isTtsReady.value && text.isNotBlank()) {
            tts?.stop()
            val cleanText = text.replace(Regex("[*#_`~]"), "") // Strip markdown symbols for speech
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    // --- Standard Manual Voice Input (Tap-To-Talk) ---
    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isSpeechRecognitionAvailable()) {
            onError("Reconocimiento de voz no soportado en este dispositivo")
            return
        }

        stopSpeaking()
        onSpeechResultCallback = onResult
        onSpeechErrorCallback = onError

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _voiceAmplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Error en captura de audio"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se identificó ningún comando claro"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Sin conexión para procesar voz"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono no concedido"
                            else -> "Reconocimiento finalizado"
                        }
                        onSpeechErrorCallback?.invoke(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onSpeechResultCallback?.invoke(text)
                        } else {
                            onSpeechErrorCallback?.invoke("No se detectaron palabras")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Fallo al inicializar micrófono: ${e.localizedMessage}")
        }
    }

    // --- Standby Wake-Word Sentinel Mode («Oye JARVIS») ---
    fun startStandbyListening(
        onCommandReceived: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isSpeechRecognitionAvailable()) {
            onError("Reconocimiento de voz no soportado en este dispositivo")
            return
        }

        stopSpeaking()
        _isStandbyModeActive.value = true
        _standbyStatusText.value = "CENTINELA // A LA ESCUCHA DE «OYE JARVIS»..."
        onStandbyCommandCallback = onCommandReceived
        restartStandbyRecognizer()
    }

    private fun restartStandbyRecognizer() {
        if (!_isStandbyModeActive.value || _isSpeaking.value) return

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _voiceAmplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f

                        // In standby mode, silence/no-match errors are expected. Auto-restart smoothly!
                        if (_isStandbyModeActive.value && !_isSpeaking.value) {
                            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                                _isStandbyModeActive.value = false
                                _standbyStatusText.value = "CENTINELA DETENIDO // REQUIERE PERMISO"
                            } else {
                                mainHandler.postDelayed({
                                    if (_isStandbyModeActive.value && !_isSpeaking.value) {
                                        restartStandbyRecognizer()
                                    }
                                }, 350)
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _voiceAmplitude.value = 0f

                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""

                        if (text.isNotBlank() && _isStandbyModeActive.value) {
                            // Check for wake word variants: "oye jarvis", "jarvis", "oye yarvis", "yarvis"
                            val wakeWordPattern = Regex("(?i)\\b(oye\\s+)?(jarvis|yarvis|charvis)\\b")
                            if (wakeWordPattern.containsMatchIn(text)) {
                                triggerHapticPulse()
                                _standbyStatusText.value = "«OYE JARVIS» DETECTADO // PROCESANDO..."

                                // Extract the order after the wake word if any
                                val remainingCommand = text.replaceFirst(
                                    Regex("(?i)^.*?\\b(oye\\s+)?(jarvis|yarvis|charvis)\\b[,:]?\\s*"),
                                    ""
                                ).trim()

                                if (remainingCommand.isNotBlank()) {
                                    onStandbyCommandCallback?.invoke(remainingCommand)
                                } else {
                                    onStandbyCommandCallback?.invoke("oye jarvis")
                                }
                                return
                            }
                        }

                        // If wake word was not present, continue loop seamlessly
                        if (_isStandbyModeActive.value && !_isSpeaking.value) {
                            mainHandler.postDelayed({
                                if (_isStandbyModeActive.value && !_isSpeaking.value) {
                                    restartStandbyRecognizer()
                                }
                            }, 300)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _isListening.value = false
            if (_isStandbyModeActive.value) {
                mainHandler.postDelayed({
                    if (_isStandbyModeActive.value && !_isSpeaking.value) {
                        restartStandbyRecognizer()
                    }
                }, 1000)
            }
        }
    }

    fun stopStandbyListening() {
        _isStandbyModeActive.value = false
        _standbyStatusText.value = "MODO CENTINELA: INACTIVO"
        stopListening()
    }

    private fun triggerHapticPulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        _isListening.value = false
        _voiceAmplitude.value = 0f
    }

    fun createTtsSettingsIntent(): Intent {
        return Intent("com.android.settings.TTS_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun release() {
        stopStandbyListening()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}

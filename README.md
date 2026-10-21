# JARVIS - Asistente Personal Inteligente para Android

Aplicación Android nativa desarrollada en **Kotlin** y **Jetpack Compose**, con una interfaz holográfica inspirada en el sistema táctico JARVIS / Iron Man HUD, interacción por voz exclusiva, síntesis vocal de estilo mayordomo y control de herramientas reales del dispositivo.

---

## 🚀 Características Principales

* **Interfaz Táctica Futurista (HUD):**
  * Fondo negro profundo (`#02060D`) con paneles azul marino y líneas perimetrales cian holográficas.
  * **Arc Reactor interactivo:** Núcleo con rotación concéntrica suave, retícula de puntería y pulso reactivo en tiempo real a la amplitud de voz.
  * **Paneles laterales de telemetría:** Indicador en vivo de estado de la IA, analizador vocal, porcentaje de batería y estado de red.
  * **100% Interacción por Voz:** Sin teclados ni campos de texto en la pantalla principal; todo el control se realiza mediante el micrófono y la tarjeta de estado.

* **Activación y Control por Voz:**
  * Modo de espera táctico con detección de la frase de invocación *«Oye, JARVIS»*.
  * Botón circular flotante de micrófono para iniciar o detener la escucha en cualquier momento.

* **Síntesis Vocal (Perfil Mayordomo):**
  * Configuración acústica ajustada a un registro barítono/grave (pitch 0.74x) y cadencia pausada y elegante (rate 0.90x).
  * Priorización de voces masculinas en español para una experiencia inmersiva.

* **Comandos Tácticos Reales de Android:**
  * **Linterna:** Enciende y apaga el flash LED físico del teléfono mediante `CameraManager`.
  * **Batería:** Reporte de porcentaje de carga y estado de conexión eléctrica.
  * **Alarma:** Disparo de `AlarmClock.ACTION_SET_ALARM` al reloj del sistema.
  * **Navegación:** Apertura de Google Maps u otras aplicaciones de mapas vía `geo:` intent.
  * **Notas Tácticas:** Base de datos local SQLite con persistencia de notas y conversaciones mediante **Android Room**.

* **Cerebro Inteligente (Híbrido):**
  * Respuestas sin conexión inmediatas para comandos del sistema y cortesía.
  * Soporte opcional para Gemini API (Google Generative AI) ingresando la clave API en Ajustes.

---

## 🛠️ Tecnologías y Arquitectura

* **Lenguaje:** Kotlin 2.0+
* **UI Toolkit:** Jetpack Compose con Material Design 3 (M3)
* **Arquitectura:** MVVM (Model-View-ViewModel) con Kotlin Coroutines y StateFlow
* **Base de Datos:** Room Database (SQLite) con DAOs reactivos
* **Audio & Voz:** `SpeechRecognizer` de Android y `TextToSpeech` nativo
* **Control de Hardware:** `CameraManager`, `BatteryManager`, `ConnectivityManager`

---

## 📱 Cómo compilar y ejecutar en Android Studio

1. **Requisitos:**
   * Android Studio (versión Koala, Ladybug o superior).
   * Java Development Kit (JDK) 17 o superior.
   * Dispositivo Android físico o emulador con Android 8.0 (API 26) o superior (recomendado Android 12+).

2. **Pasos:**
   1. Clona este repositorio o abre la carpeta del proyecto en Android Studio:
      ```bash
      git clone <URL_DEL_REPOSITORIO>
      ```
   2. Abre Android Studio y selecciona **File > Open**, seleccionando la raíz del proyecto.
   3. Espera a que Gradle sincronice las dependencias automáticamente (`libs.versions.toml`).
   4. Conecta tu dispositivo Android mediante USB (con la depuración USB habilitada en Opciones de desarrollador).
   5. Presiona el botón verde **Run (▶)** para instalar y ejecutar la aplicación.

---

## 🔒 Permisos Requeridos
* `RECORD_AUDIO`: Para el reconocimiento de voz e invocación de comandos.
* `CAMERA`: Exclusivamente para el control del flash LED (linterna) del teléfono.
* `INTERNET` & `ACCESS_NETWORK_STATE`: Para consultas externas de IA y telemetría de red.
* `VIBRATE`: Para retroalimentación táctil al activar el micrófono.

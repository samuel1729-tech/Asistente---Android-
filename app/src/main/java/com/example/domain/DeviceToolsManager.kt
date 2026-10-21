package com.example.domain

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri

data class NetworkStatus(
    val isConnected: Boolean,
    val typeName: String,
    val label: String
)

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val powerSource: String,
    val statusText: String
)

data class DeviceTelemetry(
    val deviceModel: String,
    val androidVersion: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val freeStorageGb: Long,
    val manufacturer: String
)

class DeviceToolsManager(private val context: Context) {

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private var isTorchOn = false

    fun isFlashlightSupported(): Boolean {
        return try {
            val cameraIds = cameraManager?.cameraIdList ?: emptyArray()
            cameraIds.any { id ->
                cameraManager?.getCameraCharacteristics(id)
                    ?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun toggleFlashlight(forceState: Boolean? = null): Result<Boolean> {
        return try {
            val newState = forceState ?: !isTorchOn
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager?.getCameraCharacteristics(id)
                    ?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(cameraId, newState)
                isTorchOn = newState
                Result.success(newState)
            } else {
                Result.failure(Exception("Hardware de linterna no disponible en este dispositivo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isTorchActive(): Boolean = isTorchOn

    fun getBatteryStatus(): BatteryInfo {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val status = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL
            val plugged = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando celda de energía"
                BatteryManager.BATTERY_STATUS_FULL -> "Carga máxima completada"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "En descarga operativa"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Conectado, sin carga activa"
                else -> "Estado nominal"
            }

            BatteryInfo(
                percentage = if (level in 0..100) level else 85,
                isCharging = isCharging,
                powerSource = if (isCharging) "Fuente Externa CA/USB" else "Batería Interna",
                statusText = statusText
            )
        } catch (_: Exception) {
            BatteryInfo(85, false, "Batería Interna", "Lectura estándar")
        }
    }

    fun getNetworkStatus(): NetworkStatus {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNet)
            if (caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                 caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                 caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                val type = if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "WIFI"
                           else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "MÓVIL"
                           else "RED"
                NetworkStatus(isConnected = true, typeName = type, label = "CONECTADO")
            } else {
                NetworkStatus(isConnected = false, typeName = "RED", label = "OFFLINE")
            }
        } catch (_: Exception) {
            NetworkStatus(isConnected = true, typeName = "RED", label = "CONECTADO")
        }
    }

    fun getDeviceTelemetry(): DeviceTelemetry {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)

        var freeStorageGb: Long = 0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            freeStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
        } catch (_: Exception) {}

        return DeviceTelemetry(
            deviceModel = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            freeStorageGb = freeStorageGb,
            manufacturer = Build.MANUFACTURER
        )
    }

    fun createAlarmIntent(hour: Int, minutes: Int, message: String): Intent {
        return Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createTimerIntent(seconds: Int, message: String): Intent {
        return Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createWebSearchIntent(query: String): Intent {
        return Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(android.app.SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createAppLaunchIntent(target: String): Intent? {
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "maps" to "com.google.android.apps.maps",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "camera" to "android.media.action.IMAGE_CAPTURE"
        )

        val pkg = packageMap[target.lowercase()]
        if (pkg != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return launchIntent
            }
        }

        // Generic launch or fallback web
        return when (target.lowercase()) {
            "camera" -> Intent("android.media.action.IMAGE_CAPTURE").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            "settings" -> Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            else -> null
        }
    }
}

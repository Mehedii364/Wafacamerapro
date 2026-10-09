package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wafa_camera_prefs")

data class CameraSettings(
    val flashMode: String = "AUTO", // AUTO, ON, OFF, TORCH
    val gridType: String = "RULE_OF_THIRDS", // NONE, RULE_OF_THIRDS, GOLDEN_RATIO, CENTER_CROSS
    val timerSeconds: Int = 0, // 0, 3, 5, 10
    val aspectRatio: String = "4:3", // 4:3, 16:9, 1:1, FULL
    val touchToCapture: Boolean = false,
    val volumeKeyShutter: Boolean = true,
    val mirrorFrontCamera: Boolean = true,
    val watermarkEnabled: Boolean = false,
    val selectedWatermarkPresetId: Long = 1L,
    val audioEnabledForVideo: Boolean = true,
    val darkTheme: Boolean = true,
    val shootingModesOrder: String = "VIDEO,PHOTO,PORTRAIT,NIGHT,PRO"
)

class CameraPreferencesRepository(private val context: Context) {

    private object Keys {
        val FLASH_MODE = stringPreferencesKey("flash_mode")
        val GRID_TYPE = stringPreferencesKey("grid_type")
        val TIMER_SECONDS = intPreferencesKey("timer_seconds")
        val ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        val TOUCH_TO_CAPTURE = booleanPreferencesKey("touch_to_capture")
        val VOLUME_KEY_SHUTTER = booleanPreferencesKey("volume_key_shutter")
        val MIRROR_FRONT = booleanPreferencesKey("mirror_front")
        val WATERMARK_ENABLED = booleanPreferencesKey("watermark_enabled")
        val WATERMARK_PRESET_ID = longPreferencesKey("watermark_preset_id")
        val AUDIO_VIDEO = booleanPreferencesKey("audio_video")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SHOOTING_MODES_ORDER = stringPreferencesKey("shooting_modes_order")
    }

    val settingsFlow: Flow<CameraSettings> = context.dataStore.data.map { prefs ->
        CameraSettings(
            flashMode = prefs[Keys.FLASH_MODE] ?: "AUTO",
            gridType = prefs[Keys.GRID_TYPE] ?: "RULE_OF_THIRDS",
            timerSeconds = prefs[Keys.TIMER_SECONDS] ?: 0,
            aspectRatio = prefs[Keys.ASPECT_RATIO] ?: "4:3",
            touchToCapture = prefs[Keys.TOUCH_TO_CAPTURE] ?: false,
            volumeKeyShutter = prefs[Keys.VOLUME_KEY_SHUTTER] ?: true,
            mirrorFrontCamera = prefs[Keys.MIRROR_FRONT] ?: true,
            watermarkEnabled = prefs[Keys.WATERMARK_ENABLED] ?: false,
            selectedWatermarkPresetId = prefs[Keys.WATERMARK_PRESET_ID] ?: 1L,
            audioEnabledForVideo = prefs[Keys.AUDIO_VIDEO] ?: true,
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            shootingModesOrder = prefs[Keys.SHOOTING_MODES_ORDER] ?: "VIDEO,PHOTO,PORTRAIT,NIGHT,PRO"
        )
    }

    suspend fun updateFlashMode(mode: String) {
        context.dataStore.edit { it[Keys.FLASH_MODE] = mode }
    }

    suspend fun updateGridType(grid: String) {
        context.dataStore.edit { it[Keys.GRID_TYPE] = grid }
    }

    suspend fun updateTimer(seconds: Int) {
        context.dataStore.edit { it[Keys.TIMER_SECONDS] = seconds }
    }

    suspend fun updateAspectRatio(ratio: String) {
        context.dataStore.edit { it[Keys.ASPECT_RATIO] = ratio }
    }

    suspend fun updateTouchToCapture(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TOUCH_TO_CAPTURE] = enabled }
    }

    suspend fun updateVolumeKeyShutter(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOLUME_KEY_SHUTTER] = enabled }
    }

    suspend fun updateMirrorFront(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MIRROR_FRONT] = enabled }
    }

    suspend fun updateWatermarkEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WATERMARK_ENABLED] = enabled }
    }

    suspend fun updateAudioForVideo(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUDIO_VIDEO] = enabled }
    }

    suspend fun updateShootingModesOrder(order: String) {
        context.dataStore.edit { it[Keys.SHOOTING_MODES_ORDER] = order }
    }
}

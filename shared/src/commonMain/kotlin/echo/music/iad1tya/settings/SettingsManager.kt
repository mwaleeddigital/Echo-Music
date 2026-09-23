package echo.music.iad1tya.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(@PublishedApi internal val settings: ObservableSettings) {

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return settings.getBooleanFlow(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        return settings.getString(key, defaultValue)
    }

    fun setString(key: String, value: String) {
        settings.putString(key, value)
    }

    fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return settings.getStringFlow(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return settings.getInt(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        settings.putInt(key, value)
    }

    fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return settings.getIntFlow(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return settings.getFloat(key, defaultValue)
    }

    fun setFloat(key: String, value: Float) {
        settings.putFloat(key, value)
    }

    fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return settings.getFloatFlow(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return settings.getLong(key, defaultValue)
    }

    fun setLong(key: String, value: Long) {
        settings.putLong(key, value)
    }

    fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
        return settings.getLongFlow(key, defaultValue)
    }

    inline fun <reified T : Enum<T>> getEnum(key: String, defaultValue: T): T {
        val name = settings.getStringOrNull(key) ?: return defaultValue
        return try {
            enumValueOf<T>(name)
        } catch (e: Exception) {
            defaultValue
        }
    }

    inline fun <reified T : Enum<T>> setEnum(key: String, value: T) {
        settings.putString(key, value.name)
    }

    inline fun <reified T : Enum<T>> getEnumFlow(key: String, defaultValue: T): Flow<T> {
        return settings.getStringFlow(key, defaultValue.name).map { name ->
            try {
                enumValueOf<T>(name)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    fun remove(key: String) {
        settings.remove(key)
    }

    fun clear() {
        settings.clear()
    }
}

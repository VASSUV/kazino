package ru.vassuv.kazino.repository

import android.content.SharedPreferences
import android.preference.PreferenceManager
import ru.vassuv.kazino.App
import ru.vassuv.kazino.FullscreenActivity

enum class SharedData {
    LOG, LOG_STATE, IS_SAVE_LOG, LOG_FIELD1, LOG_FIELD2,
    CHECK_2_37, CHECK_HOT,
    COUNT_NOT_P;

    private val instance: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(App.context) }

    fun getString() = instance.getString(name, "")
    fun getInt() = instance.getInt(name, 0)
    fun getBoolean() = instance.getBoolean(name, false)
    fun getLong() = instance.getLong(name, 0)

    fun saveString(value: String) = instance.edit().putString(name, value).apply()
    fun saveInt(value: Int) = instance.edit().putInt(name, value).apply()
    fun saveBoolean(value: Boolean) = instance.edit().putBoolean(name, value).apply()
    fun saveLong(value: Long) = instance.edit().putLong(name, value).apply()

    fun remove() = instance.edit().remove(name).apply()
}
package com.sk.revisit.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

enum class KEY {
    ROOT_PATH,
    THEME,
    DN_PATH,
    IS_FIRST,
    USR_AGENT_STR,
    USR_AGENT_DEF,
    MAX_WEB_TIMEOUT,
}

open class MySettingsManager(context: Context) : SettingsHelp(PreferenceManager.getDefaultSharedPreferences(context)) {

    var rootStoragePath: String
        get() = getStr(KEY.ROOT_PATH, "/sdcard/Android/obb/com.sk.revisit")!!
        set(folderPath) = putStr(KEY.ROOT_PATH, folderPath)

    val downloadStoragePath: String?
        get() = getStr(KEY.DN_PATH, null)

    var isFirst: Boolean
        get() = getBoolean(KEY.IS_FIRST, true)
        set(valValue) = putBoolean(KEY.IS_FIRST, valValue)

    fun setTheme(valValue: String) {
        putStr(KEY.THEME, valValue)
    }

    fun setUserAgentCustom(valValue: String) {
        putStr(KEY.USR_AGENT_STR, valValue)
    }
}

open class SettingsHelp(protected val prefs: SharedPreferences) {

    fun getStr(key: KEY, def: String?): String? {
        return prefs.getString(key.name, def)
    }

    fun getBoolean(key: KEY, def: Boolean): Boolean {
        return prefs.getBoolean(key.name, def)
    }

    fun putStr(key: KEY, valValue: String) {
        prefs.edit().putString(key.name, valValue).apply()
    }

    fun putInt(key: KEY, valValue: Int) {
        prefs.edit().putInt(key.name, valValue).apply()
    }

    fun putBoolean(key: KEY, valValue: Boolean) {
        prefs.edit().putBoolean(key.name, valValue).apply()
    }
}

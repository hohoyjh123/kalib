package com.yesjnet.gwanak.storage

import android.content.Context
import android.content.SharedPreferences
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.util.RSACryptor
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date

class SecurePreference(val context: Context) {

    fun clearAll(prefName: String) {
        val pref = getPref(prefName)
        val editor = pref.edit()
        editor.clear()
        editor.commit()
    }

    fun getPref(name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun setStrValue(key: String, value: String, isEncryption: Boolean = false) {
        var temp = value
        if (isEncryption) {
            temp = RSACryptor.instance.encrypt(value)
        }
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putString(key, temp)
        editor.apply()
    }

    fun setIntValue(name: String, value: Int) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putInt(name, value)
        editor.apply()
    }

    fun setLongValue(name: String, value: Long) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putLong(name, value)
        editor.apply()
    }

    fun setFloatValue(name: String, value: Float) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putFloat(name, value)
        editor.apply()
    }

    fun setDoubleValue(name: String, defValue: Double) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putString(name, defValue.toString() + "")
        editor.apply()
    }

    fun setBoolValue(name: String, value: Boolean) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.putBoolean(name, value)
        editor.apply()
    }

    fun getStrValue(name: String, defValue: String, isDecrypt: Boolean = false): String {
        val pref = getPref(DEF_PREF_NAME)
        var str = pref.getString(name, defValue)
        if (str.isNullOrBlank()) {
            str = defValue
        }
        if (isDecrypt) {
            str = RSACryptor.instance.decrypt(str)
        }
        return str
    }

    fun getIntValue(name: String, defValue: Int): Int {
        val pref = getPref(DEF_PREF_NAME)
        return pref.getInt(name, defValue)
    }

    fun getLongValue(name: String, defValue: Long): Long {
        val pref = getPref(DEF_PREF_NAME)
        return pref.getLong(name, defValue)
    }

    fun getFloatValue(name: String, defValue: Float): Float {
        val pref = getPref(DEF_PREF_NAME)
        return pref.getFloat(name, defValue)
    }

    fun getDoubleValue(name: String, defValue: Double): Double {
        val pref = getPref(DEF_PREF_NAME)
        val data = pref.getString(name, "")
        return if (data == "") {
            defValue
        } else data!!.toDouble()
    }

    fun getBoolValue(name: String, defValue: Boolean): Boolean {
        val pref = getPref(DEF_PREF_NAME)
        return pref.getBoolean(name, defValue)
    }

    fun removeValue(name: String) {
        val pref = getPref(DEF_PREF_NAME)
        val editor = pref.edit()
        editor.remove(name)
        editor.apply()
    }

    fun getConfigString(key: String, defaultValue: String): String {
        return getConfigString(key, defaultValue, false)
    }

    fun getConfigString(key: String, defaultValue: String, isDecrypt: Boolean): String {
        val pref = getPref(CONFIG_PREF_NAME)
        var str = pref.getString(key, defaultValue)
        if (str.isNullOrBlank()) {
            str = defaultValue
        }

        if (isDecrypt) {
            str = RSACryptor.instance.decrypt(str)
        }
        return str
    }

    fun setConfigString(key: String, value: String) {
        setConfigString(key, value, false)
    }

    fun setConfigString(key: String, value: String, isEncryption: Boolean) {
        var value = value
        if (isEncryption) {
            value = RSACryptor.instance.encrypt(value)
        }
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setConfigDate(key: String, date: Date) {
        val df = SimpleDateFormat()
        df.applyPattern(ConstsData.API_DATE_PATTERN)

        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putString(key, df.format(date))
        editor.apply()
    }

    fun getConfigDate(key: String, defaultValue: String): Date {
        val pref = getPref(CONFIG_PREF_NAME)
        var str = pref.getString(key, defaultValue)
        val df = SimpleDateFormat()
        df.applyPattern(ConstsData.API_DATE_PATTERN)

        return df.parse(str)
    }

    fun getConfigBool(key: String, defaultValue: Boolean): Boolean {
        val pref = getPref(CONFIG_PREF_NAME)
        return pref.getBoolean(key, defaultValue)
    }

    fun setConfigBool(key: String, value: Boolean) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getConfigInt(key: String, defaultValue: Int): Int {
        val pref = getPref(CONFIG_PREF_NAME)
        return pref.getInt(key, defaultValue)
    }

    fun setConfigInt(key: String, value: Int) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getConfigLong(key: String, defaultValue: Long): Long {
        val pref = getPref(CONFIG_PREF_NAME)
        return pref.getLong(key, defaultValue)
    }

    fun setConfigLong(key: String, value: Long) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getConfigFloat(key: String, defaultValue: Float): Float {
        val pref = getPref(CONFIG_PREF_NAME)
        return pref.getFloat(key, defaultValue)
    }

    fun setConfigFloat(key: String, value: Float) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    inline fun <reified T : Enum<T>> getConfigEnum(key: String, defaultValue: T): T {
        val pref = getPref(CONFIG_PREF_NAME)
        return pref.getInt(key, -1).let { if (it >= 0 ) enumValues<T>()[it] else defaultValue }
    }

    fun <T : Enum<T>> setConfigEnum(key: String, value: T?) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        editor.putInt(key, value?.ordinal ?: -1)
        editor.apply()
    }


    fun setConfigStringArray(key: String, values: ArrayList<String>) {
        val pref = getPref(CONFIG_PREF_NAME)
        val editor = pref.edit()
        val a = JSONArray()
        for (i in values.indices) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    fun getConfigStringArray(key: String): ArrayList<String>? {
        val pref = getPref(CONFIG_PREF_NAME)
        val json = pref.getString(key, null)
        val urls = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optString(i)
                    urls.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return urls
    }

    companion object {
        // 로그아웃시 삭제하면 안되는 캐시파일
        const val DEF_PREF_NAME = "Default"

        // 로그아웃시 삭제하는 캐시파일
        const val CONFIG_PREF_NAME = "Config"
    }
}
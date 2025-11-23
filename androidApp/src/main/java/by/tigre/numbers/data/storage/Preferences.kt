package by.tigre.numbers.data.storage

import android.content.Context
import androidx.core.content.edit

interface Preferences {
    fun saveBoolean(key: String, value: Boolean?)
    fun loadBoolean(key: String, default: Boolean): Boolean

    fun saveLong(key: String, value: Long?)
    fun loadLong(key: String, default: Long): Long

    class Impl(context: Context, name: String) : Preferences {
        private val preference = context.getSharedPreferences(name, Context.MODE_PRIVATE)

        override fun saveBoolean(key: String, value: Boolean?) {
            preference.edit {
                if (value != null) {
                    putBoolean(key, value)
                } else {
                    remove(key)
                }
            }
        }

        override fun loadBoolean(key: String, default: Boolean): Boolean = preference.getBoolean(key, default)

        override fun saveLong(key: String, value: Long?) {
            preference.edit {
                if (value != null) {
                    putLong(key, value)
                } else {
                    remove(key)
                }
            }
        }

        override fun loadLong(key: String, default: Long): Long = preference.getLong(key, default)
    }
}
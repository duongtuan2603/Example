package com.example.scanning.utils

import android.content.Context
import androidx.core.content.edit

object PreferenceManager {
    fun putStringToPreference(context: Context, key: String, value: String) {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString(key, value)
                commit()
            }
        }
    }

    fun putBooleanToPreference(context: Context, key: String, value: Boolean) {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putBoolean(key, value)
                commit()
            }
        }
    }

//    fun putObjectToPreference(context: Context, data: Any) {
//        context.apply {
//            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
//            sharedPreferences.edit {
//                putString(data::class.java.name, Gson().toJson(data))
//                commit()
//            }
//        }
//    }

    fun getStringFromPreference(context: Context, key: String): String? {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, null)
        }
    }

    fun getBooleanFromPreference(context: Context, key: String): Boolean {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(key, false)
        }
    }

//    inline fun <reified T : Any> getObjectFromPreference(context: Context): T? {
//        context.apply {
//            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
//            val data = sharedPreferences.getString(T::class.java.name, "")
//            return Gson().fromJson(data, T::class.java)
//        }
//    }

    fun removeSharedPreference(context: Context, key: String) {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            if (sharedPreferences.contains(key)) {
                sharedPreferences.edit {
                    remove(key)
                    commit()
                }
            }
        }
    }

    fun removeAllSharedPreference(context: Context) {
        context.apply {
            val sharedPreferences = getSharedPreferences("AppSharePreference", Context.MODE_PRIVATE)
            sharedPreferences.all.keys.forEach { k ->
                sharedPreferences.edit {
                    remove(k)
                    commit()
                }
            }
        }
    }


}

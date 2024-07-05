package com.joyce.chessgame

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.joyce.chessgame.base.UserData

object ShareTool {

    private const val USER_DATA = "userData"

    private fun getShare(): SharedPreferences {
        return MyApplication.instance.applicationContext.getSharedPreferences("SaveName", MODE_PRIVATE)
    }

    private fun getEncryptShare(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "EncryptedPrefs",
            masterKeyAlias,
            MyApplication.instance.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveUserData(userData: UserData){
        val userDataJson = Gson().toJson(userData)
        getEncryptShare().edit().putString(USER_DATA, userDataJson).apply()
    }

    fun getUserData(): UserData {
        val userDataJson = getEncryptShare().getString(USER_DATA, null)
        return if (userDataJson != null){
            val type = object : TypeToken<UserData>() {}.type
            Gson().fromJson(userDataJson, type)
        } else {
            UserData()
        }
    }
}
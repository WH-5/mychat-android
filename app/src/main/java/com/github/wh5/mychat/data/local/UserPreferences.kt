package com.github.wh5.mychat.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. 定义一个 Context 扩展属性
val Context.userDataStore by preferencesDataStore(name = "user_prefs")

object LoginPreferences {
    private val TOKEN = stringPreferencesKey("token")
    public val UNIQUE_ID = stringPreferencesKey("unique_id")
    private val PHONE = stringPreferencesKey("phone")
    private val KDF_SALT = stringPreferencesKey("kdf_salt")
    private val PUBLIC_KEY = stringPreferencesKey("public_key")
    private val ENCRYPTED_PRIVATE_KEY = stringPreferencesKey("encrypted_private_key")

    // 2. 保存登录信息
    suspend fun saveLoginInfo(
        context: Context,
        token: String,
        uniqueId: String,
        phone: String,
        kdfSalt: String,
        publicKey: String,
        encryptedPrivateKey: String
    ) {
        context.userDataStore.edit { prefs ->
            prefs[TOKEN] = token
            prefs[UNIQUE_ID] = uniqueId
            prefs[PHONE] = phone
            prefs[KDF_SALT] = kdfSalt
            prefs[PUBLIC_KEY] = publicKey
            prefs[ENCRYPTED_PRIVATE_KEY] = encryptedPrivateKey
        }
    }

    // 3. 获取登录信息（返回 Flow）
    fun getToken(context: Context): Flow<String> = context.userDataStore.data.map { it[TOKEN] ?: "" }
    fun getUniqueId(context: Context): Flow<String> = context.userDataStore.data.map { it[UNIQUE_ID] ?: "" }
    fun getPhone(context: Context): Flow<String> = context.userDataStore.data.map { it[PHONE] ?: "" }
    fun getKdfSalt(context: Context): Flow<String> = context.userDataStore.data.map { it[KDF_SALT] ?: "" }
    fun getPublicKey(context: Context): Flow<String> = context.userDataStore.data.map { it[PUBLIC_KEY] ?: "" }
    fun getEncryptedPrivateKey(context: Context): Flow<String> = context.userDataStore.data.map { it[ENCRYPTED_PRIVATE_KEY] ?: "" }

    // 4. 清除信息（登出时用）
    suspend fun clear(context: Context) {
        context.userDataStore.edit { it.clear() }
    }

    // 保存唯一标识
    suspend fun saveUniqueId(context: Context, uniqueId: String) {
        context.userDataStore.edit { prefs ->
            prefs[UNIQUE_ID] = uniqueId
        }
    }
}
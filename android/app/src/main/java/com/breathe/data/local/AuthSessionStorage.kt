package com.breathe.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.breathe.BuildConfig
import com.breathe.data.model.RegisterResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSessionSnapshot(
  val serverUrl: String = BuildConfig.DEFAULT_SERVER_URL,
  val token: String? = null,
  val userId: Long? = null,
  val username: String? = null,
  val coupleId: Long? = null,
  val pairingCode: String? = null,
  val pairingExpiresAt: String? = null
) {
  val hasToken: Boolean get() = !token.isNullOrBlank()
  val isPaired: Boolean get() = coupleId != null
}

@Singleton
class AuthSessionStorage @Inject constructor(
  @ApplicationContext context: Context
) {
  private val prefs: SharedPreferences = createPrefs(context)
  private val state = MutableStateFlow(readSnapshot())

  fun observe(): StateFlow<AuthSessionSnapshot> = state.asStateFlow()

  fun token(): String? = state.value.token?.takeIf { it.isNotBlank() }

  fun serverUrl(): String = state.value.serverUrl.ifBlank { BuildConfig.DEFAULT_SERVER_URL }

  fun saveRegistration(response: RegisterResponse) {
    prefs.edit()
      .putString(KEY_TOKEN, response.token)
      .putLong(KEY_USER_ID, response.user.id)
      .putString(KEY_USERNAME, response.user.username)
      .remove(KEY_COUPLE_ID)
      .remove(KEY_PAIRING_CODE)
      .remove(KEY_PAIRING_EXPIRES_AT)
      .apply()
    state.value = readSnapshot()
  }

  fun savePairing(coupleId: Long, pairingCode: String, expiresAt: String) {
    prefs.edit()
      .putLong(KEY_COUPLE_ID, coupleId)
      .putString(KEY_PAIRING_CODE, pairingCode)
      .putString(KEY_PAIRING_EXPIRES_AT, expiresAt)
      .apply()
    state.value = readSnapshot()
  }

  fun saveJoinedCouple(coupleId: Long) {
    prefs.edit()
      .putLong(KEY_COUPLE_ID, coupleId)
      .remove(KEY_PAIRING_CODE)
      .remove(KEY_PAIRING_EXPIRES_AT)
      .apply()
    state.value = readSnapshot()
  }

  private fun readSnapshot(): AuthSessionSnapshot = AuthSessionSnapshot(
    serverUrl = prefs.getString(KEY_SERVER_URL, BuildConfig.DEFAULT_SERVER_URL).orEmpty(),
    token = prefs.getString(KEY_TOKEN, null),
    userId = prefs.getLong(KEY_USER_ID, -1L).takeIf { it > 0L },
    username = prefs.getString(KEY_USERNAME, null),
    coupleId = prefs.getLong(KEY_COUPLE_ID, -1L).takeIf { it > 0L },
    pairingCode = prefs.getString(KEY_PAIRING_CODE, null),
    pairingExpiresAt = prefs.getString(KEY_PAIRING_EXPIRES_AT, null)
  )

  private fun createPrefs(context: Context): SharedPreferences =
    runCatching {
      val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

      EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      )
    }.getOrElse {
      context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

  private companion object {
    const val PREF_NAME = "breathe_session"
    const val KEY_SERVER_URL = "server_url"
    const val KEY_TOKEN = "token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_COUPLE_ID = "couple_id"
    const val KEY_PAIRING_CODE = "pairing_code"
    const val KEY_PAIRING_EXPIRES_AT = "pairing_expires_at"
  }
}

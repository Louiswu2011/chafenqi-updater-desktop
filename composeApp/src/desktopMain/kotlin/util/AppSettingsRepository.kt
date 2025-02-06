package util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepository(
    val dataStore: DataStore<Preferences>,
) {
    companion object {
        const val DEFAULT_CACHED_USERNAME = ""
        const val DEFAULT_CACHED_TOKEN = ""
    }

    val cachedUsernameKey = stringPreferencesKey("cachedUsername")
    val cachedTokenKey = stringPreferencesKey("cachedToken")

    val cachedUsername: Flow<String> =
        dataStore.data.map { it[cachedUsernameKey] ?: DEFAULT_CACHED_USERNAME }

    val cachedToken: Flow<String> =
        dataStore.data.map { it[cachedTokenKey] ?: DEFAULT_CACHED_TOKEN }

    suspend fun setCachedUsername(username: String) {
        dataStore.edit { preferences -> preferences[cachedUsernameKey] = username }
    }

    suspend fun setCachedToken(token: String) {
        dataStore.edit { preferences -> preferences[cachedTokenKey] = token }
    }

    suspend fun saveSettings(
        cachedUsername: String,
        cachedToken: String,
    ) {
        dataStore.edit {
            it[cachedUsernameKey] = cachedUsername
            it[cachedTokenKey] = cachedToken
        }
    }
}

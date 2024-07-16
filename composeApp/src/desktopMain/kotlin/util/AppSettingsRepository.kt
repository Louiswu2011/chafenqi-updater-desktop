package util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val cachedUsername: String = "",
    val cachedToken: String = ""
)

class AppSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val DEFAULT_CACHED_USERNAME = ""
        const val DEFAULT_CACHED_TOKEN = ""
    }

    private val cachedUsernameKey = stringPreferencesKey("cachedUsername")
    private val cachedTokenKey = stringPreferencesKey("cachedToken")

    val settings: Flow<AppSettings> = dataStore.data.map {
        AppSettings(
            cachedUsername = it[cachedUsernameKey] ?: DEFAULT_CACHED_USERNAME,
            cachedToken = it[cachedTokenKey] ?: DEFAULT_CACHED_TOKEN
        )
    }

    suspend fun saveSettings(
        cachedUsername: String,
        cachedToken: String
    ) {
        dataStore.edit {
            it[cachedUsernameKey] = cachedUsername
            it[cachedTokenKey] = cachedToken
        }
    }
}
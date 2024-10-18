package user

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import util.AppSettingsRepository

class AppState : ViewModel() {
    var user = CFQUser
    var isLoggedIn by mutableStateOf(false)
    var loggedOut by mutableStateOf(false)

    suspend fun logout(repository: AppSettingsRepository) {
        CFQUser.token = ""
        CFQUser.username = ""
        isLoggedIn = false
        loggedOut = true
        removeUserProfile()
        resetCredentialsCache(repository)
    }

    private fun removeUserProfile() {
        user.isPremium = false
        user.fishToken = ""
        user.fishForward = false
        user.bindQQ = ""
    }

    private suspend fun resetCredentialsCache(repository: AppSettingsRepository) {
        repository.saveSettings("", "")
    }
}

val LocalAppState = compositionLocalOf<AppState> { error("App State not found.") }
val LocalDataStore = compositionLocalOf<DataStore<Preferences>> { error("Datastore not found") }
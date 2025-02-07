package model

import GITHUB_PREFIX
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import user.CFQUser
import util.CFQServer
import util.FishServer
import java.awt.Desktop
import java.net.URI

data class SettingsUiState(
    val loadingSettings: Boolean = false,
    val validatingFishCredentials: Boolean = false,
    val fishCredentialsMismatch: Boolean = false,
    val fishBindSuccess: Boolean = false,
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    val user = CFQUser

    var showBindFishDialog by mutableStateOf(false)
    var showBindQQDialog by mutableStateOf(false)

    fun loadFishForward() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingSettings = true,
                )
            }
            user.fishForward =
                try {
                    CFQServer.apiFetchUserOption(user.token, "forwarding_fish") == "1"
                } catch (e: Exception) {
                    println("User fish forward option failed to load, fallback to false")
                    false
                }
            println("Fetched user fish forward option: ${user.fishForward}")
            _uiState.update {
                it.copy(
                    loadingSettings = false,
                )
            }
        }
    }

    fun submitFishForwardChange(state: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingSettings = true,
                )
            }
            CFQServer.apiUploadUserOption(user.token, "forwarding_fish", if (state) "1" else "0")
            loadFishForward()
            _uiState.update {
                it.copy(
                    loadingSettings = false,
                )
            }
        }
    }

    fun validateFishCredentials(
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    validatingFishCredentials = true,
                )
            }
            val token = FishServer.getUserToken(username, password)
            if (token.isEmpty()) {
                _uiState.update {
                    it.copy(
                        fishCredentialsMismatch = true,
                    )
                }
            } else {
                submitFishToken(token)
                clearFishErrors()
                triggerFishBindSuccess()
            }
            _uiState.update {
                it.copy(
                    validatingFishCredentials = false,
                )
            }
        }
    }

    private suspend fun submitFishToken(token: String) {
        user.fishToken = token
        CFQServer.fishUploadToken(user.token, user.fishToken)
    }

    fun clearFishErrors() {
        if (_uiState.value.fishCredentialsMismatch) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        fishCredentialsMismatch = false,
                    )
                }
            }
        }
    }

    private fun triggerFishBindSuccess() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    fishBindSuccess = true,
                )
            }
        }
    }

    fun fishBindDialogDismissed() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    fishBindSuccess = false,
                    fishCredentialsMismatch = false,
                    validatingFishCredentials = false,
                )
            }
        }
    }

    fun submitQQ(qq: String) {
        viewModelScope.launch {
            user.bindQQ = qq
            CFQServer.apiUpdateBindQQ(user.token, qq)
        }
    }

    fun openGithubPage(platform: Int) {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop == null) return
        try {
            val platformGithub = if (platform == 0) "chafenqi" else "chafenqi-android"
            desktop.browse(URI.create(GITHUB_PREFIX + platformGithub))
        } catch (e: Exception) {
            println("Cannot open browser: $e")
        }
    }
}

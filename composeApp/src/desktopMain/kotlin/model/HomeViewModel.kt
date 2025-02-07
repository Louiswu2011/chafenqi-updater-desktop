package model

import PORTAL_ADDRESS
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import user.CFQUser
import util.SystemProxy

data class HomeViewState(
    val isProxyOn: Boolean = false,
)

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeViewState())
    val homeState = _homeState.asStateFlow()

    var showLogoutAlertDialog by mutableStateOf(false)
    var showCopyDialog by mutableStateOf(false)
    var showHelpSheet by mutableStateOf(false)
    var showSettingsSheet by mutableStateOf(false)

    fun toggleProxy(state: Boolean) {
        try {
            if (state) {
                SystemProxy.setProxy()
                viewModelScope.launch {
                    _homeState.update {
                        it.copy(
                            isProxyOn = true,
                        )
                    }
                }
            } else {
                SystemProxy.disableProxy()
                viewModelScope.launch {
                    _homeState.update {
                        it.copy(
                            isProxyOn = false,
                        )
                    }
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _homeState.update {
                    it.copy(
                        isProxyOn = false,
                    )
                }
            }
        }
    }

    fun copyToClipboard(
        game: Int,
        clipboardManager: ClipboardManager,
    ) {
        val url = buildUrl(game)
        clipboardManager.setText(
            buildAnnotatedString {
                append(url)
            },
        )
    }

    private fun buildUrl(game: Int): String {
        val token = CFQUser.token
        if (token.isEmpty()) {
            return ""
        }
        return "${PORTAL_ADDRESS}${if (game == 0) "upload/chunithm" else "upload/maimai"}?jwt=$token"
    }
}

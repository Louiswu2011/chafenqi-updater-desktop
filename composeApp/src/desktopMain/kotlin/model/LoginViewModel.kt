package model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import user.CFQUser
import util.*

data class LoginState(
    val isCredentialsMismatched: Boolean = false,
    val errorText: String = "",
    val isLoggingIn: Boolean = false,
    val loginSuccess: Boolean = false,
    val loginPrompt: String = ""
)

class LoginViewModel(
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {
    val user = CFQUser

    val settings: StateFlow<AppSettings?> = settingsRepository
        .settings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            null
        )

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        // Start login
        updatePrompt("登录中...")
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isLoggingIn = true
                )
            }

            try {
                CFQUser.token = CFQServer.authenticate(username, password)
                CFQUser.username = username
                createUserProfile()
                saveCredentialsToCache(username, CFQUser.token)
                _loginState.update {
                    it.copy(
                        loginSuccess = true
                    )
                }
            } catch (e: Exception) {
                when (e) {
                    is CredentialsMismatchException, is UserNotFoundException -> {
                        raiseError("用户名或密码错误！")
                    }

                    else -> {
                        raiseError("未知错误：${e}")
                    }
                }
            }

            _loginState.update {
                it.copy(
                    isLoggingIn = false
                )
            }
        }
    }

    private fun saveCredentialsToCache(username: String, token: String) = viewModelScope.launch {
        settingsRepository.saveSettings(username, token)
    }

    fun loginWithCachedToken(username: String, token: String) {
        // Start login
        updatePrompt("登录中...")
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isLoggingIn = true
                )
            }
            try {
                CFQUser.token = token
                CFQUser.username = username
                createUserProfile()
                _loginState.update {
                    it.copy(
                        loginSuccess = true
                    )
                }
            } catch (e: Exception) {
                raiseError("登录认证过期，请重新登录")
            }

            _loginState.update {
                it.copy(
                    isLoggingIn = false
                )
            }
        }
    }

    private fun raiseError(errorText: String) {
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isCredentialsMismatched = true,
                    errorText = errorText
                )
            }
        }
    }

    fun clearErrors() {
        if (_loginState.value.isCredentialsMismatched) {
            viewModelScope.launch {
                _loginState.update {
                    it.copy(
                        isCredentialsMismatched = false,
                        errorText = ""
                    )
                }
            }
        }
    }

    fun resetLoginState() {
        viewModelScope.launch {
            _loginState.update {
                LoginState()
            }
        }
    }

    private suspend fun createUserProfile() {
        updatePrompt("加载用户数据...")
        user.isPremium = CFQServer.apiIsPremium(user.username)

        user.fishToken = try {
            CFQServer.fishFetchToken(user.token)
        } catch (e: Exception) {
            println("User did not bind fish account.")
            ""
        }
        println("Fetched user fish token: ${user.fishToken}")

        user.fishForward = try {
            CFQServer.apiFetchUserOption(user.token, "forwarding_fish") == "1"
        } catch (e: Exception) {
            println("User fish forward option failed to load, fallback to false")
            false
        }
        println("Fetched user fish forward option: ${user.fishForward}")

        user.bindQQ = try {
            CFQServer.apiFetchUserOption(user.token, "bindQQ")
        } catch (e: Exception) {
            println("User did not bind qq.")
            ""
        }
        println("Fetched user bind qq: ${user.bindQQ}")
    }

    private fun updatePrompt(newPrompt: String) {
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    loginPrompt = newPrompt
                )
            }
        }
    }
}
package model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import user.CFQUser
import util.*

data class LoginState(
    val isCredentialsMismatched: Boolean = false,
    val errorText: String = "",
    val isLoggingIn: Boolean = false,
    val loginSuccess: Boolean = false,
    val loginPrompt: String = "",
)

class LoginViewModel(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {
    val user = CFQUser

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun login(
        username: String,
        password: String,
    ) {
        // Start login
        updatePrompt("登录中...")
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isLoggingIn = true,
                )
            }

            val (response, code) = CFQServer.authenticate(username, password)
            if (code != HttpStatusCode.OK) {
                raiseError("登录失败，请检查用户名或密码")
                _loginState.update {
                    it.copy(
                        isLoggingIn = false,
                    )
                }
            } else {
                CFQUser.token = response
                CFQUser.username = username
                createUserProfile()
                saveCredentialsToCache(username, CFQUser.token)
                _loginState.update {
                    it.copy(
                        loginSuccess = true,
                    )
                }
            }

            _loginState.update {
                it.copy(
                    isLoggingIn = false,
                )
            }
        }
    }

    suspend fun fetchCachedCredentials(): List<String> =
        listOf(
            settingsRepository.cachedUsername.first(),
            settingsRepository.cachedToken.first(),
        )

    private fun saveCredentialsToCache(
        username: String,
        token: String,
    ) = viewModelScope.launch {
        settingsRepository.setCachedUsername(username)
        settingsRepository.setCachedToken(token)
    }

    fun loginWithCachedToken(
        username: String,
        token: String,
    ) {
        // Start login
        updatePrompt("登录中...")
        if (username.isEmpty() || token.isEmpty()) {
            raiseError("请输入用户名或密码")
            return
        }
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isLoggingIn = true,
                )
            }
            try {
                CFQUser.token = token
                CFQUser.username = username
                createUserProfile()
                _loginState.update {
                    it.copy(
                        loginSuccess = true,
                    )
                }
            } catch (_: Exception) {
                raiseError("登录认证过期，请重新登录")
            }

            _loginState.update {
                it.copy(
                    isLoggingIn = false,
                )
            }
        }
    }

    private fun raiseError(errorText: String) {
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isCredentialsMismatched = true,
                    errorText = errorText,
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
                        errorText = "",
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
        user.isPremium = CFQServer.apiIsPremium(user.token)

        user.fishToken =
            try {
                CFQServer.fishFetchToken(user.token)
            } catch (_: Exception) {
                println("User did not bind fish account.")
                ""
            }
        println("Fetched user fish token: ${user.fishToken}")

        user.fishForward =
            try {
                CFQServer.apiFetchUserOption(user.token, "forwarding_fish") == "1"
            } catch (_: Exception) {
                println("User fish forward option failed to load, fallback to false")
                false
            }
        println("Fetched user fish forward option: ${user.fishForward}")

        user.bindQQ =
            try {
                CFQServer.apiFetchBindQQ(user.token)
            } catch (_: Exception) {
                println("User did not bind qq.")
                ""
            }
        println("Fetched user bind qq: ${user.bindQQ}")
    }

    private fun updatePrompt(newPrompt: String) {
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    loginPrompt = newPrompt,
                )
            }
        }
    }
}

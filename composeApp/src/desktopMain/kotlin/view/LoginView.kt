package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import chafenqi_updater_desktop.composeapp.generated.resources.Res
import chafenqi_updater_desktop.composeapp.generated.resources.app_icon
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.launch
import model.LoginViewModel
import org.jetbrains.compose.resources.painterResource
import user.LocalAppState
import user.LocalDataStore
import util.AppSettingsRepository
import util.sha256

@Composable
fun LoginView() {
    val scope = rememberCoroutineScope()
    val repository = AppSettingsRepository(dataStore = LocalDataStore.current)
    val appState = LocalAppState.current

    val model: LoginViewModel = viewModel {
        LoginViewModel(repository)
    }

    val loginState by model.loginState.collectAsState()
    val toaster = rememberToasterState()

    LaunchedEffect(loginState.loginSuccess) {
        if (loginState.loginSuccess && !appState.isLoggedIn) {
            appState.isLoggedIn = true
            model.resetLoginState()
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val cache = model.fetchCachedCredentials()
            val cachedUsername = cache[0]
            val cachedToken = cache[1]
            println(cache)

            if (cachedUsername.isNotEmpty() && cachedToken.isNotEmpty() && !appState.loggedOut) {
                appState.loggedOut = false
                model.loginWithCachedToken(cachedUsername, cachedToken)
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "App Icon",
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .size(128.dp)
        )
        AnimatedVisibility(!loginState.isLoggingIn) {
            LoginComponent(toaster)
        }
        AnimatedVisibility(loginState.isLoggingIn) {
            LoadingComponent()
        }
    }

    Toaster(state = toaster, maxVisibleToasts = 1, alignment = Alignment.BottomEnd)
}

@Composable
fun LoadingComponent() {
    val model = viewModel<LoginViewModel>()
    val loginState by model.loginState.collectAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = loginState.loginPrompt,
            modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}

@Composable
fun LoginComponent(toaster: ToasterState) {
    val model = viewModel<LoginViewModel>()
    val loginState by model.loginState.collectAsState()

    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(loginState.isCredentialsMismatched) {
        if (loginState.isCredentialsMismatched) {
            toaster.show(message = loginState.errorText, type = ToastType.Error)
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "登录到查分器",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 20.dp)
        )
        TextField(
            value = username,
            onValueChange = {
                username = it
                model.clearErrors()
            },
            label = {
                Text("用户名")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Username input field icon"
                )
            },
            isError = loginState.isCredentialsMismatched,
            singleLine = true,
            modifier = Modifier.padding(top = 40.dp)
        )
        TextField(
            value = password,
            onValueChange = {
                password = it
                model.clearErrors()
            },
            label = {
                Text("密码")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = "Password input field icon"
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = "Password visibility toggle button",
                    modifier = Modifier.clickable {
                        passwordVisible = !passwordVisible
                    }
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = loginState.isCredentialsMismatched,
            singleLine = true,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = {
                model.login(username, password.sha256())
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("登录")
        }
        TextButton(
            onClick = {

            }
        ) {
            Text("注册新账号")
        }
    }
}
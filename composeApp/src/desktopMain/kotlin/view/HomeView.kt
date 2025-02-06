package view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.launch
import model.HomeViewModel
import user.LocalAppState
import user.LocalDataStore
import util.AppSettingsRepository
import kotlin.math.hypot

@Composable
fun HomeView() {
    val model = viewModel<HomeViewModel>()
    val state by model.homeState.collectAsState()
    val repository = AppSettingsRepository(dataStore = LocalDataStore.current)
    val scope = rememberCoroutineScope()
    val toaster = rememberToasterState()

    val appState = LocalAppState.current

    var radius by remember {
        mutableFloatStateOf(0f)
    }

    val animatedRadius = remember { Animatable(0f) }
    val width = 400f
    val height = 700f
    val maxRadiusPx = hypot(width, height)
    val color = MaterialTheme.colors.secondary
    LaunchedEffect(state.isProxyOn) {
        if (state.isProxyOn) {
            animatedRadius.animateTo(maxRadiusPx, animationSpec = tween()) {
                radius = value / 2
            }
        } else {
            animatedRadius.animateTo(0f, animationSpec = tween()) {
                radius = value
            }
        }
    }

    Toaster(state = toaster)

    HelpSheet()
    SettingsSheet()

    if (model.showLogoutAlertDialog) {
        LogoutAlertDialog(onDismissRequest = {
            model.showLogoutAlertDialog = false
        }, onConfirmation = {
            scope.launch {
                appState.logout(repository)
            }
            model.showLogoutAlertDialog = false
        })
    }

    if (model.showCopyDialog) {
        CopyDialog(toaster) {
            model.showCopyDialog = false
        }
    }

    Box {
        Spacer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = Color.Transparent)
                    .drawBehind {
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = Offset(size.width / 2f, size.height / 2f),
                        )
                    },
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(all = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { model.showHelpSheet = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Help, contentDescription = "help button")
                }
                Text(
                    buildAnnotatedString {
                        append("欢迎回来，")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(appState.user.username)
                        }
                    },
                    maxLines = 1,
                )
                IconButton(onClick = { model.showSettingsSheet = true }) {
                    Icon(imageVector = Icons.Outlined.Settings, contentDescription = "settings button")
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Switch(checked = state.isProxyOn, onCheckedChange = {
                    model.toggleProxy(it)
                })
                Text("代理${if (state.isProxyOn) "已" else "未"}启动")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { model.showCopyDialog = true }) {
                    Text("复制链接")
                }
                Button(onClick = { model.showLogoutAlertDialog = true }) {
                    Text("切换用户")
                }
            }
        }
    }
}

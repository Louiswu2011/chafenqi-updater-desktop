package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import chafenqi_updater_desktop.composeapp.generated.resources.Res
import chafenqi_updater_desktop.composeapp.generated.resources.ios_24px
import model.HomeViewModel
import model.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import user.LocalAppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet() {
    val model = viewModel<HomeViewModel>()
    val settingsModel = viewModel<SettingsViewModel>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        settingsModel.loadFishForward()
    }

    if (settingsModel.showBindFishDialog) {
        BindFishDialog {
            settingsModel.fishBindDialogDismissed()
            settingsModel.showBindFishDialog = false
        }
    }

    if (settingsModel.showBindQQDialog) {
        BindQQDialog {
            settingsModel.showBindQQDialog = false
        }
    }

    if (model.showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                model.showSettingsSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(fraction = 0.9f)
                    .padding(all = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("App设置", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = 20.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    BindFishSetting()
                    FishForwardingSetting()
                    Divider(modifier = Modifier.padding(bottom = 10.dp))
                    BindQQSetting()
                    Divider(modifier = Modifier.padding(bottom = 10.dp))
                    AboutApp()
                }
            }
        }
    }
}

@Composable
fun FishForwardingSetting() {
    val state = LocalAppState.current
    val settingsModel = viewModel<SettingsViewModel>()
    val uiState by settingsModel.uiState.collectAsState()

    Row(
        modifier = Modifier.height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("同步到水鱼", fontWeight = FontWeight.Bold)
            if (state.user.fishToken.isEmpty()) {
                Text("请先绑定水鱼网账号", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text("开启该选项来将数据同步到水鱼查分器", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Switch(
            checked = state.user.fishForward,
            onCheckedChange = {
                settingsModel.submitFishForwardChange(it)
            },
            enabled = !uiState.loadingSettings && state.user.fishToken.isNotEmpty()
        )
    }
}

@Composable
fun BindFishSetting() {
    val model = viewModel<SettingsViewModel>()
    val state = LocalAppState.current

    Row(
        modifier = Modifier.height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("绑定水鱼网账号", fontWeight = FontWeight.Bold)
            if (state.user.fishToken.isEmpty()) {
                Text("点击绑定水鱼网账号", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text("已绑定，点击重新绑定水鱼网账号", fontSize = 12.sp, color = Color.Gray)
            }
        }

        IconButton(
            onClick = { model.showBindFishDialog = true }
        ) {
            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "绑定水鱼网账号")
        }
    }
}

@Composable
fun BindQQSetting() {
    val model = viewModel<SettingsViewModel>()
    val state = LocalAppState.current

    Row(
        modifier = Modifier.height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("绑定QQ号", fontWeight = FontWeight.Bold)
            if (state.user.bindQQ.isEmpty()) {
                Text("点击绑定QQ号", fontSize = 12.sp, color = Color.Gray)
            } else {
                Text("当前绑定QQ号：${model.user.bindQQ}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        IconButton(
            onClick = { model.showBindQQDialog = true }
        ) {
            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "绑定QQ号")
        }
    }
}

@Composable
fun AboutApp() {
    val model = viewModel<SettingsViewModel>()

    Row(
        modifier = Modifier.height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("前往查分器App介绍页", fontWeight = FontWeight.Bold)
            Text("点击对应图标前往", fontSize = 12.sp, color = Color.Gray)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = { model.openGithubPage(1) }
            ) {
                Icon(Icons.Outlined.Android, contentDescription = "安卓介绍页")
            }
            IconButton(
                onClick = { model.openGithubPage(0) }
            ) {
                Icon(painterResource(Res.drawable.ios_24px), contentDescription = "iOS介绍页")
            }
        }
    }
}
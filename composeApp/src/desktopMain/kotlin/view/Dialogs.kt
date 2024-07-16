package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import model.HomeViewModel
import model.SettingsViewModel


@Composable
fun CopyDialog(toasterState: ToasterState, onDismissRequest: () -> Unit) {
    val model = viewModel<HomeViewModel>()
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("选择需要上传的游戏")
            }
        },
        buttons = {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    model.copyToClipboard(0, clipboardManager)
                    toasterState.show(message = "已复制链接至剪贴板", type = ToastType.Success)
                    onDismissRequest()
                }, modifier = Modifier.width(100.dp)) {
                    Text("中二节奏")
                }
                Button(onClick = {
                    model.copyToClipboard(1, clipboardManager)
                    toasterState.show(message = "已复制链接至剪贴板", type = ToastType.Success)
                    onDismissRequest()
                }, modifier = Modifier.width(100.dp)) {
                    Text("舞萌DX")
                }
                Button(onClick = {
                    onDismissRequest()
                }, modifier = Modifier.width(100.dp)) {
                    Text("取消")
                }
            }
        },
        modifier = Modifier.width(200.dp)
    )
}

@Composable
fun LogoutAlertDialog(onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("确定要切换用户吗？")
        },
        text = {
            Text("将返回到登录页面。")
        },
        confirmButton = {
            Button(onClick = { onConfirmation() }) {
                Text("确定")
            }
        },
        dismissButton = {
            Button(onClick = { onDismissRequest() }) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BindQQDialog(onDismissRequest: () -> Unit) {
    val model = viewModel<SettingsViewModel>()
    var qq by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("绑定QQ号")
                TextField(
                    value = qq,
                    onValueChange = { qq = it },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = "QQ") },
                    singleLine = true,
                    label = { Text("QQ号") },
                    placeholder = { Text("请在此输入您的QQ号") },
                    keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                model.submitQQ(qq)
                qq = ""
                onDismissRequest()
            }) {
                Text("绑定")
            }
        },
        dismissButton = {
            Button(onClick = {
                qq = ""
                onDismissRequest()
            }) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BindFishDialog(onDismissRequest: () -> Unit) {
    val model = viewModel<SettingsViewModel>()
    val state by model.uiState.collectAsState()

    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("登录到水鱼网")
                TextField(
                    value = username,
                    onValueChange = {
                        username = it
                        model.clearFishErrors()
                    },
                    label = {
                        Text("水鱼网用户名")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Username input field icon"
                        )
                    },
                    isError = state.fishCredentialsMismatch,
                    singleLine = true
                )
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        model.clearFishErrors()
                    },
                    label = {
                        Text("水鱼网密码")
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
                    isError = state.fishCredentialsMismatch,
                    singleLine = true
                )
                AnimatedVisibility(state.fishCredentialsMismatch) {
                    Text("用户名或密码错误", color = Color.Red)
                }
                AnimatedVisibility(state.fishBindSuccess) {
                    Text("绑定水鱼网账号成功", color = Color.Green)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    model.validateFishCredentials(username, password)
                    username = ""
                    password = ""
                }, enabled = !state.validatingFishCredentials
            ) {
                Text("绑定")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    username = ""
                    password = ""
                    model.clearFishErrors()
                    onDismissRequest()
                }, enabled = !state.validatingFishCredentials
            ) {
                Text("取消")
            }
        }
    )
}
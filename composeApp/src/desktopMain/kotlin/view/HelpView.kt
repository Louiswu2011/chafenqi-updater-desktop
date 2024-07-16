package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import model.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSheet() {
    val model = viewModel<HomeViewModel>()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (model.showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                model.showHelpSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(fraction = 0.9f)
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("使用帮助", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                StepInfo("打开代理开关", buildAnnotatedString { append("点击中间的开关打开代理。") })
                StepInfo("复制传分链接", buildAnnotatedString { append("点击下方的复制链接按钮，选择需要上传的游戏。") })
                StepInfo("在微信聊天中访问链接", buildAnnotatedString {
                    append("将传分链接粘贴到任意聊天框，并点击访问。")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("若无法访问，请在关闭代理的情况下点击链接，并在微信的安全提示出现后打开链接并继续访问。")
                    }
                })
                StepInfo(
                    "等待上传完成",
                    buildAnnotatedString { append("若您在iOS端或安卓端登录了相同账号并开启了通知权限，将收到开始上传和上传完成的通知。") })
            }
        }
    }
}

@Composable
fun StepInfo(title: String, text: AnnotatedString) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(text)
    }
}
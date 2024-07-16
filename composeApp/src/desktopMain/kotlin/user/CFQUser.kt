package user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object CFQUser {
    var token by mutableStateOf("")
    var username by mutableStateOf("")

    var isPremium by mutableStateOf(false)

    var fishForward by mutableStateOf(false)
    var fishToken by mutableStateOf("")

    var bindQQ by mutableStateOf("")
}
package view

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import user.LocalAppState

@Composable
fun RootView() {
    val appState = LocalAppState.current

    AnimatedContent(targetState = appState.isLoggedIn) {
        if (it) {
            HomeView()
        } else {
            LoginView()
        }
    }
}

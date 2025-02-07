import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import chafenqi_updater_desktop.composeapp.generated.resources.Res
import chafenqi_updater_desktop.composeapp.generated.resources.app_icon
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.painterResource
import user.AppState
import user.LocalAppState
import user.LocalDataStore
import util.CFQServer
import view.RootView

const val PORTAL_ADDRESS = "http://43.139.107.206:9030/"
const val GITHUB_PREFIX = "https://github.com/Louiswu2011/"

fun main() =
    application {
        val appState = AppState()
        val windowState =
            rememberWindowState(
                size = DpSize(width = 400.dp, height = 700.dp),
            )
        val dataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    "./cache.preferences_pb".toPath()
                },
            )

        Window(
            onCloseRequest = {
                exitApplication()
                CFQServer.onExit()
            },
            title = "查分器App传分工具",
            state = windowState,
            icon = painterResource(Res.drawable.app_icon),
            resizable = false,
        ) {
            CompositionLocalProvider(LocalAppState provides appState) {
                CompositionLocalProvider(LocalDataStore provides dataStore) {
                    MaterialTheme {
                        RootView()
                    }
                }
            }
        }
    }

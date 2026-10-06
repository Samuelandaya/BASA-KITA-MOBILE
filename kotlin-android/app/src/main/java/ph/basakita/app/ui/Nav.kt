package ph.basakita.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import ph.basakita.app.state.AppState

val LocalApp = staticCompositionLocalOf<AppState> { error("AppState not provided") }
val LocalNav = staticCompositionLocalOf<Nav> { error("Nav not provided") }

/** The five tabs in the bottom bar. */
val tabRoutes = listOf("home", "books", "learn", "cards", "me")

/**
 * Moving between screens, named like the Flutter app's router:
 * push opens a page on top, go jumps to a tab (or a page from Home), replace swaps the current page.
 */
class Nav(private val controller: NavHostController?) {
    fun push(route: String) {
        controller?.navigate(route.trimStart('/'))
    }

    fun go(route: String) {
        val r = route.trimStart('/')
        val c = controller ?: return
        c.navigate(r) {
            popUpTo("home")
            launchSingleTop = true
        }
    }

    fun replace(route: String) {
        val c = controller ?: return
        val current = c.currentBackStackEntry?.destination?.id
        c.navigate(route.trimStart('/')) {
            if (current != null) popUpTo(current) { inclusive = true }
        }
    }

    fun back() {
        val c = controller ?: return
        if (!c.popBackStack()) go("home")
    }

    val canBack: Boolean get() = (controller?.previousBackStackEntry != null)
}

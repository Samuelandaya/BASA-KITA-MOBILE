package ph.basakita.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ph.basakita.app.screens.AdminScreen
import ph.basakita.app.screens.ArScreen
import ph.basakita.app.screens.CompleteScreen
import ph.basakita.app.screens.CreateScreen
import ph.basakita.app.screens.FlashcardsScreen
import ph.basakita.app.screens.ForgotScreen
import ph.basakita.app.screens.HelpScreen
import ph.basakita.app.screens.HomeScreen
import ph.basakita.app.screens.LearnScreen
import ph.basakita.app.screens.ListenScreen
import ph.basakita.app.screens.LoginScreen
import ph.basakita.app.screens.MadeStoryScreen
import ph.basakita.app.screens.MeScreen
import ph.basakita.app.screens.MyStoriesScreen
import ph.basakita.app.screens.NormalReadingScreen
import ph.basakita.app.screens.ProfileScreen
import ph.basakita.app.screens.ProgressScreen
import ph.basakita.app.screens.ReadAndLearnScreen
import ph.basakita.app.screens.ReadWithMeScreen
import ph.basakita.app.screens.ResetScreen
import ph.basakita.app.screens.ScanScreen
import ph.basakita.app.screens.SettingsGate
import ph.basakita.app.screens.SignUpScreen
import ph.basakita.app.screens.StoryDetailsScreen
import ph.basakita.app.screens.StorybooksScreen
import ph.basakita.app.screens.VerifyScreen
import ph.basakita.app.screens.WelcomeScreen
import ph.basakita.app.services.Sounds
import ph.basakita.app.services.Speech
import ph.basakita.app.state.AppState
import ph.basakita.app.ui.BasaKitaTheme
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Nav
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.tabRoutes

/**
 * Which screens are open depends on who is signed in: the sign-in screens when no one is,
 * the email code screen for a new account, the teacher view for an admin, and the
 * five learner tabs for everyone else. The role comes from the backend (demo for now).
 */
@Composable
fun BasaKitaApp(app: AppState, resetToken: MutableState<String?>) {
    // Keeps the voice, volume, sound effects and music in step with Settings.
    val s = app.settings
    LaunchedEffect(s.voice, s.volume, s.effects, s.music) {
        Speech.voiceId = s.voice
        Speech.volume = s.volume / 100f
        Sounds.configure(volume = s.volume, effects = s.effects, music = s.music)
    }
    BasaKitaTheme {
        CompositionLocalProvider(LocalApp provides app) {
            val user = app.user
            val token = resetToken.value
            when {
                token != null && user == null -> SignedOut(start = "reset", token = token, onReset = { resetToken.value = null })
                user == null -> SignedOut()
                user.role == "pending" -> CompositionLocalProvider(LocalNav provides Nav(null)) { VerifyScreen() }
                user.role == "admin" -> CompositionLocalProvider(LocalNav provides Nav(null)) { AdminScreen() }
                else -> Learner()
            }
        }
    }
}

@Composable
private fun SignedOut(start: String = "welcome", token: String? = null, onReset: () -> Unit = {}) {
    val controller = rememberNavController()
    val nav = remember(controller) { Nav(controller) }
    CompositionLocalProvider(LocalNav provides nav) {
        NavHost(controller, startDestination = start) {
            composable("welcome") { WelcomeScreen() }
            composable("login") { LoginScreen() }
            composable("signup") { SignUpScreen() }
            composable("forgot") { ForgotScreen() }
            composable("reset") { ResetScreen(token) { onReset(); controller.navigate("login") { popUpTo(0) } } }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selected: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    Tab("books", "Books", Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    Tab("learn", "Learn", Icons.Outlined.School, Icons.Filled.School),
    Tab("cards", "Cards", Icons.Outlined.Style, Icons.Filled.Style),
    Tab("me", "Me", Icons.Outlined.Person, Icons.Filled.Person),
)

@Composable
private fun Learner() {
    val controller = rememberNavController()
    val nav = remember(controller) { Nav(controller) }
    val entry by controller.currentBackStackEntryAsState()
    val route = entry?.destination?.route?.substringBefore('?') ?: "home"
    val onTab = route in tabRoutes
    CompositionLocalProvider(LocalNav provides nav) {
        Scaffold(
            containerColor = C.cream,
            bottomBar = {
                if (onTab) {
                    NavigationBar(containerColor = Color.White, modifier = Modifier.shadow(14.dp, ambientColor = Color(0x1A142938), spotColor = Color(0x1A142938))) {
                        for (t in tabs) {
                            NavigationBarItem(
                                selected = route == t.route,
                                onClick = { nav.go(t.route) },
                                icon = { Icon(if (route == t.route) t.selected else t.icon, null) },
                                label = { Text(t.label, style = T.body(12, weight = W800)) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = C.orange, selectedIconColor = C.ink, unselectedIconColor = C.muted, selectedTextColor = C.ink, unselectedTextColor = C.ink),
                            )
                        }
                    }
                }
            },
        ) { inner ->
            val bottom = PaddingValues(bottom = inner.calculateBottomPadding())
            NavHost(controller, startDestination = "home", modifier = Modifier.padding(bottom).consumeWindowInsets(bottom)) {
                composable("home") { HomeScreen() }
                composable("books") { StorybooksScreen() }
                composable("learn") { LearnScreen() }
                composable("cards?card={card}", arguments = listOf(navArgument("card") { type = NavType.StringType; nullable = true })) {
                    FlashcardsScreen(startCard = it.arguments?.getString("card"))
                }
                composable("me") { MeScreen() }
                composable("books/{id}") { StoryDetailsScreen(it.arguments?.getString("id").orEmpty()) }
                composable("books/{id}/listen") { ListenScreen(it.arguments?.getString("id").orEmpty()) }
                composable("books/{id}/read") { ReadAndLearnScreen(it.arguments?.getString("id").orEmpty()) }
                composable("books/{id}/with-me") { ReadWithMeScreen(it.arguments?.getString("id").orEmpty()) }
                composable("books/{id}/normal") { NormalReadingScreen(it.arguments?.getString("id").orEmpty()) }
                composable("books/{id}/complete") { CompleteScreen(it.arguments?.getString("id").orEmpty()) }
                composable("scan") { ScanScreen(cardMode = false) }
                composable("scan-card") { ScanScreen(cardMode = true) }
                composable("ar/{card}") { ArScreen(it.arguments?.getString("card").orEmpty()) }
                composable("create?story={story}", arguments = listOf(navArgument("story") { type = NavType.StringType; nullable = true })) {
                    CreateScreen(editId = it.arguments?.getString("story"))
                }
                composable("made/{id}") { MadeStoryScreen(it.arguments?.getString("id").orEmpty()) }
                composable("progress") { ProgressScreen() }
                composable("my-stories") { MyStoriesScreen() }
                composable("settings") { SettingsGate() }
                composable("profile") { ProfileScreen() }
                composable("help") { HelpScreen() }
            }
        }
    }
}

package ph.basakita.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import ph.basakita.app.api.AuthApi
import ph.basakita.app.api.emailPattern
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.Field
import ph.basakita.app.ui.FormError
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.MessageDialog
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.asset
import ph.basakita.app.ui.span

/** Yellow sign-in backdrop with the logo on top and a white card. */
@Composable
private fun AuthShell(wordmark: Boolean = false, back: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val nav = LocalNav.current
    Column(Modifier.fillMaxSize().background(C.yellow)) {
        Box(Modifier.fillMaxWidth().background(C.cream).statusBarsPadding().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            AsyncImage(asset(if (wordmark) "images/wordmark.png" else "images/logo.png"), "BASA-KITA", Modifier.height(64.dp))
            if (back && nav.canBack) {
                IconButton(onClick = { nav.back() }, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = C.navy)
                }
            }
        }
        Column(
            Modifier.weight(1f).imePadding().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 28.dp, top = 24.dp, bottom = 24.dp),
        ) {
            // White rounded card with the offset shadow from the design.
            Box {
                Box(Modifier.matchParentSize().offset(8.dp, 8.dp).background(Color(0xFFDCC56E), RoundedCornerShape(34.dp)))
                Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(34.dp)).padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp), content = content)
            }
        }
        Text(
            "© 2026 BASA-KITA · Read • Speak • Learn",
            textAlign = TextAlign.Center,
            style = T.body(12, color = Color.White),
            modifier = Modifier.fillMaxWidth().background(C.blue).navigationBarsPadding().padding(vertical = 10.dp),
        )
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit, hint: String? = null, password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    var hide by remember { mutableStateOf(true) }
    Column(Modifier.padding(bottom = 14.dp)) {
        Text(label, style = T.body(14, weight = W800))
        Spacer(Modifier.height(6.dp))
        Field(
            value, onChange,
            hint = hint,
            password = password && hide,
            keyboard = if (password) KeyboardType.Password else keyboard,
            trailing = if (password) {
                {
                    IconButton(onClick = { hide = !hide }) {
                        Icon(if (hide) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, if (hide) "Show password" else "Hide password", tint = C.muted)
                    }
                }
            } else null,
        )
    }
}

@Composable
private fun CardTitle(text: String, size: Int = 34) =
    Text(text, textAlign = TextAlign.Center, style = T.head(size), modifier = Modifier.fillMaxWidth())

@Composable
private fun CardNote(text: String) =
    Text(text, textAlign = TextAlign.Center, style = T.body(14, color = C.muted), modifier = Modifier.fillMaxWidth())

@Composable
private fun LinkButton(text: String, color: Color = C.blueBtn, modifier: Modifier = Modifier, onClick: () -> Unit) =
    Text(text, style = T.body(14, color = color, weight = W800), modifier = modifier.clickable(onClick = onClick).padding(8.dp))

// ---------------------------------------------------------------- Welcome
@Composable
fun WelcomeScreen() {
    val nav = LocalNav.current
    Column(Modifier.fillMaxSize().background(C.yellow).windowInsetsPadding(WindowInsets.safeDrawing), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        AsyncImage(asset("images/wordmark.png"), "BASA-KITA", Modifier.height(74.dp))
        Spacer(Modifier.height(6.dp))
        Text("Read • Speak • Learn", style = T.fun_(24, color = C.navy))
        AsyncImage(asset("images/login-illustration.png"), null, Modifier.weight(1f).fillMaxWidth().padding(18.dp), contentScale = ContentScale.Fit)
        Column(Modifier.padding(start = 28.dp, end = 28.dp, bottom = 8.dp)) {
            Btn("Login", big = true, expand = true, onClick = { nav.push("login") })
            Spacer(Modifier.height(10.dp))
            Btn("Sign Up", big = true, expand = true, kind = BtnKind.White, onClick = { nav.push("signup") })
        }
        Text(
            "Filipino-English reading for kindergarten learners",
            textAlign = TextAlign.Center,
            style = T.body(12, color = Color.White),
            modifier = Modifier.padding(top = 14.dp).fillMaxWidth().background(C.blue).padding(vertical = 10.dp),
        )
    }
}

// ---------------------------------------------------------------- Login
@Composable
fun LoginScreen() {
    val app = LocalApp.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun submit() {
        error = ""
        busy = true
        scope.launch {
            try {
                app.login(AuthApi.signIn(email, password)) // the app opens the learner or admin home from the role
            } catch (e: Exception) {
                error = e.message.orEmpty()
            } finally {
                busy = false
            }
        }
    }

    AuthShell(wordmark = true) {
        CardTitle("LOGIN")
        Spacer(Modifier.height(4.dp))
        CardNote("Sign in to access your learning activities.")
        Spacer(Modifier.height(20.dp))
        LabeledField("Email Address", email, { email = it }, hint = "name@email.com", keyboard = KeyboardType.Email)
        LabeledField("Password", password, { password = it }, password = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LinkButton("Forgot Password?") { nav.push("forgot") } }
        FormError(error)
        Btn(if (busy) "Logging in…" else "Login", kind = BtnKind.Blue, big = true, expand = true, onClick = if (busy) null else ::submit)
        Spacer(Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                withStyle(span(T.body(14, color = C.muted))) { append("Don’t have an account? ") }
                withStyle(span(T.body(14, color = C.orangeDark, weight = W800))) { append("Sign Up") }
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { nav.replace("signup") }.padding(8.dp),
        )
    }
}

// ---------------------------------------------------------------- Sign up
@Composable
fun SignUpScreen() {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun submit() {
        error = when {
            name.isBlank() || username.isBlank() || email.isBlank() -> "Please fill in every field."
            !emailPattern.matches(email.trim()) -> "Please enter a valid email address."
            password.length < 8 || !password.any(Char::isUpperCase) || !password.any(Char::isDigit) ->
                "Password needs at least 8 characters, 1 uppercase letter and 1 number."
            password != confirm -> "The two passwords don’t match."
            else -> ""
        }
        if (error.isNotEmpty()) return
        busy = true
        scope.launch {
            try {
                app.login(AuthApi.signUp(name, username, email, password)) // stays "pending" until the email code is verified
            } catch (e: Exception) {
                error = e.message.orEmpty()
            } finally {
                busy = false
            }
        }
    }

    AuthShell {
        CardTitle("Sign Up")
        Spacer(Modifier.height(16.dp))
        LabeledField("Name", name, { name = it }, hint = "First Name, Last Name...")
        LabeledField("Username", username, { username = it }, hint = "Username...")
        LabeledField("Email Address", email, { email = it }, hint = "name@email.com", keyboard = KeyboardType.Email)
        LabeledField("Password", password, { password = it }, password = true)
        LabeledField("Confirm Password", confirm, { confirm = it }, password = true)
        FormError(error)
        Btn(if (busy) "Signing up…" else "Sign Up", big = true, expand = true, onClick = if (busy) null else ::submit)
    }
}

// ---------------------------------------------------------------- Verify
@Composable
fun VerifyScreen() {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var resent by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    fun submit() {
        if (code.length != 6) {
            error = "Please enter all 6 numbers."
            return
        }
        error = ""
        busy = true
        scope.launch {
            try {
                val u = AuthApi.verifyEmail(app.user!!, code)
                app.updateUser { it.role = u.role }
            } catch (e: Exception) {
                error = e.message.orEmpty()
            } finally {
                busy = false
            }
        }
    }

    AuthShell(back = false) {
        CardTitle("Check your email", 30)
        Spacer(Modifier.height(6.dp))
        CardNote("We sent a 6-digit code to ${app.user?.email ?: "your email"}.")
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().clickable { focus.requestFocus() }, contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in 0 until 6) {
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = 44.dp, height = 54.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(2.dp, if (i == code.length) C.blueBtn else Color(0xFFE5E7EB), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text(code.getOrNull(i)?.toString() ?: "", style = T.head(26, weight = W700)) }
                }
            }
            // The real input sits invisibly on top so paste and one-time-code autofill work.
            BasicTextField(
                value = code,
                onValueChange = { v -> code = v.filter(Char::isDigit).take(6) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.matchParentSize().alpha(0f).focusRequester(focus),
            )
        }
        Spacer(Modifier.height(16.dp))
        FormError(error)
        Btn(if (busy) "Checking…" else "Verify", big = true, expand = true, onClick = if (busy) null else ::submit)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LinkButton(if (resent) "A new code is on its way." else "Didn’t get it? Resend code") {
                scope.launch {
                    AuthApi.resendCode(app.user?.email.orEmpty())
                    resent = true
                }
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { LinkButton("Back to Login", color = C.muted) { app.logout() } }
    }
}

// ---------------------------------------------------------------- Forgot / reset
@Composable
fun ForgotScreen() {
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }

    fun submit() {
        if (!emailPattern.matches(email.trim())) {
            error = "Please enter a valid email address."
            return
        }
        error = ""
        busy = true
        scope.launch {
            try {
                AuthApi.requestPasswordReset(email.trim())
                sent = true
            } catch (e: Exception) {
                error = e.message.orEmpty()
            } finally {
                busy = false
            }
        }
    }

    AuthShell {
        CardTitle("Forgot Password", 30)
        Spacer(Modifier.height(6.dp))
        CardNote("Enter your email and we’ll send a secure reset link.")
        Spacer(Modifier.height(18.dp))
        LabeledField("Email Address", email, { email = it }, hint = "name@email.com", keyboard = KeyboardType.Email)
        FormError(error)
        Btn(if (busy) "Sending…" else "Send reset link", kind = BtnKind.Blue, big = true, expand = true, onClick = if (busy) null else ::submit)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { LinkButton("Back to Login") { nav.back() } }
    }
    if (sent) {
        MessageDialog("Reset link sent!", "Check your email for a secure link to choose a new password.", "Back to Login") {
            sent = false
            nav.back()
        }
    }
}

private val resetRules: List<Pair<String, (String) -> Boolean>> = listOf(
    "At least 8 characters, with letters and numbers." to { p -> p.length >= 8 && p.any(Char::isLetter) && p.any(Char::isDigit) },
    "At least 1 uppercase letter (A–Z)" to { p -> p.any(Char::isUpperCase) },
    "At least 1 number (0–9)" to { p -> p.any(Char::isDigit) },
)

/** Opened from the link in the reset email (basakita://reset?token=...). */
@Composable
fun ResetScreen(token: String?, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pw by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var updated by remember { mutableStateOf(false) }

    fun submit() {
        error = when {
            !resetRules.all { it.second(pw) } -> "Your new password doesn’t meet all the rules yet."
            pw != confirm -> "The two passwords don’t match."
            else -> ""
        }
        if (error.isNotEmpty()) return
        scope.launch {
            try {
                AuthApi.resetPassword(token, pw)
                updated = true
            } catch (e: Exception) {
                error = e.message.orEmpty()
            }
        }
    }

    AuthShell(back = false) {
        CardTitle("Reset Your Password", 28)
        Spacer(Modifier.height(16.dp))
        LabeledField("New password", pw, { pw = it }, password = true)
        LabeledField("Confirm new password", confirm, { confirm = it }, password = true)
        for ((label, ok) in resetRules) {
            val met = ok(pw)
            Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null, tint = if (met) C.green else C.muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = T.body(13, color = C.muted))
            }
        }
        Spacer(Modifier.height(8.dp))
        FormError(error)
        Btn("Update Password", kind = BtnKind.Blue, big = true, expand = true, onClick = ::submit)
    }
    if (updated) MessageDialog("Password Updated!", "You can now log in with your new password.", "Back to Login") { updated = false; onDone() }
}

package ph.basakita.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.basakita.app.Lock
import ph.basakita.app.services.Reminders
import ph.basakita.app.services.Sounds
import ph.basakita.app.services.Speech
import ph.basakita.app.services.isNaturalVoice
import ph.basakita.app.services.voiceProfile
import ph.basakita.app.services.voiceProfiles
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.Field
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.Seg
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap

/** Settings are behind the parent PIN when the parent lock is on. */
@Composable
fun SettingsGate() {
    val app = LocalApp.current
    val s = app.settings
    var unlocked by remember { mutableStateOf(Lock.isUnlocked(app.user?.email)) }
    if (!(s.parentLock && s.pin.isNotEmpty()) || unlocked) return SettingsScreen()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AppPage(back = true, title = "Settings", subtitle = "Grown-ups only. Enter the parent PIN to continue.") {
        WhiteCard {
            Text("Parent PIN", style = T.body(14, weight = W800))
            Spacer(Modifier.height(6.dp))
            Field(pin, { pin = it.filter(Char::isDigit).take(4) }, password = true, keyboard = KeyboardType.NumberPassword)
            if (error.isNotEmpty()) Text(error, style = T.body(14, color = C.red, weight = W800), modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(12.dp))
            Btn("Unlock", onClick = {
                if (pin == s.pin) {
                    Lock.unlock(app.user?.email)
                    error = ""
                    unlocked = true
                } else {
                    error = "That PIN isn’t right. Try again."
                    pin = ""
                }
            })
        }
    }
}

@Composable
private fun SettingRow(label: String, hint: String? = null, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = T.body(15, weight = W800, color = C.navy))
            if (hint != null) Text(hint, style = T.body(12, color = C.muted))
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun SettingSwitch(label: String, value: Boolean, hint: String? = null, onChange: (Boolean) -> Unit) {
    SettingRow(label, hint) {
        Switch(value, onChange, colors = SwitchDefaults.colors(checkedTrackColor = C.green, checkedThumbColor = Color.White, uncheckedThumbColor = Color.White, uncheckedTrackColor = C.track, uncheckedBorderColor = C.track))
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    WhiteCard(Modifier.padding(bottom = 14.dp), padding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)) {
        Text(title, style = T.head(20))
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = C.border)
        content()
    }
}

@Composable
fun SettingsScreen() {
    val app = LocalApp.current
    val nav = LocalNav.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val s = app.settings
    var reminderNote by remember { mutableStateOf("") }
    var pinDialog by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    val current = voiceProfile(s.voice)
    val real = Speech.pickVoice("en-US", current.id)

    fun toggleReminder(on: Boolean) = scope.launch {
        if (!on) {
            Reminders.set(context, false)
            app.updateSettings { it.reminder = false }
            reminderNote = ""
            return@launch
        }
        val ok = Reminders.set(context, true)
        app.updateSettings { it.reminder = ok }
        reminderNote = if (ok) "Reminder on! You’ll get a notification every day at 4:00 PM, even when the app is closed."
        else "Notifications are blocked. Allow them in your phone settings to get reminders."
    }

    AppPage(back = true, title = "Settings", subtitle = "Make reading just right for you.") {
        Section("Reading") {
            SettingRow("Story language") { ScrollSeg(listOf("Filipino", "English", "Bilingual"), s.language, { it }) { v -> app.updateSettings { it.language = v } } }
            SettingRow("Read-aloud speed") { Seg(listOf(0.75, 1.0, 1.25), s.speed, { "$it×" }) { v -> app.updateSettings { it.speed = v } } }
            SettingRow("Text size") { Seg(listOf("Normal", "Big", "Bigger"), s.textSize, { it }) { v -> app.updateSettings { it.textSize = v } } }
            SettingSwitch("Highlight words while reading", s.highlight) { v -> app.updateSettings { it.highlight = v } }
        }
        Section("Sounds") {
            SettingSwitch("Story narration", s.narration) { v -> app.updateSettings { it.narration = v } }
            Column(Modifier.padding(vertical = 10.dp)) {
                Text("Voice", style = T.body(15, weight = W800, color = C.navy))
                Text(current.blurb + (real?.let { " · ${it.name}" } ?: ""), style = T.body(12, color = C.muted), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, C.track, RoundedCornerShape(16.dp))
                                .clickable { voiceMenu = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(current.name, style = T.body(15, weight = W700), modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ArrowDropDown, null, tint = C.muted)
                        }
                        DropdownMenu(voiceMenu, { voiceMenu = false }, containerColor = Color.White) {
                            for (p in voiceProfiles) {
                                DropdownMenuItem(text = { Text(p.name, style = T.body(15, weight = W700)) }, onClick = {
                                    voiceMenu = false
                                    app.updateSettings { it.voice = p.id }
                                    Speech.speak(p.sample, "en-US", .95, voiceId = p.id)
                                })
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Btn("Hear", icon = Icons.AutoMirrored.Filled.VolumeUp, kind = BtnKind.White, onClick = { Speech.speak(current.sample, "en-US", .95, voiceId = current.id) })
                }
                if (real != null && !isNaturalVoice(real)) {
                    Text(
                        "This phone only has a basic voice, so it may sound like a robot. Install a better voice in your phone’s text-to-speech settings.",
                        style = T.body(12, color = C.muted),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            SettingSwitch("Sound effects", s.effects) { v ->
                app.updateSettings { it.effects = v }
                if (v) Sounds.play("good")
            }
            SettingSwitch("Background music", s.music) { v -> app.updateSettings { it.music = v } }
            SettingRow("Volume") {
                Slider(
                    value = s.volume.toFloat(),
                    onValueChange = { v -> app.updateSettings { it.volume = v.toInt() } },
                    valueRange = 0f..100f,
                    modifier = Modifier.width(180.dp),
                    colors = SliderDefaults.colors(thumbColor = C.blueBtn, activeTrackColor = C.blueBtn),
                )
            }
        }
        Section("For grown-ups") {
            SettingSwitch("Parent / teacher lock", s.parentLock, hint = "Ask for a 4-digit PIN before opening Settings") { v ->
                if (v && s.pin.isEmpty()) pinDialog = true else app.updateSettings { it.parentLock = v }
            }
            if (s.parentLock && s.pin.isNotEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { Btn("Change PIN", kind = BtnKind.White, onClick = { pinDialog = true }) }
            }
            SettingSwitch("Daily reading reminder", s.reminder, hint = "Every day at 4:00 PM") { toggleReminder(it) }
            if (reminderNote.isNotEmpty()) Text(reminderNote, style = T.body(13, color = C.muted, weight = W700), modifier = Modifier.padding(bottom = 8.dp))
        }
        Section("Account") {
            SettingRow("Signed in as") { Text(app.user?.email.orEmpty(), style = T.body(13, color = C.muted), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            Wrap(spacing = 10.dp) {
                Btn("My Profile", kind = BtnKind.White, onClick = { nav.push("profile") })
                Btn("Log out", icon = Icons.AutoMirrored.Filled.Logout, kind = BtnKind.White, onClick = { leaving = true })
            }
            Spacer(Modifier.height(8.dp))
        }
    }
    if (pinDialog) {
        PinDialog(onDismiss = { pinDialog = false }) { pin ->
            pinDialog = false
            app.updateSettings {
                it.parentLock = true
                it.pin = pin
            }
            Lock.unlock(app.user?.email)
        }
    }
    if (leaving) LogoutDialog { leaving = false }
}

@Composable
private fun PinDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Set a parent PIN", style = T.head(22)) },
        text = {
            Column {
                Text("Grown-ups enter this PIN to open Settings.", style = T.body(14, color = C.muted))
                Spacer(Modifier.height(12.dp))
                for ((label, value, set) in listOf(Triple("New PIN", a) { v: String -> a = v }, Triple("Type it again", b) { v: String -> b = v })) {
                    Text(label, style = T.body(14, weight = W800))
                    Spacer(Modifier.height(4.dp))
                    Field(value, { set(it.filter(Char::isDigit).take(4)) }, password = true, keyboard = KeyboardType.NumberPassword)
                    Spacer(Modifier.height(10.dp))
                }
                if (error.isNotEmpty()) Text(error, style = T.body(13, color = C.red, weight = W800))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn("Cancel", kind = BtnKind.White, onClick = onDismiss)
                Btn("Save PIN", onClick = {
                    error = when {
                        !Regex("^\\d{4}$").matches(a) -> "The PIN needs exactly 4 numbers."
                        a != b -> "The two PINs don’t match."
                        else -> ""
                    }
                    if (error.isEmpty()) onSave(a)
                })
            }
        },
    )
}

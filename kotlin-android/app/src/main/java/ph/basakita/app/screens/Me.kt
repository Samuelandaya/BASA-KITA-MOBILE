package ph.basakita.app.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ph.basakita.app.data.lessons
import ph.basakita.app.data.stories
import ph.basakita.app.learnerLevel
import ph.basakita.app.state.historyIds
import ph.basakita.app.state.percentRead
import ph.basakita.app.state.storyById
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.BookRow
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.ConfirmDialog
import ph.basakita.app.ui.Field
import ph.basakita.app.ui.IconCircle
import ph.basakita.app.ui.LevelCard
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.StoryCover
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.levelBg
import ph.basakita.app.ui.levelFg
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun Avatar(size: Dp = 72.dp) {
    val app = LocalApp.current
    Box(
        Modifier
            .size(size + 6.dp)
            .background(C.yellow, CircleShape)
            .padding(3.dp)
            .background(Color.White, CircleShape)
            .padding(4.dp)
            .background(Color(app.avatarColor), CircleShape),
        contentAlignment = Alignment.Center,
    ) { Text(app.displayName.first().uppercase(), style = T.fun_((size.value * .5f).toInt(), color = C.ink)) }
}

/** "Leaving already?" before logging out. */
@Composable
fun LogoutDialog(onDismiss: () -> Unit) {
    val app = LocalApp.current
    ConfirmDialog("Leaving already?", "Your reading progress is saved. See you next time!", yes = "Log out", no = "Keep reading", onYes = {
        onDismiss()
        app.logout()
    }, onDismiss = onDismiss)
}

// ------------------------------------------------------------------ Me tab
@Composable
fun MeScreen() {
    val app = LocalApp.current
    val nav = LocalNav.current
    val level = learnerLevel(app.account)
    var leaving by remember { mutableStateOf(false) }
    val items = listOf(
        Triple(Icons.Outlined.PhotoCamera, "Scan a Book", "scan"),
        Triple(Icons.Outlined.Edit, "Create a Story", "create"),
        Triple(Icons.AutoMirrored.Filled.TrendingUp, "My Progress", "progress"),
        Triple(Icons.AutoMirrored.Filled.LibraryBooks, "My Stories", "my-stories"),
        Triple(Icons.Outlined.Settings, "Settings", "settings"),
        Triple(Icons.Outlined.Person, "My Profile", "profile"),
        Triple(Icons.AutoMirrored.Filled.HelpOutline, "Help", "help"),
    )
    AppPage(title = "Me") {
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar()
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(listOfNotNull(app.user?.firstName, app.user?.lastName).filter { it.isNotEmpty() }.joinToString(" "), style = T.head(22, weight = W700))
                    Text("@${app.user?.username.orEmpty()}", style = T.body(14, color = C.muted, weight = W800))
                    Spacer(Modifier.height(6.dp))
                    Wrap(spacing = 6.dp, runSpacing = 6.dp) {
                        Tag("Kindergarten", bg = C.cream2)
                        Tag("${level.name} reader", bg = levelBg(level.name), fg = levelFg(level.name))
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        for ((icon, label, route) in items) {
            WhiteCard(Modifier.padding(bottom = 10.dp), padding = PaddingValues(14.dp), onClick = { nav.push(route) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = C.navy)
                    Spacer(Modifier.width(14.dp))
                    Text(label, style = T.body(16, weight = W800, color = C.navy), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = C.muted)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Btn("Logout", icon = Icons.AutoMirrored.Filled.Logout, kind = BtnKind.White, expand = true, onClick = { leaving = true })
    }
    if (leaving) LogoutDialog { leaving = false }
}

// ------------------------------------------------------------------ Progress
private val week = listOf("Mon" to 8, "Tue" to 10, "Wed" to 5, "Thu" to 12, "Fri" to 13, "Sat" to 0, "Sun" to 0)

private data class Badge(val name: String, val icon: ImageVector, val bg: Color, val fg: Color, val earned: Boolean)

@Composable
fun ProgressScreen() {
    val app = LocalApp.current
    val a = app.account
    val sk = learnerLevel(a).skills
    val finished = stories.filter { a.progress[it.id]?.finished == true }
    val skills = listOf("Letter sounds" to sk.letterSounds, "English pronunciation" to sk.english, "Filipino word reading accuracy" to sk.filipino, "Understanding the story" to sk.understanding)
    val badges = listOf(
        Badge("First Story", Icons.AutoMirrored.Filled.MenuBook, C.blueBtn, Color.White, finished.isNotEmpty()),
        Badge("Sharing Star", Icons.Filled.Star, C.orange, C.ink, a.progress["mia-and-the-red-ball"]?.finished == true),
        Badge("Brave Reader", Icons.Filled.Mic, C.green, C.ink, finished.size >= 3),
        Badge("7-Day Streak", Icons.Filled.Lock, C.locked, C.muted, false),
    )
    val maxMin = week.maxOf { it.second }
    val today = LocalDate.now().dayOfWeek.value - 1
    AppPage(back = true, title = "My Progress", subtitle = if (app.sampleData) "Look how much you’ve grown!" else "Every story you read helps you grow!") {
        LevelCard()
        Spacer(Modifier.height(14.dp))
        if (!app.sampleData) {
            WhiteCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircle(Icons.Filled.Star, C.yellow, C.orangeText, iconSize = 32.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        val n = finished.size
                        Text(if (n > 0) "You’ve finished $n ${if (n == 1) "story" else "stories"}!" else "Your stars will appear here!", style = T.head(18))
                        val sounds = if (a.lessonsDone > 0) " You know ${a.lessonsDone} letter ${if (a.lessonsDone == 1) "sound" else "sounds"}!" else ""
                        Text((if (n > 0) "Keep reading to fill your chart." else "Finish your first story to start your progress chart.") + sounds, style = T.body(13, color = C.muted))
                    }
                }
            }
        } else {
            Row {
                for ((label, value) in listOf("Stories finished" to "${finished.size}", "Minutes this week" to "${week.sumOf { it.second }}", "Letter sounds" to "${a.lessonsDone}/${lessons.size}")) {
                    WhiteCard(Modifier.weight(1f).padding(horizontal = 4.dp), padding = PaddingValues(12.dp)) {
                        Text(label, style = T.body(11, color = C.muted, weight = W800))
                        Text(value, style = T.head(26, weight = W700))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            WhiteCard {
                Text("Minutes read this week", style = T.head(18))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(150.dp), verticalAlignment = Alignment.Bottom) {
                    week.forEachIndexed { i, (day, min) ->
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$min", style = T.body(11, weight = W800))
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier
                                    .width(26.dp)
                                    .height(if (min == 0) 4.dp else (min * 100f / maxMin).dp)
                                    .background(if (min == 0) Color(0xFFC9D5DF) else if (i == today) C.orange else C.blueBtn, RoundedCornerShape(8.dp)),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(day, style = T.body(11, color = C.muted, weight = W800))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        WhiteCard {
            Text("Reading skills", style = T.head(18))
            Spacer(Modifier.height(10.dp))
            for ((name, v) in skills) {
                Column(Modifier.padding(bottom = 12.dp)) {
                    Row {
                        Text(name, style = T.body(14, weight = W800), modifier = Modifier.weight(1f))
                        Text(if (v == null) "Not yet" else "$v%", style = T.body(14, weight = W800))
                    }
                    Spacer(Modifier.height(5.dp))
                    Bar(v ?: 0, color = C.green, height = 12.dp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        WhiteCard {
            Text(if (app.sampleData) "My badges" else "Badges to earn", style = T.head(18))
            Spacer(Modifier.height(12.dp))
            Wrap(spacing = 14.dp, runSpacing = 12.dp) {
                for (b in badges) {
                    Column(Modifier.width(74.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        IconCircle(if (b.earned) b.icon else Icons.Filled.Lock, if (b.earned) b.bg else C.locked, if (b.earned) b.fg else C.muted)
                        Spacer(Modifier.height(4.dp))
                        Text(b.name, textAlign = TextAlign.Center, style = T.body(11, weight = W800, color = if (b.earned) C.navy else C.muted))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ My Stories
private fun whenRead(ts: Long): String {
    val zone = ZoneId.systemDefault()
    val d = Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()
    val days = ChronoUnit.DAYS.between(Instant.ofEpochMilli(ts), Instant.now())
    return when {
        days < 1 -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> "${listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[d.monthValue - 1]} ${d.dayOfMonth}"
    }
}

private val storyTabs = listOf("Reading now", "Saved", "History", "Finished", "Made by me")
private val emptyNotes = listOf(
    "Stories you start will wait for you here.",
    "Tap the heart on a story to save it here.",
    "Every story you open is added to your reading history.",
    "Finished stories and your stars will show up here.",
    "Stories you write will be saved here.",
)

@Composable
fun MyStoriesScreen() {
    val app = LocalApp.current
    val nav = LocalNav.current
    var tab by remember { mutableIntStateOf(0) }
    val p = app.progress
    val history = historyIds(p).mapNotNull(::storyById)
    val reading = history.filter { p[it.id]?.finished != true }
    val savedStories = app.saved.mapNotNull(::storyById)
    val finished = stories.filter { p[it.id]?.finished == true }
    val counts = listOf(reading.size, savedStories.size, history.size, finished.size, app.drafts.size)
    AppPage(back = true, title = "My Stories", subtitle = "Stories you’re reading, saved, finished and made, plus your reading history.") {
        Panel {
            ScrollSeg(listOf(0, 1, 2, 3, 4), tab, { "${storyTabs[it]} (${counts[it]})" }) { tab = it }
            Spacer(Modifier.height(14.dp))
            if (counts[tab] == 0) {
                Text(emptyNotes[tab], textAlign = TextAlign.Center, style = T.body(15, color = C.muted, weight = W800), modifier = Modifier.fillMaxWidth().padding(20.dp))
            } else when (tab) {
                2 -> for (s in history) {
                    val entry = p.getValue(s.id)
                    WhiteCard(Modifier.padding(bottom = 10.dp), padding = PaddingValues(10.dp), onClick = { nav.push("books/${s.id}") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StoryCover(s, Modifier.size(60.dp), radius = 14.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.title, style = T.head(16))
                                Spacer(Modifier.height(4.dp))
                                Bar(percentRead(s, entry), height = 7.dp)
                                Text("${if (entry.finished) "Finished" else "${percentRead(s, entry)}% read"} · ${s.language}", style = T.body(12, color = C.muted, weight = W700))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(whenRead(entry.lastRead ?: 0), style = T.body(11, color = C.muted, weight = W800))
                        }
                    }
                }
                4 -> for (d in app.drafts) {
                    WhiteCard(Modifier.padding(bottom = 10.dp), onClick = { nav.push("made/${d.id}") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(60.dp).background(C.yellow, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("Aa", style = T.head(24, weight = W700)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(d.title, style = T.head(17))
                                Text("${d.language} · ${d.pages} pages", style = T.body(12, color = C.muted, weight = W700))
                            }
                            Btn("Edit", kind = BtnKind.White, onClick = { nav.push("create?story=${d.id}") })
                        }
                    }
                }
                else -> for (s in when (tab) { 0 -> reading; 1 -> savedStories; else -> finished }) Box(Modifier.padding(bottom = 14.dp)) { BookRow(s) }
            }
            if (tab == 4) Btn("Write a new story", icon = Icons.Filled.Add, kind = BtnKind.White, onClick = { nav.push("create") })
        }
    }
}

// ------------------------------------------------------------------ Profile
private val avatarColors = listOf(0xFF7FB3E0 to "Blue", 0xFFFFB27A to "Orange", 0xFF8FD6AE to "Green", 0xFFF6B6C8 to "Pink", 0xFFFFE588 to "Yellow")

@Composable
fun ProfileScreen() {
    val app = LocalApp.current
    val u = app.user ?: return
    val level = learnerLevel(app.account)
    var first by remember { mutableStateOf(u.firstName) }
    var last by remember { mutableStateOf(u.lastName) }
    var username by remember { mutableStateOf(u.username) }
    var email by remember { mutableStateOf(u.email) }
    var school by remember { mutableStateOf(u.school) }
    var teacher by remember { mutableStateOf(u.teacher) }
    var saved by remember { mutableStateOf(false) }

    @Composable
    fun field(label: String, value: String, keyboard: KeyboardType = KeyboardType.Text, set: (String) -> Unit) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(label, style = T.body(14, weight = W800))
            Spacer(Modifier.height(6.dp))
            Field(value, { set(it); saved = false }, keyboard = keyboard)
        }
    }

    AppPage(back = true, title = "My Profile", subtitle = "This is you, ${first.ifEmpty { "reader" }}!") {
        WhiteCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(104.dp)
                Spacer(Modifier.height(10.dp))
                Tag("${level.name} reader", bg = levelBg(level.name), fg = levelFg(level.name))
                Spacer(Modifier.height(12.dp))
                Text("Choose your avatar color", style = T.body(14, weight = W800))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for ((color, name) in avatarColors) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .background(Color(color), CircleShape)
                                .border(4.dp, if (app.avatarColor == color) C.navy else Color.Transparent, CircleShape)
                                .clickable { app.setAvatarColor(color) }
                                .semantics { contentDescription = "$name avatar" },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        WhiteCard {
            Text("About me", style = T.head(20))
            Spacer(Modifier.height(12.dp))
            field("First name", first) { first = it }
            field("Last name", last) { last = it }
            field("Username", username) { username = it }
            field("Parent’s email", email, KeyboardType.Email) { email = it }
            field("School", school) { school = it }
            field("Teacher", teacher) { teacher = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (saved) Text("Saved!", style = T.body(14, color = C.greenText, weight = W800), modifier = Modifier.padding(end = 12.dp))
                Btn("Save changes", onClick = {
                    app.updateUser {
                        it.firstName = first
                        it.lastName = last
                        it.username = username
                        it.email = email
                        it.school = school
                        it.teacher = teacher
                    }
                    saved = true
                })
            }
        }
    }
}

// ------------------------------------------------------------------ Help
private val faq = listOf(
    "How do I start reading a story?" to "Go to Books, tap a story, then choose Listen to Story (the story is read to you) or Start Reading (you practice reading with Marungko or phonics support).",
    "What is the Marungko Method?" to "A Filipino phonics approach that teaches letter sounds in a set order (M, A, S, I, O…), then blends them into syllables, words and sentences.",
    "The microphone isn’t working." to "Allow microphone access when your phone asks. If you tapped “Don’t allow”, open your phone’s Settings, find BASA-KITA and turn the microphone on.",
    "The story isn’t being read aloud." to "Check that your phone volume is up and that Story narration is on in Settings.",
    "How do I see a flashcard in AR?" to "Open Cards, choose a card, then tap the AR button on the 3D object and point your camera at the floor or a table.",
    "How do I scan a book?" to "Tap the camera icon in the Books search bar, take a photo of one page, then tap Read this page.",
    "How do I scan a flashcard?" to "Open Cards, tap Scan a flashcard, take a photo of one card, then tap Find this card.",
    "How do I change the story language?" to "Open Me → Settings → Reading → Story language and choose Filipino, English or Bilingual.",
    "I forgot my password." to "On the login screen, tap Forgot Password? and we’ll email you a reset link.",
)

@Composable
fun HelpScreen() {
    var q by remember { mutableStateOf("") }
    var open by remember { mutableStateOf(setOf(0)) }
    val list = faq.withIndex().filter { (_, e) -> "${e.first} ${e.second}".lowercase().contains(q.trim().lowercase()) }
    AppPage(back = true, title = "Need Help?", subtitle = "Find answers for kids, parents and teachers.") {
        Panel {
            Field(q, { q = it }, hint = "Search help topics...", leading = { Icon(Icons.Filled.Search, null) }, shape = RoundedCornerShape(26.dp))
            Spacer(Modifier.height(12.dp))
            if (list.isEmpty()) {
                Text("No answers for “$q”. Try another word.", style = T.body(15, color = C.muted, weight = W800), modifier = Modifier.padding(16.dp))
            } else {
                WhiteCard(padding = PaddingValues(horizontal = 6.dp)) {
                    list.forEachIndexed { n, (i, e) ->
                        val expanded = i in open || q.isNotEmpty() && n == 0
                        Column(Modifier.fillMaxWidth().animateContentSize().clickable { open = if (i in open) open - i else open + i }.padding(horizontal = 12.dp, vertical = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(e.first, style = T.body(15, weight = W800, color = C.navy), modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = C.muted)
                            }
                            if (expanded) Text(e.second, style = T.body(14, color = C.muted, height = 1.4), modifier = Modifier.padding(top = 8.dp))
                        }
                        if (n < list.size - 1) HorizontalDivider(color = C.cardBorder)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            WhiteCard {
                Text("Still need help?", style = T.head(18))
                Spacer(Modifier.height(4.dp))
                Text("Grown-ups can message the BASA-KITA team or the learner’s teacher.", style = T.body(14, color = C.muted))
            }
        }
    }
}

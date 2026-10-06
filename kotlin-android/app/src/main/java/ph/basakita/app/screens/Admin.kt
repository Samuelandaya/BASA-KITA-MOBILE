package ph.basakita.app.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ph.basakita.app.api.emailPattern
import ph.basakita.app.data.stories
import ph.basakita.app.learnerLevel
import ph.basakita.app.levelNames
import ph.basakita.app.state.AppState
import ph.basakita.app.state.historyIds
import ph.basakita.app.state.percentRead
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.Field
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.Seg
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.levelBg
import ph.basakita.app.ui.levelFg
import ph.basakita.app.ui.span
import kotlin.math.roundToInt

private data class Learner(
    val name: String,
    val level: String,
    val done: Int,
    val pct: Int,
    val en: Int?,
    val fil: Int?,
    val last: String,
    val tag: String = "", // "" | this device | new
)

private val sample = listOf(
    Learner("Mia Santos", "Growing", 9, 82, 84, 90, "Today"),
    Learner("Paolo Reyes", "Beginner", 3, 34, 52, 61, "3 days ago"),
    Learner("Liza Garcia", "Confident", 11, 91, 93, 95, "Yesterday"),
    Learner("Ben Aquino", "Beginner", 4, 47, 66, null, "Yesterday"),
    Learner("Ana Villanueva", "Growing", 7, 58, 71, 80, "2 days ago"),
)

@Serializable
private data class AddedLearner(val name: String, val email: String)

@Serializable
private data class AddedBook(val title: String, val language: String)

@Serializable
private data class AdminData(val learners: List<AddedLearner> = emptyList(), val books: List<AddedBook> = emptyList())

private const val ADMIN_KEY = "basa-kita:admin:v1"

private fun lastActive(ts: Long?): String {
    if (ts == null) return "Not yet"
    val days = (System.currentTimeMillis() - ts) / 86_400_000
    return if (days < 1) "Today" else if (days == 1L) "Yesterday" else "$days days ago"
}

private fun learners(app: AppState, added: List<AddedLearner>): List<Learner> {
    val onDevice = app.accounts.filterKeys { !it.startsWith("admin") }.map { (email, a) ->
        val lvl = learnerLevel(a)
        val pct = (stories.sumOf { percentRead(it, a.progress[it.id]) }.toDouble() / stories.size).roundToInt()
        val recent = historyIds(a.progress).firstOrNull()
        val handle = email.substringBefore('@').replace(Regex("[._-]+"), " ")
        Learner(
            if (handle.isEmpty()) email else handle.replaceFirstChar { it.uppercase() },
            lvl.name, lvl.skills.finished, pct, lvl.skills.english, lvl.skills.filipino,
            lastActive(recent?.let { a.progress[it]?.lastRead }), "this device",
        )
    }
    return added.map { Learner(it.name, "Beginner", 0, 0, null, null, "Not yet", "new") } + onDevice + sample
}

/** Teacher view. It works best on a tablet; on a phone it is a compact list of the same data. */
@Composable
fun AdminScreen() {
    val app = LocalApp.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("basa-kita", Context.MODE_PRIVATE) }
    val saved = remember { prefs.getString(ADMIN_KEY, null)?.let { runCatching { Json.decodeFromString(AdminData.serializer(), it) }.getOrNull() } ?: AdminData() }
    val addedLearners = remember { mutableStateListOf(*saved.learners.toTypedArray()) }
    val addedBooks = remember { mutableStateListOf(*saved.books.toTypedArray()) }
    var view by remember { mutableIntStateOf(0) }
    var q by remember { mutableStateOf("") }
    var levelFilter by remember { mutableStateOf("All") }
    var dialog by remember { mutableStateOf<String?>(null) } // learner | book
    var leaving by remember { mutableStateOf(false) }

    fun persist() = prefs.edit().putString(ADMIN_KEY, Json.encodeToString(AdminData.serializer(), AdminData(addedLearners.toList(), addedBooks.toList()))).apply()

    val all = learners(app, addedLearners)
    val books = stories.size + addedBooks.size
    val rows = all.filter { it.name.lowercase().contains(q.trim().lowercase()) && (levelFilter == "All" || it.level == levelFilter) }
    val labels = listOf("Overview", "Learners", "Books", "Reports", "Account")

    AppPage(title = if (view == 0) "Welcome, Teacher ${app.displayName}" else labels[view], subtitle = "Kindergarten class overview · [SECTION NAME]") {
        ScrollSeg(listOf(0, 1, 2, 3, 4), view, { labels[it] }) {
            view = it
            q = ""
        }
        Spacer(Modifier.height(10.dp))
        Wrap(spacing = 10.dp) {
            Btn("Add learner", icon = Icons.Filled.Add, kind = BtnKind.White, onClick = { dialog = "learner" })
            Btn("Add storybook", icon = Icons.Filled.Add, onClick = { dialog = "book" })
        }
        Spacer(Modifier.height(10.dp))
        when (view) {
            1 -> {
                ScrollSeg(listOf("All") + levelNames, levelFilter, { it }) { levelFilter = it }
                Spacer(Modifier.height(10.dp))
                Field(q, { q = it }, hint = "Search learners...", leading = { Icon(Icons.Filled.Search, null) })
                Spacer(Modifier.height(12.dp))
                if (rows.isEmpty()) Text("No learners match.", style = T.body(14, color = C.muted)) else rows.forEach { LearnerCard(it) }
            }
            2 -> {
                val list = addedBooks.map { Triple(it.title, it.language, "Draft") } + stories.map { Triple(it.title, it.language, "${it.pages.size} pages") }
                for ((title, lang, note) in list) {
                    WhiteCard(Modifier.padding(bottom = 8.dp), padding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, style = T.head(16), modifier = Modifier.weight(1f))
                            Tag(lang)
                            Spacer(Modifier.width(8.dp))
                            Text(note, style = T.body(12, color = C.muted, weight = W800))
                        }
                    }
                }
            }
            3 -> Reports(all, books, context)
            4 -> WhiteCard {
                Text("Account", style = T.head(20))
                Spacer(Modifier.height(10.dp))
                val name = listOfNotNull(app.user?.firstName, app.user?.lastName).filter { it.isNotEmpty() }.joinToString(" ")
                for ((k, v) in listOf("Name" to name, "Email" to app.user?.email.orEmpty(), "Role" to "Admin (from the backend)", "Server" to "Not connected yet (demo data)")) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(k, style = T.body(14, weight = W800), modifier = Modifier.weight(1f))
                        Text(v, textAlign = TextAlign.End, style = T.body(14, color = C.muted))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Btn("Log out", icon = Icons.AutoMirrored.Filled.Logout, kind = BtnKind.White, onClick = { leaving = true })
            }
            else -> {
                Stats(all, books)
                Spacer(Modifier.height(6.dp))
                Text("Recent activity", style = T.head(18), modifier = Modifier.padding(vertical = 8.dp))
                for ((dot, who, what, at) in listOf(
                    Activity(C.green, "Mia Santos", "finished The Magic Kite · 3 stars", "10 minutes ago"),
                    Activity(C.blueBtn, "Sam Dela Cruz", "started Lesson 6: Bb", "25 minutes ago"),
                    Activity(C.orange, "Paolo Reyes", "needs help with letter sounds", "Yesterday"),
                )) {
                    WhiteCard(Modifier.padding(bottom = 8.dp), padding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(dot, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                buildAnnotatedString {
                                    withStyle(span(T.body(14, weight = W800))) { append(who) }
                                    withStyle(span(T.body(14))) { append(" $what\n") }
                                    withStyle(span(T.body(12, color = C.muted))) { append(at) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    when (dialog) {
        "learner" -> AddLearnerDialog(onDismiss = { dialog = null }) { name, email ->
            addedLearners.add(0, AddedLearner(name, email))
            persist()
            dialog = null
        }
        "book" -> AddBookDialog(onDismiss = { dialog = null }) { title, lang ->
            addedBooks.add(0, AddedBook(title, lang))
            persist()
            dialog = null
        }
    }
    if (leaving) LogoutDialog { leaving = false }
}

private data class Activity(val dot: Color, val who: String, val what: String, val at: String)

@Composable
private fun LearnerCard(l: Learner) {
    WhiteCard(Modifier.padding(bottom = 10.dp), padding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    withStyle(span(T.head(17))) { append(l.name) }
                    if (l.tag.isNotEmpty()) withStyle(span(T.body(12, color = C.muted))) { append(" · ${l.tag}") }
                },
                modifier = Modifier.weight(1f),
            )
            Tag(l.level, bg = levelBg(l.level), fg = levelFg(l.level))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Bar(l.pct, color = if (l.pct < 40) C.red else C.blueBtn, height = 8.dp, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text("${l.pct}%", style = T.body(13, weight = W800))
        }
        Spacer(Modifier.height(6.dp))
        Text("${l.done} stories · English ${l.en?.let { "$it%" } ?: "—"} · Filipino ${l.fil?.let { "$it%" } ?: "—"} · ${l.last}", style = T.body(12, color = C.muted, weight = W700))
    }
}

@Composable
private fun Stats(all: List<Learner>, books: Int) {
    val avg = if (all.isEmpty()) 0 else (all.sumOf { it.pct }.toDouble() / all.size).roundToInt()
    val support = all.count { it.pct < 40 && it.last != "Not yet" }
    val active = all.count { it.last == "Today" || it.last == "Yesterday" }

    @Composable
    fun Row2(a: List<String>, b: List<String>, colorA: Color, colorB: Color) {
        Row {
            for ((t, c) in listOf(a to colorA, b to colorB)) {
                WhiteCard(Modifier.weight(1f).padding(4.dp), padding = PaddingValues(12.dp)) {
                    Text(t[0], style = T.body(11, color = C.muted, weight = W800))
                    Text(t[1], style = T.head(28, weight = W700))
                    Text(t[2], style = T.body(11, color = c, weight = W700))
                }
            }
        }
    }
    Row2(listOf("Learners", "${all.size}", "$active active in 2 days"), listOf("Storybooks", "$books", "In the library"), C.greenText, C.muted)
    Row2(listOf("Avg. progress", "$avg%", "Across all learners"), listOf("Need support", "$support", "Below 40% progress"), C.muted, Color(0xFFB5442A))
}

@Composable
private fun Reports(all: List<Learner>, books: Int, context: Context) {
    val support = all.filter { it.pct < 40 && it.last != "Not yet" }
    fun mean(xs: List<Int?>): Int? = xs.filterNotNull().takeIf { it.isNotEmpty() }?.let { (it.sum().toDouble() / it.size).roundToInt() }
    val en = mean(all.map { it.en })
    val fil = mean(all.map { it.fil })
    fun csv() = (listOf("Name,Level,Stories done,Progress %,English pronunciation %,Filipino accuracy %,Last active") +
        all.map { "\"${it.name}\",${it.level},${it.done},${it.pct},${it.en ?: ""},${it.fil ?: ""},${it.last}" }).joinToString("\n")

    Stats(all, books)
    Spacer(Modifier.height(6.dp))
    WhiteCard {
        Text("Reading levels", style = T.head(18))
        Spacer(Modifier.height(10.dp))
        for (n in levelNames) {
            val count = all.count { it.level == n }
            Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(96.dp)) { Tag(n, bg = levelBg(n), fg = levelFg(n)) }
                Spacer(Modifier.width(10.dp))
                Bar(if (all.isEmpty()) 0 else count * 100 / all.size, height = 10.dp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text("$count", style = T.body(14, weight = W800))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Class averages", style = T.head(18))
        Spacer(Modifier.height(8.dp))
        for ((name, v) in listOf("English pronunciation" to en, "Filipino word reading accuracy" to fil)) {
            Column(Modifier.padding(bottom = 10.dp)) {
                Row {
                    Text(name, style = T.body(14, weight = W800), modifier = Modifier.weight(1f))
                    Text(v?.let { "$it%" } ?: "Not yet", style = T.body(14, weight = W800))
                }
                Spacer(Modifier.height(4.dp))
                Bar(v ?: 0, color = C.green, height = 12.dp)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    WhiteCard {
        Text("Learners who need support", style = T.head(18))
        Spacer(Modifier.height(8.dp))
        if (support.isEmpty()) Text("Everyone is on track.", style = T.body(14, color = C.muted))
        for (l in support) {
            Row(Modifier.padding(bottom = 6.dp)) {
                Text(l.name, style = T.body(14, weight = W800), modifier = Modifier.weight(1f))
                Text("${l.pct}% progress", style = T.body(13, color = C.muted))
            }
        }
        Spacer(Modifier.height(8.dp))
        Btn("Copy learners (CSV)", icon = Icons.Filled.ContentCopy, kind = BtnKind.White, onClick = {
            context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("BASA-KITA learners", csv()))
            Toast.makeText(context, "Copied. Paste it into a spreadsheet.", Toast.LENGTH_SHORT).show()
        })
    }
}

@Composable
private fun AddLearnerDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Add learner", style = T.head(22)) },
        text = {
            Column {
                Field(name, { name = it }, label = "Learner’s name")
                Spacer(Modifier.height(10.dp))
                Field(email, { email = it }, label = "Parent’s email", keyboard = KeyboardType.Email)
                if (error.isNotEmpty()) Text(error, style = T.body(13, color = C.red, weight = W800), modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn("Cancel", kind = BtnKind.White, onClick = onDismiss)
                Btn("Add learner", onClick = {
                    error = when {
                        name.isBlank() -> "Please enter the learner’s name."
                        !emailPattern.matches(email.trim()) -> "Please enter a valid parent email."
                        else -> ""
                    }
                    if (error.isEmpty()) onAdd(name.trim(), email.trim())
                })
            }
        },
    )
}

@Composable
private fun AddBookDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Filipino") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Add storybook", style = T.head(22)) },
        text = {
            Column {
                Field(title, { title = it }, label = "Title")
                Spacer(Modifier.height(10.dp))
                Seg(listOf("Filipino", "English", "Bilingual"), language, { it }) { language = it }
                if (error.isNotEmpty()) Text(error, style = T.body(13, color = C.red, weight = W800), modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn("Cancel", kind = BtnKind.White, onClick = onDismiss)
                Btn("Add storybook", onClick = {
                    if (title.isBlank()) error = "Please enter the storybook title." else onAdd(title.trim(), language)
                })
            }
        },
    )
}

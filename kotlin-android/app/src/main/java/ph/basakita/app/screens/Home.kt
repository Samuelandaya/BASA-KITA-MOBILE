package ph.basakita.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ph.basakita.app.data.stories
import ph.basakita.app.learnerLevel
import ph.basakita.app.recommendStories
import ph.basakita.app.starterStories
import ph.basakita.app.state.continueId
import ph.basakita.app.state.isNewReader
import ph.basakita.app.state.percentRead
import ph.basakita.app.state.storyById
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.SectionTitle
import ph.basakita.app.ui.StoryCard
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.asset
import ph.basakita.app.ui.levelBg
import ph.basakita.app.ui.levelFg
import ph.basakita.app.ui.span
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun greeting(): String {
    val h = LocalDateTime.now().hour
    return if (h < 12) "Good morning" else if (h < 18) "Good afternoon" else "Good evening"
}

private fun dateLabel(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))

@Composable
fun HomeScreen() {
    val app = LocalApp.current
    val isNew = isNewReader(app.progress)
    AppPage {
        Spacer(Modifier.height(34.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${greeting()}, ${app.displayName}!", style = T.head(26, weight = W700, height = 1.1))
                Spacer(Modifier.height(4.dp))
                Text(if (isNew) "Your reading adventure starts today!" else "Ready for another fun reading adventure?", style = T.body(14, color = C.muted))
                Spacer(Modifier.height(10.dp))
                Text(
                    dateLabel(),
                    style = T.body(12, color = C.orangeText, weight = W800),
                    modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).border(1.5.dp, C.orange, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
            AsyncImage(asset("images/teacher-1.png"), null, Modifier.size(84.dp))
        }
        Spacer(Modifier.height(16.dp))
        QuickActions()
        Spacer(Modifier.height(16.dp))
        if (isNew) NewReaderHome() else ReturningHome()
    }
}

@Composable
private fun QuickActions() {
    val nav = LocalNav.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Quick(Icons.Filled.PhotoCamera, "Scan a book", C.blueBtn, Color.White, Modifier.weight(1f)) { nav.push("scan") }
        Quick(Icons.Filled.ViewInAr, "AR flashcards", C.orange, C.ink, Modifier.weight(1f)) { nav.go("cards") }
    }
}

@Composable
private fun Quick(icon: ImageVector, label: String, color: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(22.dp)).background(color).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = T.body(15, color = fg, weight = W800))
    }
}

private data class Step(val done: Boolean, val icon: ImageVector, val color: Color, val fg: Color, val title: String, val route: String)

@Composable
private fun NewReaderHome() {
    val app = LocalApp.current
    val nav = LocalNav.current
    val list = starterStories(app.settings.language)
    val first = list.firstOrNull() ?: stories.first()
    val steps = listOf(
        Step(app.progress.isNotEmpty(), Icons.Filled.Headphones, C.orange, C.ink, "Listen to a story", "books/${first.id}/listen"),
        Step(app.lessonsDone > 0, Icons.Filled.School, C.blueBtn, Color.White, "Learn your first sound: Mm", "learn"),
        Step(false, Icons.Filled.Star, C.green, C.ink, "Earn your first star", "books/${first.id}"),
    )
    val doneCount = steps.count { it.done }
    Panel {
        WhiteCard {
            Text("Welcome to BASA-KITA!", style = T.body(14, color = C.orangeText, weight = W800))
            Spacer(Modifier.height(4.dp))
            Text("Let’s read your first story together, ${app.displayName}!", style = T.head(22, weight = W700, height = 1.15))
            Spacer(Modifier.height(12.dp))
            Text("I want to read in:", style = T.body(14, weight = W800))
            Spacer(Modifier.height(6.dp))
            ScrollSeg(listOf("Filipino", "English", "Bilingual"), app.settings.language, { if (it == "Bilingual") "Both" else it }) { v ->
                app.updateSettings {
                    it.language = v
                    it.languageChosen = true
                }
            }
            Spacer(Modifier.height(14.dp))
            Btn("Start my first story", icon = Icons.Filled.PlayArrow, big = true, expand = true, onClick = { nav.push("books/${first.id}/listen") })
            Text("“${first.title}” · ${first.minutes} min", style = T.body(13, color = C.muted, weight = W800))
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your first steps", style = T.head(20), modifier = Modifier.weight(1f))
            Text("$doneCount of ${steps.size} done", style = T.body(13, color = C.muted, weight = W800))
        }
        Spacer(Modifier.height(10.dp))
        for (s in steps) {
            WhiteCard(Modifier.padding(bottom = 10.dp), padding = androidx.compose.foundation.layout.PaddingValues(12.dp), onClick = { if (s.route == "learn") nav.go(s.route) else nav.push(s.route) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(s.color, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(s.icon, null, tint = s.fg, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(s.title, style = T.body(16, weight = W800, color = C.navy), modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(28.dp).background(if (s.done) C.green else Color.Transparent, CircleShape).border(2.dp, if (s.done) C.green else Color(0xFFB7C4D1), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { if (s.done) Icon(Icons.Filled.Check, "Done", tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        SectionTitle("Easy stories to start with", "See all") { nav.go("books") }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(list.take(4)) { StoryCard(it) } }
        Spacer(Modifier.height(14.dp))
        Text(
            buildAnnotatedString {
                withStyle(span(T.body(13, color = C.navy, weight = W800))) { append("Tip for grown-ups: ") }
                withStyle(span(T.body(13, color = C.muted))) { append("sit with your child for the first story and read along together.") }
            },
        )
    }
}

@Composable
private fun ReturningHome() {
    val app = LocalApp.current
    val nav = LocalNav.current
    val level = learnerLevel(app.account)
    val currentId = continueId(app.progress)
    val current = currentId?.let(::storyById)
    val recs = recommendStories(app.account, level, app.settings.language, count = 5).filter { it.story.id != currentId }.take(4)
    Panel {
        if (current != null) {
            val pct = percentRead(current, app.progress[current.id])
            WhiteCard(color = C.cream) {
                Text("Continue Reading", style = T.body(14, color = C.orangeText, weight = W800))
                Spacer(Modifier.height(4.dp))
                Text(current.title, style = T.head(24, weight = W700))
                Spacer(Modifier.height(4.dp))
                Text("You’re $pct% through this story.", style = T.body(13, color = C.muted))
                Spacer(Modifier.height(8.dp))
                Bar(pct)
                Spacer(Modifier.height(14.dp))
                Btn("Continue", icon = Icons.Filled.PlayArrow, expand = true, onClick = { nav.push("books/${current.id}/normal") })
            }
        } else {
            WhiteCard {
                Text("Pick a new story to start!", style = T.head(20))
                Spacer(Modifier.height(10.dp))
                Btn("Explore Storybooks", onClick = { nav.go("books") })
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recommended for you", style = T.head(20), modifier = Modifier.weight(1f))
            Tag("${level.name} Reader", bg = levelBg(level.name), fg = levelFg(level.name), modifier = Modifier.clickable { nav.push("progress") })
        }
        Spacer(Modifier.height(10.dp))
        if (recs.isEmpty()) {
            Text("You’ve finished every story! Try writing your own in Create.", style = T.body(14, color = C.muted))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(recs) { StoryCard(it.story, note = it.reason) } }
        }
    }
}

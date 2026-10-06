package ph.basakita.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ph.basakita.app.data.Story
import ph.basakita.app.data.stories
import ph.basakita.app.state.percentRead
import ph.basakita.app.state.storyById
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.BookRow
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.SaveHeart
import ph.basakita.app.ui.StoryCover
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.Wrap

private fun matches(s: Story, filter: String, saved: List<String>) =
    if (filter == "Saved") saved.contains(s.id) else filter == "All" || s.language == filter || s.language == "Bilingual"

@Composable
fun StorybooksScreen() {
    val app = LocalApp.current
    val nav = LocalNav.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val list = stories.filter { matches(it, filter, app.saved) && it.title.lowercase().contains(query.trim().lowercase()) }
    AppPage(banner = "Explore Storybooks") {
        Panel {
            Text("Ready for another fun reading adventure?", style = T.body(15, color = C.muted))
            Spacer(Modifier.height(12.dp))
            // Search bar with the search-by-picture icon (opens the camera scanner).
            val shape = RoundedCornerShape(32.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(5.dp, shape, ambientColor = Color(0x14142938), spotColor = Color(0x14142938))
                    .background(Color(0xFFFFFAF0), shape)
                    .border(1.5.dp, Color(0xFFD9CFBD), shape)
                    .padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, null, tint = Color(0xFF7A8794), modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text("Search stories...", style = T.body(16, color = C.muted))
                    BasicTextField(query, { query = it }, textStyle = T.body(16), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                IconButton(onClick = { nav.push("scan") }) {
                    Icon(Icons.Filled.CenterFocusStrong, "Search by picture: scan a book", tint = Color(0xFF7A8794), modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (f in listOf("All", "Filipino", "English", "Saved")) {
                    Row(
                        Modifier
                            .height(44.dp)
                            .shadow(4.dp, RoundedCornerShape(26.dp))
                            .clip(RoundedCornerShape(26.dp))
                            .background(if (filter == f) C.orange else Color.White)
                            .clickable { filter = f }
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (f == "Saved") Icon(Icons.Filled.Favorite, null, tint = C.ink, modifier = Modifier.padding(end = 6.dp).size(16.dp))
                        Text(f.uppercase(), style = T.pill(16))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (list.isEmpty()) {
                Text(
                    if (filter == "Saved" && app.saved.isEmpty()) "Tap the heart on a story to save it here." else "No stories match “$query”. Try another word!",
                    textAlign = TextAlign.Center,
                    style = T.body(15, color = C.muted, weight = W800),
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                )
            } else {
                for (s in list) Box(Modifier.padding(bottom = 14.dp)) { BookRow(s) }
            }
        }
    }
}

@Composable
fun StoryDetailsScreen(id: String) {
    val story = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    val entry = app.progress[id]
    val pct = percentRead(story, entry)
    AppPage(back = true, banner = story.title) {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(story.language)
                Spacer(Modifier.width(6.dp))
                if (story.shownLevel != story.language) Tag(story.shownLevel, bg = C.orange)
                Spacer(Modifier.weight(1f))
                SaveHeart(story)
            }
            Spacer(Modifier.height(12.dp))
            StoryCover(story, height = 190.dp, radius = 28.dp)
            Spacer(Modifier.height(12.dp))
            Text(story.blurb, style = T.body(16, weight = W800, height = 1.35))
            Spacer(Modifier.height(10.dp))
            Wrap {
                Meta(Icons.Filled.Schedule, "${story.minutes} minutes", Color(0xFFE2F0FB))
                Meta(Icons.Filled.FavoriteBorder, story.theme, Color(0xFFDDF3E6))
            }
            if (entry != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    if (entry.finished) "You finished this story!" else "You’re $pct% through · page ${entry.page.coerceIn(1, story.pages.size)} of ${story.pages.size}",
                    style = T.body(14, weight = W800, color = C.navy),
                )
                Spacer(Modifier.height(6.dp))
                Bar(pct)
                if (!entry.finished && entry.page > 1) {
                    Spacer(Modifier.height(10.dp))
                    Btn("Continue Reading", icon = Icons.Filled.PlayArrow, kind = BtnKind.White, onClick = { nav.push("books/$id/normal") })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Choose your Reading Mode", style = T.head(22, weight = W700, height = 1.1), modifier = Modifier.weight(1f))
                Box(Modifier.size(54.dp).background(C.navy, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = C.yellow2, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            ModeCard(Color(0xFFFFB27A), C.orange, Icons.Filled.Headphones, "Let the AI read the whole story aloud while you follow along.", "Listen to Story") { nav.push("books/$id/listen") }
            Spacer(Modifier.height(12.dp))
            ModeCard(
                Color(0xFF9CCBF5), Color(0xFF1F7FE0), Icons.AutoMirrored.Filled.MenuBook,
                if (story.language == "English") "Learn and Practice Reading with English Phonics" else "Learn and Practice Reading with the Marungko Method",
                "Start Reading",
            ) { nav.push("books/$id/read") }
            Spacer(Modifier.height(14.dp))
            Text("More ways to read:", style = T.body(14, weight = W800, color = C.navy), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Wrap(center = true, spacing = 10.dp, runSpacing = 6.dp, modifier = Modifier.fillMaxWidth()) {
                Btn("Read With Me", icon = Icons.Filled.Mic, kind = BtnKind.White, onClick = { nav.push("books/$id/with-me") })
                Btn("Read on My Own", icon = Icons.AutoMirrored.Outlined.MenuBook, kind = BtnKind.White, onClick = { nav.push("books/$id/normal") })
            }
        }
    }
}

@Composable
private fun Meta(icon: ImageVector, text: String, bg: Color) {
    Row(Modifier.background(bg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = C.navy, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text.uppercase(), style = T.body(11, weight = W800, color = C.navy))
    }
}

@Composable
private fun ModeCard(color1: Color, color2: Color, icon: ImageVector, text: String, button: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(color1, color2)))
            .border(3.dp, Color.White, shape)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(92.dp).background(Color.White.copy(alpha = .4f), CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(76.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color2, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(text, textAlign = TextAlign.Center, style = T.body(15, color = C.ink, weight = W700, height = 1.3))
        Spacer(Modifier.height(12.dp))
        Text(
            button.uppercase(),
            style = T.pill(18),
            modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(C.yellow).clickable(onClick = onClick).padding(horizontal = 28.dp, vertical = 14.dp),
        )
    }
}

@Composable
fun MissingStory() {
    val nav = LocalNav.current
    AppPage(back = true, title = "Story not found") { Btn("Back to Storybooks", onClick = { nav.go("books") }) }
}

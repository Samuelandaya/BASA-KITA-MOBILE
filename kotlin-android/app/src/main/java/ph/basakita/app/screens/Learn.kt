package ph.basakita.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.basakita.app.data.Lesson
import ph.basakita.app.data.decks
import ph.basakita.app.data.lessons
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.data.stories
import ph.basakita.app.data.syllables
import ph.basakita.app.services.Sounds
import ph.basakita.app.services.Speech
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.MicButton
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.Seg
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.tile

private val vowels = listOf("a", "e", "i", "o", "u")

// English phonics: short-vowel word families, and how to say each letter's sound.
private val families = mapOf(
    "a" to listOf("cat", "map", "bag", "hat", "sad"),
    "e" to listOf("bed", "net", "pen", "red", "leg"),
    "i" to listOf("pig", "sit", "big", "lip", "fin"),
    "o" to listOf("dog", "top", "hot", "box", "mop"),
    "u" to listOf("sun", "bus", "cup", "bug", "mud"),
)
private val soundOf = mapOf(
    "a" to "ah", "e" to "eh", "i" to "ih", "o" to "aw", "u" to "uh", "b" to "buh", "c" to "kuh", "d" to "duh", "f" to "fff",
    "g" to "guh", "h" to "huh", "j" to "juh", "k" to "kuh", "l" to "lll", "m" to "mmm", "n" to "nnn", "p" to "puh", "r" to "rrr",
    "s" to "sss", "t" to "tuh", "v" to "vvv", "w" to "wuh", "x" to "ks", "y" to "yuh", "z" to "zzz",
)

@Composable
fun LearnScreen() {
    val app = LocalApp.current
    var tab by remember { mutableIntStateOf(0) }
    AppPage(title = "Let’s Learn Our Sounds!", subtitle = "Marungko Method · one letter sound at a time, then practice!") {
        Panel {
            ScrollSeg(listOf(0, 1, 2), tab, { listOf("Letter Sounds", "Marungko Practice", "English Phonics")[it] }) { tab = it }
            Spacer(Modifier.height(14.dp))
            when (tab) {
                0 -> LetterSounds()
                1 -> MarungkoPractice(app.lessonsDone)
                else -> PhonicsPractice()
            }
        }
    }
}

@Composable
private fun LetterSounds() {
    val app = LocalApp.current
    val nav = LocalNav.current
    var filipino by remember { mutableStateOf(true) }
    val done = app.lessonsDone
    val allDone = done >= lessons.size
    val next = lessons[if (allDone) lessons.size - 1 else done]
    val practiceStory = stories.firstOrNull { it.focus.letter == next.letter } ?: stories.first()
    WhiteCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(96.dp).clip(RoundedCornerShape(26.dp)).background(C.orange).clickable { Speech.speak(next.letter.takeLast(1), "fil-PH", .8) },
                contentAlignment = Alignment.Center,
            ) { Text(next.letter, style = T.head(48, color = C.ink, weight = W700)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Up next · Lesson ${next.id}", style = T.body(13, color = C.orangeText, weight = W800))
                Text("The ${next.letter} sound", style = T.head(22, weight = W700))
                Text("Tap the letter to hear it, then say it: ${next.fil} · ${next.eng}", style = T.body(13, color = C.muted))
            }
        }
        Spacer(Modifier.height(12.dp))
        Bar(done * 100 / lessons.size)
        Spacer(Modifier.height(4.dp))
        Text("$done of ${lessons.size} letter sounds learned", style = T.body(12, color = C.muted))
        Spacer(Modifier.height(12.dp))
        Btn(
            if (allDone) "All sounds learned!" else "I can say it!",
            icon = Icons.Filled.Check, expand = true,
            onClick = if (allDone) null else ({
                Sounds.play("good")
                app.completeLesson()
            }),
        )
        Btn("Practice in a story", kind = BtnKind.White, expand = true, onClick = { nav.push("books/${practiceStory.id}/read") })
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("All lessons", style = T.head(20), modifier = Modifier.weight(1f))
        Seg(listOf(true, false), filipino, { if (it) "Filipino" else "English" }) { filipino = it }
    }
    Spacer(Modifier.height(10.dp))
    lessons.chunked(3).forEachIndexed { row, three ->
        Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            three.forEachIndexed { col, l ->
                val i = row * 3 + col
                LessonTile(l, if (i < done) "done" else if (i == done) "now" else "locked", filipino, Modifier.weight(1f))
            }
            repeat(3 - three.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun LessonTile(l: Lesson, status: String, filipino: Boolean, modifier: Modifier) {
    val word = if (filipino) l.fil else l.eng
    val locked = status == "locked"
    Column(
        modifier
            .aspectRatio(1.05f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (locked) Color(0xFFF6F3EC) else Color.White)
            .border(2.dp, if (status == "done") C.green else if (status == "now") C.orange else C.cardBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = !locked) { Speech.speak("${l.letter.takeLast(1)}. $word", if (filipino) "fil-PH" else "en-US", .85) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(l.letter, style = T.head(28, weight = W700, color = if (locked) C.muted else C.navy))
        Text(word, style = T.body(12, weight = W800, color = if (locked) C.muted else C.navy))
        Spacer(Modifier.height(2.dp))
        when (status) {
            "done" -> Icon(Icons.Filled.Check, "Learned", tint = C.greenText, modifier = Modifier.size(14.dp))
            "now" -> Text("Now", style = T.body(11, weight = W800, color = C.orangeDark))
            else -> Icon(Icons.Filled.Lock, "Locked", tint = C.muted, modifier = Modifier.size(13.dp))
        }
    }
}

/** One word at a time: tap the parts, hear the word, then say it into the microphone. */
@Composable
private fun WordDrill(words: List<String>, lang: String, parts: (String) -> List<String>, partSound: (String) -> String, joiner: String) {
    val mic = rememberMic(lang)
    val scope = rememberCoroutineScope()
    var idx by remember(words) { mutableIntStateOf(0) }
    var stars by remember(words) { mutableStateOf(setOf<String>()) }
    val word = words[idx]
    val said = mic.heard.contains(normalizeWord(word))
    LaunchedEffect(words) { mic.reset() }
    LaunchedEffect(said, word) {
        if (said && word !in stars) {
            stars = stars + word
            Sounds.play("good")
        }
    }

    fun pick(i: Int) {
        mic.reset()
        idx = i
    }

    fun say(t: String, rate: Double = .75) = Speech.speak(t, lang, rate)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(words) { i, w ->
            Row(
                Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (i == idx) C.paleBlue else Color.White)
                    .border(2.dp, if (i == idx) C.blueBtn else C.cardBorder, RoundedCornerShape(16.dp))
                    .clickable { pick(i) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(w, style = T.body(15, weight = W800))
                if (w in stars) Icon(Icons.Filled.Star, "Said", tint = C.yellow2, modifier = Modifier.padding(start = 6.dp).size(16.dp))
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Wrap(center = true, modifier = Modifier.fillMaxWidth()) {
                parts(word).forEachIndexed { i, p ->
                    if (i > 0) Text(joiner, style = T.head(26, color = C.orange, weight = W700))
                    Box(Modifier.defaultMinSize(62.dp, 62.dp).tile(radius = 18).clickable { say(partSound(p), .7) }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(p, style = T.head(30, weight = W700))
                    }
                }
                Text("=", style = T.head(26, color = C.orange, weight = W700))
                Text(word, style = T.head(36, color = if (said) C.green else C.navy, weight = W700), modifier = Modifier.clickable { say(word) })
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (said) "Great job! You said it!" else if (mic.listening) "Listening… say the word now!" else "Tap each part, blend them, then say the word.",
                textAlign = TextAlign.Center,
                style = T.body(14, color = C.muted, weight = W800),
            )
            Spacer(Modifier.height(10.dp))
            MicButton(mic.listening, "Say it!") { scope.launch { mic.listen(listOf(word)) } }
            if (mic.notice.isNotEmpty()) Text(mic.notice, textAlign = TextAlign.Center, style = T.body(13, color = C.red, weight = W800))
            Wrap(center = true, spacing = 10.dp) {
                if (!said) Btn("I said it!", icon = Icons.Filled.Check, kind = BtnKind.White, onClick = { mic.markAll(listOf(word)) })
                if (said && idx < words.size - 1) Btn("Next word", icon = Icons.AutoMirrored.Filled.ArrowForward, onClick = { pick(idx + 1) })
            }
            Text("${stars.size} of ${words.size} words said", style = T.body(12, color = C.muted))
        }
    }
}

@Composable
private fun LetterButton(text: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .defaultMinSize(56.dp, 52.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (on) C.orange else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = T.head(20, weight = W700)) }
}

/** Marungko practice: the letter sounds learned so far, blended into syllables and words. */
@Composable
private fun MarungkoPractice(lessonsDone: Int) {
    var letter by remember { mutableStateOf<String?>(null) }
    val open = lessons.take(lessonsDone + 1)
    val current = letter ?: open.last().letter
    val lesson = lessons.first { it.letter == current }
    val sound = if (current == "NGng") "ng" else current.takeLast(1).lowercase()
    val isVowel = sound in vowels
    val sylls = if (isVowel) listOf(sound) else vowels.map { "$sound$it" }
    val deck = decks.firstOrNull { it.letter == current }
    val words = (deck?.cards?.map { it.fil }.orEmpty() + lesson.fil).distinct()
    Wrap { for (l in open) LetterButton(l.letter, l.letter == current) { letter = l.letter } }
    Spacer(Modifier.height(12.dp))
    WhiteCard {
        Text(if (isVowel) "The vowel $current" else "Blend $current with the vowels", style = T.head(18))
        Spacer(Modifier.height(10.dp))
        Wrap(spacing = 10.dp, runSpacing = 10.dp) {
            for (s in sylls) {
                Box(Modifier.defaultMinSize(64.dp, 64.dp).tile(radius = 18).clickable { Speech.speak(s, "fil-PH", .75) }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(s, style = T.head(30, weight = W700))
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    key(current) { WordDrill(words, "fil-PH", { syllables(it) }, { it }, "·") }
}

/** English phonics practice: sound out short-vowel words letter by letter. */
@Composable
private fun PhonicsPractice() {
    var vowel by remember { mutableStateOf("a") }
    Wrap {
        for (v in vowels) {
            Box(
                Modifier
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (v == vowel) C.orange else Color.White)
                    .clickable {
                        vowel = v
                        Speech.speak(soundOf.getValue(v), "en-US", .7)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) { Text("short $v", style = T.head(16, weight = W700)) }
        }
    }
    Spacer(Modifier.height(12.dp))
    key(vowel) { WordDrill(families.getValue(vowel), "en-US", { w -> w.map(Char::toString) }, { soundOf[it] ?: it }, "+") }
}

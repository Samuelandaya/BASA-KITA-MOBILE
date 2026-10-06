package ph.basakita.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.data.syllables
import ph.basakita.app.data.tokenize
import ph.basakita.app.data.vocabFor
import ph.basakita.app.learnerLevel
import ph.basakita.app.levelNames
import ph.basakita.app.scoreReading
import ph.basakita.app.services.Speech

fun textSizeFor(size: String): Int = when (size) {
    "Normal" -> 24
    "Bigger" -> 36
    else -> 30
}

/**
 * A paragraph of tappable words, with one word lit up while it is being read.
 * Tap a word to open its vocabulary card.
 */
@Composable
fun WordWrap(
    text: String,
    lang: String,
    active: Int = -1,
    fontSize: Int = 30,
    highlight: Boolean = true,
    tappable: Boolean = true,
    focusWord: String? = null,
    said: Set<String> = emptySet(),
) {
    var vocabWord by remember { mutableStateOf<String?>(null) }
    val tokens = remember(text) { tokenize(text) }
    Wrap(spacing = 2.dp, runSpacing = 4.dp) {
        tokens.forEachIndexed { i, t ->
            val n = normalizeWord(t.word)
            val isSaid = said.contains(n)
            val isFocus = focusWord != null && n == normalizeWord(focusWord)
            val bg by animateColorAsState(
                when {
                    isSaid -> Color(0xFFDDF3E6)
                    highlight && i == active -> C.yellow
                    isFocus -> C.yellow
                    else -> Color.Transparent
                },
                label = "word",
            )
            Text(
                t.word,
                style = T.head(fontSize, color = if (isSaid) C.greenText else C.navy, height = 1.25),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .then(if (tappable) Modifier.clickable { vocabWord = t.word } else Modifier)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
    vocabWord?.let { VocabSheet(it, lang) { vocabWord = null } }
}

/** Text that the voice reads aloud, lighting up each word as it is spoken. */
@Composable
fun ReadAloudText(
    text: String,
    lang: String = "en-US",
    rate: Double = 1.0,
    showButton: Boolean = true,
    highlight: Boolean = true,
    fontSize: Int = 30,
    onDone: (() -> Unit)? = null,
) {
    val app = LocalApp.current
    var active by remember(text) { mutableIntStateOf(-1) }
    var playing by remember(text) { mutableStateOf(false) }
    val tokens = remember(text) { tokenize(text) }
    DisposableEffect(text) { onDispose { Speech.stop() } }

    fun play() {
        playing = true
        active = 0
        Speech.speak(
            text, lang, rate,
            onWord = { start, _ -> tokens.indexOfFirst { start >= it.start && start < it.end + 1 }.takeIf { it >= 0 }?.let { active = it } },
            onEnd = {
                playing = false
                active = -1
                onDone?.invoke()
            },
        )
    }

    fun stop() {
        Speech.stop()
        playing = false
        active = -1
    }

    Column {
        WordWrap(text, lang, active = active, fontSize = fontSize, highlight = highlight)
        if (showButton) {
            Spacer(Modifier.height(12.dp))
            Btn(
                if (playing) "Stop" else "Read it to me",
                icon = if (playing) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                kind = BtnKind.Blue,
                onClick = if (app.settings.narration) ({ if (playing) stop() else play() }) else null,
            )
        }
    }
}

/** The vocabulary card: word parts, meaning, and the word in the other language. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabSheet(word: String, lang: String, onDismiss: () -> Unit) {
    val clean = normalizeWord(word)
    val entry = vocabFor(clean)
    val fil = lang.startsWith("fil")
    val parts = syllables(clean, filipino = fil)
    LaunchedEffect(clean) { Speech.speak(clean, lang, .8) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .padding(12.dp)
                .navigationBarsPadding()
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(3.dp, C.yellow, RoundedCornerShape(32.dp))
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.width(44.dp).height(5.dp).background(C.cardBorder, RoundedCornerShape(3.dp)))
            Spacer(Modifier.height(14.dp))
            Text(clean, style = T.head(40, weight = W700))
            Spacer(Modifier.height(12.dp))
            Wrap(center = true) {
                for (p in parts) {
                    Text(
                        p,
                        style = T.head(30, weight = W700),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(3.dp, C.yellow, RoundedCornerShape(18.dp))
                            .clickable { Speech.speak(p, lang, .7) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            if (entry != null) {
                Text(entry.meaning, textAlign = TextAlign.Center, style = T.body(17, height = 1.35))
                Spacer(Modifier.height(8.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(T.body(15, color = C.muted).toSpanStyle()) { append(if (fil) "In English: " else "Sa Filipino: ") }
                        withStyle(T.body(17, color = C.navy, weight = W800).toSpanStyle()) { append(entry.pair) }
                    },
                )
            } else {
                Text("Tap the parts to hear them, then say the whole word!", textAlign = TextAlign.Center, style = T.body(15, color = C.muted))
            }
            Spacer(Modifier.height(16.dp))
            Btn("Hear it again", icon = Icons.AutoMirrored.Filled.VolumeUp, onClick = { Speech.speak(clean, lang, .8) })
        }
    }
}

/** Shows (and saves) the score after the child reads a page aloud. */
@Composable
fun ReadingScoreCard(words: List<String>, heard: Set<String>, confidence: Double?, lang: String, storyId: String, page: Int) {
    if (heard.isEmpty()) return
    val app = LocalApp.current
    val r = scoreReading(words, heard, confidence, lang)
    LaunchedEffect(storyId, page, r.score) { app.recordScore(storyId, page, r.score, r.english) }
    val missed = words.map(::normalizeWord).filter { it.isNotEmpty() && !heard.contains(it) }.toSet()
    val cheer = if (r.score >= 90) "Excellent!" else if (r.score >= 70) "Great job!" else "Keep practicing!"
    WhiteCard {
        Row {
            Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { r.score / 100f }, modifier = Modifier.size(76.dp), color = C.green, strokeWidth = 9.dp, trackColor = C.track)
                Text("${r.score}%", style = T.head(20, weight = W700))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (r.english) "Pronunciation score" else "Word reading accuracy", style = T.body(16, weight = W800, color = C.navy))
                Text("$cheer ${r.correct} of ${r.total} words read correctly.", style = T.body(13, color = C.muted))
                if (missed.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Wrap(spacing = 6.dp, runSpacing = 6.dp) {
                        Text("Practice:", style = T.body(13, color = C.muted))
                        for (w in missed) {
                            Text(
                                w,
                                style = T.body(14, weight = W800),
                                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(C.cream2).clickable { Speech.speak(w, lang, .75) }.padding(horizontal = 12.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Learner level with the steps to the next level. */
@Composable
fun LevelCard() {
    val level = learnerLevel(LocalApp.current.account)
    WhiteCard {
        Row {
            Box(
                Modifier.size(68.dp).background(levelBg(level.name), CircleShape).border(3.dp, C.yellow, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.WorkspacePremium, null, tint = levelFg(level.name), modifier = Modifier.size(34.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("My reading level", style = T.body(13, color = C.muted, weight = W800))
                Text("${level.name} Reader", style = T.head(24, weight = W700))
                Spacer(Modifier.height(6.dp))
                Wrap(spacing = 6.dp, runSpacing = 6.dp) {
                    levelNames.forEachIndexed { i, n ->
                        Tag(
                            n,
                            bg = if (i == level.index) C.orange else if (i < level.index) Color(0xFFDDF3E6) else Color(0xFFF1EDE4),
                            fg = if (i == level.index) C.ink else if (i < level.index) C.greenText else C.muted,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (level.next != null) {
                    Bar(level.toNext, color = C.green)
                    Spacer(Modifier.height(6.dp))
                    Text("To reach ${level.next}: ${level.goal.lowercase()}.", style = T.body(13, color = C.muted))
                } else {
                    Text("You’re a confident reader. ${level.goal}", style = T.body(13, color = C.muted))
                }
            }
        }
    }
}

/** Big round microphone button. */
@Composable
fun MicButton(listening: Boolean, label: String = "Tap to read aloud", onClick: () -> Unit) {
    val shape = RoundedCornerShape(40.dp)
    Row(
        Modifier
            .padding(bottom = 4.dp)
            .then(if (listening) Modifier.shadow(8.dp, shape, ambientColor = C.red, spotColor = C.red) else Modifier)
            .clip(shape)
            .background(if (listening) C.red else C.blueBtn)
            .clickable(onClick = onClick)
            .heightIn(min = 62.dp)
            .padding(horizontal = 26.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Mic, null, tint = Color.White, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(10.dp))
        Text(if (listening) "Listening… tap to stop" else label, style = T.body(16, color = Color.White, weight = W800))
    }
}

/** Builds a tile's look: white, rounded, thick yellow (or other) border. */
fun Modifier.tile(border: Color = C.yellow, radius: Int = 20): Modifier =
    this.clip(RoundedCornerShape(radius.dp)).background(Color.White).border(3.dp, border, RoundedCornerShape(radius.dp))

/** An inline style helper for mixing two text styles in one line. */
fun span(style: androidx.compose.ui.text.TextStyle): SpanStyle = style.toSpanStyle()

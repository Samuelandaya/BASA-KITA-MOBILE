package ph.basakita.app.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.basakita.app.data.Story
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.data.tokenize
import ph.basakita.app.data.wordsOf
import ph.basakita.app.services.MicCheck
import ph.basakita.app.services.Sounds
import ph.basakita.app.services.Speech
import ph.basakita.app.state.storyById
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Bar
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.MicButton
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ReadingScoreCard
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.Seg
import ph.basakita.app.ui.StoryCover
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.WordWrap
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.span
import ph.basakita.app.ui.textSizeFor
import ph.basakita.app.ui.tile

@Composable
private fun PageChips(story: Story, page: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Tag("${story.language} · ${story.shownLevel}", bg = C.cream2, fg = C.navy)
        Tag("Page ${page + 1} of ${story.pages.size}", bg = C.cream2, fg = C.navy)
    }
}

/** A microphone checker that is closed when the screen closes. */
@Composable
fun rememberMic(lang: String): MicCheck {
    val context = LocalContext.current
    val mic = remember(lang) { MicCheck(context, lang) }
    DisposableEffect(mic) { onDispose { mic.dispose() } }
    return mic
}

/** Marks the story as opened and stops the voice when the screen closes. */
@Composable
private fun OpenStory(id: String) {
    val app = LocalApp.current
    LaunchedEffect(id) { app.openStory(id) }
    DisposableEffect(id) { onDispose { Speech.stop() } }
}

@Composable
private fun PrevNext(page: Int, last: Boolean, go: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Btn("Previous", icon = Icons.AutoMirrored.Filled.ArrowBack, kind = BtnKind.White, onClick = if (page == 0) null else ({ go(-1) }))
        Btn(if (last) "Finish" else "Next", icon = if (last) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward, onClick = { go(1) })
    }
}

// ------------------------------------------------------------------ Listen
@Composable
fun ListenScreen(id: String) {
    val s = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    var page by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var active by remember { mutableIntStateOf(-1) }
    OpenStory(id)

    fun readPage(index: Int) {
        val tokens = tokenize(s.pages[index].text)
        page = index
        active = 0
        app.savePage(s.id, index + 1)
        Speech.speak(
            s.pages[index].text, s.lang, app.settings.speed,
            onWord = { start, _ -> tokens.indexOfFirst { start >= it.start && start < it.end + 1 }.takeIf { it >= 0 }?.let { active = it } },
            onEnd = {
                active = -1
                if (!playing) return@speak
                if (index + 1 < s.pages.size) {
                    scope.launch {
                        delay(700)
                        if (playing) readPage(index + 1)
                    }
                } else {
                    playing = false
                }
            },
        )
    }

    fun toggle() {
        if (playing) {
            playing = false
            active = -1
            Speech.stop()
        } else {
            playing = true
            readPage(page)
        }
    }

    fun go(delta: Int) {
        val next = page + delta
        if (next < 0) return
        if (next >= s.pages.size) {
            Speech.stop()
            nav.replace("books/${s.id}/complete")
            return
        }
        if (playing) {
            readPage(next)
        } else {
            Speech.stop()
            active = -1
            page = next
            app.savePage(s.id, next + 1)
        }
    }

    val last = page == s.pages.size - 1
    val coverScale by animateFloatAsState(if (playing) 1.02f else 1f, tween(2000), label = "cover")
    AppPage(back = true, title = s.title, subtitle = "Listen and follow each word as it lights up. Tap any word to learn it!") {
        Panel {
            PageChips(s, page)
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                page,
                transitionSpec = { (slideInHorizontally(tween(350)) { it / 4 } + fadeIn(tween(350))) togetherWith fadeOut(tween(200)) },
                label = "page",
            ) { p ->
                WhiteCard {
                    StoryCover(s, Modifier.scale(coverScale), height = 170.dp)
                    Spacer(Modifier.height(14.dp))
                    WordWrap(s.pages[p].text, s.lang, active = if (p == page) active else -1, fontSize = textSizeFor(app.settings.textSize), highlight = app.settings.highlight)
                }
            }
            if (!app.settings.narration) {
                Text("Story narration is off. Turn it on in Settings to hear the story.", style = T.body(14, color = C.red, weight = W800), modifier = Modifier.padding(top = 10.dp))
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Btn("", icon = Icons.AutoMirrored.Filled.ArrowBack, kind = BtnKind.White, onClick = if (page == 0) null else ({ go(-1) }))
                Btn(
                    if (playing) "Pause" else if (page == 0) "Play story" else "Play",
                    icon = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    kind = BtnKind.Blue, big = true,
                    onClick = if (app.settings.narration) ::toggle else null,
                )
                Btn(if (last) "Finish" else "", icon = if (last) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward, onClick = { go(1) })
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Speed:", style = T.body(14, weight = W800))
                Spacer(Modifier.width(10.dp))
                Seg(listOf(0.75, 1.0, 1.25), app.settings.speed, { "$it×" }) { v -> app.updateSettings { it.speed = v } }
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text("Try Read and Learn", style = T.body(14, color = C.blueBtn, weight = W800), modifier = Modifier.clickable { nav.replace("books/${s.id}/read") }.padding(8.dp))
            }
        }
    }
}

// ------------------------------------------------------------------ Read and Learn
private val marungkoSteps = listOf("Sounds", "Syllables", "Words", "Sentence")
private val phonicsSteps = listOf("Letter Sound", "Blend", "Words", "Sentence")

@Composable
private fun LetterTile(text: String, border: Color = C.yellow, onClick: () -> Unit) {
    Box(
        Modifier.defaultMinSize(70.dp, 70.dp).tile(border).clickable(onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = T.head(34, weight = W700)) }
}

@Composable
fun ReadAndLearnScreen(id: String) {
    val s = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    val mic = rememberMic(s.lang)
    var page by remember { mutableIntStateOf(0) }
    var step by remember { mutableIntStateOf(0) }
    OpenStory(id)

    fun say(t: String) = Speech.speak(t, s.lang, app.settings.speed.coerceIn(0.5, 1.0) * .85)

    fun go(delta: Int) {
        val next = page + delta
        if (next < 0) return
        if (next >= s.pages.size) {
            nav.replace("books/${s.id}/complete")
            return
        }
        mic.reset()
        page = next
        step = 0
        app.savePage(s.id, next + 1)
    }

    val phonics = !s.isFilipino
    val steps = if (phonics) phonicsSteps else marungkoSteps
    val sentence = s.pages[page].text
    val words = wordsOf(sentence)
    val focus = s.focus
    val correct = words.count { mic.heard.contains(normalizeWord(it)) }
    val allRead = correct == words.size

    AppPage(back = true, title = s.title, subtitle = "Read and Learn · Practice with ${if (phonics) "English Phonics" else "the Marungko Method"}") {
        Panel {
            ScrollSeg(listOf(0, 1, 2, 3), step, { "${it + 1} · ${steps[it]}" }) { step = it }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { Tag("Page ${page + 1} of ${s.pages.size}", bg = C.cream2, fg = C.navy) }
            Spacer(Modifier.height(8.dp))
            WhiteCard {
                Text("TODAY’S SENTENCE", style = T.body(12, color = C.muted, weight = W800))
                Spacer(Modifier.height(6.dp))
                WordWrap(sentence, s.lang, fontSize = 28, tappable = false, focusWord = s.pages[page].word, said = mic.heard)
            }
            Spacer(Modifier.height(12.dp))
            WhiteCard {
                when (step) {
                    0 -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(110.dp).tile(C.orange).clickable { say(focus.letter.takeLast(1)) }, contentAlignment = Alignment.Center) {
                            Text(focus.letter, style = T.head(56, weight = W700))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("The letter ${focus.letter.first()} says ${focus.sound}", style = T.head(20))
                            Spacer(Modifier.height(4.dp))
                            Text("as in ${focus.exampleFil} · ${focus.example}", style = T.body(14, color = C.muted))
                            Spacer(Modifier.height(8.dp))
                            Btn("Hear the words", icon = Icons.AutoMirrored.Filled.VolumeUp, kind = BtnKind.White, onClick = { say("${focus.exampleFil}. ${focus.example}") })
                        }
                    }
                    1 -> if (phonics) {
                        Text("Say each sound, then blend them together. Tap to hear!", style = T.head(18))
                        Spacer(Modifier.height(12.dp))
                        Wrap {
                            focus.parts.forEachIndexed { i, p ->
                                if (i > 0) Text("+", style = T.head(26, color = C.orange, weight = W700))
                                LetterTile(p) { say(p) }
                            }
                            Text("=", style = T.head(26, color = C.orange, weight = W700))
                            LetterTile(focus.example, C.orange) { say(focus.example) }
                        }
                    } else {
                        Text("Blend ${focus.letter.first()} with the vowels. Tap to hear!", style = T.head(18))
                        Spacer(Modifier.height(12.dp))
                        Wrap(spacing = 10.dp, runSpacing = 10.dp) { for (syl in focus.syllables) LetterTile(syl) { say(syl) } }
                    }
                    2 -> {
                        val groups = listOf(Triple("English", focus.parts, focus.example), Triple("Filipino", focus.partsFil, focus.exampleFil))
                        for ((label, parts, word) in if (phonics) groups else groups.reversed()) {
                            Text(label, style = T.body(14, color = C.muted, weight = W800))
                            Spacer(Modifier.height(6.dp))
                            Wrap { for (p in parts) LetterTile(p) { say(p) } }
                            Spacer(Modifier.height(6.dp))
                            Btn(word, icon = Icons.AutoMirrored.Filled.VolumeUp, kind = BtnKind.White, onClick = { say(word) })
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    else -> {
                        Text("Read the whole sentence aloud", style = T.head(18))
                        Spacer(Modifier.height(4.dp))
                        Text("Tap the microphone, then read slowly. Words you read correctly turn green.", style = T.body(14, color = C.muted))
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Btn("Hear it first", icon = Icons.AutoMirrored.Filled.VolumeUp, kind = BtnKind.White, onClick = { say(sentence) })
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (mic.heard.isNotEmpty()) (if (allRead) "Amazing! You read every word!" else "$correct of ${words.size} words read") else "",
                                style = T.body(14, weight = W800, color = if (allRead) C.greenText else C.navy),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        ReadingScoreCard(words, mic.heard, mic.confidence, s.lang, s.id, page + 1)
                    }
                }
            }
            if (mic.notice.isNotEmpty()) Text(mic.notice, style = T.body(14, color = C.red, weight = W800), modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MicButton(mic.listening) {
                    step = 3
                    scope.launch { mic.listen(words) }
                }
            }
            if ((mic.notice.isNotEmpty() || step == 3) && !mic.listening && !allRead) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Btn("I read it!", icon = Icons.Filled.Check, kind = BtnKind.White, onClick = { mic.markAll(words) }) }
            }
            Spacer(Modifier.height(8.dp))
            PrevNext(page, page == s.pages.size - 1, ::go)
        }
    }
}

// ------------------------------------------------------------------ Read With Me
@Composable
fun ReadWithMeScreen(id: String) {
    val s = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    val mic = rememberMic(s.lang)
    var page by remember { mutableIntStateOf(0) }
    var turn by remember { mutableStateOf(false) }
    var active by remember { mutableIntStateOf(-1) }
    OpenStory(id)

    fun readToMe() {
        val tokens = tokenize(s.pages[page].text)
        mic.reset()
        turn = false
        active = 0
        Speech.speak(
            s.pages[page].text, s.lang, app.settings.speed.coerceIn(0.5, 1.0) * .9,
            onWord = { start, _ -> tokens.indexOfFirst { start >= it.start && start < it.end + 1 }.takeIf { it >= 0 }?.let { active = it } },
            onEnd = {
                active = -1
                turn = true
            },
        )
    }

    fun go(delta: Int) {
        val next = page + delta
        if (next < 0) return
        if (next >= s.pages.size) {
            nav.replace("books/${s.id}/complete")
            return
        }
        Speech.stop()
        mic.reset()
        page = next
        turn = false
        active = -1
        app.savePage(s.id, next + 1)
    }

    val words = wordsOf(s.pages[page].text)
    AppPage(back = true, title = s.title, subtitle = "Read With Me · I read a page, then it’s your turn!") {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Seg(listOf(false, true), turn, { if (it) "2 · You read" else "1 · I read" }) {
                    Speech.stop()
                    turn = it
                    active = -1
                }
                Spacer(Modifier.weight(1f))
                Tag("${page + 1}/${s.pages.size}", bg = C.cream2, fg = C.navy)
            }
            Spacer(Modifier.height(12.dp))
            WhiteCard {
                StoryCover(s, height = 150.dp, radius = 20.dp)
                Spacer(Modifier.height(12.dp))
                WordWrap(s.pages[page].text, s.lang, active = active, fontSize = textSizeFor(app.settings.textSize), highlight = app.settings.highlight, said = mic.heard)
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!turn) {
                    Btn("Read it to me", icon = Icons.AutoMirrored.Filled.VolumeUp, big = true, onClick = if (app.settings.narration) ::readToMe else null)
                } else {
                    MicButton(mic.listening, "My turn!") { scope.launch { mic.listen(words) } }
                    if (mic.notice.isNotEmpty()) Text(mic.notice, style = T.body(14, color = C.red, weight = W800), modifier = Modifier.padding(top = 8.dp))
                    if (mic.heard.isEmpty() && !mic.listening) Btn("I read it!", icon = Icons.Filled.Check, kind = BtnKind.White, onClick = { mic.markAll(words) })
                }
            }
            Spacer(Modifier.height(10.dp))
            ReadingScoreCard(words, mic.heard, mic.confidence, s.lang, s.id, page + 1)
            Spacer(Modifier.height(12.dp))
            PrevNext(page, page == s.pages.size - 1, ::go)
        }
    }
}

// ------------------------------------------------------------------ Normal reading
@Composable
fun NormalReadingScreen(id: String) {
    val s = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    var page by remember {
        val p = app.progress[s.id]
        mutableIntStateOf(if (p == null || p.finished) 0 else (p.page - 1).coerceIn(0, s.pages.size - 1))
    }
    LaunchedEffect(s.id) { app.savePage(s.id, page + 1) }

    fun go(d: Int) {
        val next = page + d
        if (next < 0) return
        if (next >= s.pages.size) {
            nav.replace("books/${s.id}/complete")
            return
        }
        page = next
        app.savePage(s.id, next + 1)
    }

    AppPage(back = true, title = s.title, subtitle = "Read on your own. Tap any word to learn it!") {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Bar(((page + 1) * 100f / s.pages.size).toInt(), color = C.green, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Text("Page ${page + 1} of ${s.pages.size}", style = T.body(13, weight = W800, color = C.muted))
            }
            Spacer(Modifier.height(14.dp))
            WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
                StoryCover(s, height = 190.dp)
                Spacer(Modifier.height(16.dp))
                WordWrap(s.pages[page].text, s.lang, fontSize = textSizeFor(app.settings.textSize))
            }
            Spacer(Modifier.height(16.dp))
            PrevNext(page, page == s.pages.size - 1, ::go)
        }
    }
}

// ------------------------------------------------------------------ Story complete + quiz
@Composable
fun CompleteScreen(id: String) {
    val s = storyById(id) ?: return MissingStory()
    val app = LocalApp.current
    val nav = LocalNav.current
    var q by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var firstTry by remember { mutableStateOf(true) }
    LaunchedEffect(s.id) {
        app.finishStory(s.id, 3)
        Sounds.play("star")
    }

    fun pick(i: Int) {
        val quiz = s.questions[q]
        if (picked != null && quiz.options[picked!!].correct) return
        picked = i
        val ok = quiz.options[i].correct
        Sounds.play(if (ok) "good" else "oops")
        if (ok && firstTry) score++
        if (!ok) firstTry = false
    }

    fun next() {
        q++
        picked = null
        firstTry = true
        if (q >= s.questions.size) app.recordQuiz(s.id, score, s.questions.size)
    }

    val words = s.pages.sumOf { wordsOf(it.text).size }
    val done = q >= s.questions.size
    val quiz = if (done) null else s.questions[q]
    val right = quiz != null && picked != null && quiz.options[picked!!].correct
    AppPage {
        Spacer(Modifier.height(46.dp))
        WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(22.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Icon(Icons.Filled.Star, null, tint = C.yellow2, modifier = Modifier.size(48.dp))
                    Icon(Icons.Filled.Star, null, tint = C.yellow2, modifier = Modifier.size(64.dp))
                    Icon(Icons.Filled.Star, null, tint = C.yellow2, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Great job, ${app.displayName}!", textAlign = TextAlign.Center, style = T.fun_(40))
                Spacer(Modifier.height(6.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(span(T.body(16, color = C.muted))) { append("You finished ") }
                        withStyle(span(T.body(16, color = C.navy, weight = W800))) { append(s.title) }
                        withStyle(span(T.body(16, color = C.muted))) { append(".") }
                    },
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row {
                    for ((v, l) in listOf("${s.pages.size}/${s.pages.size}" to "Pages read", "$words" to "Words read", "${s.minutes} min" to "Reading time")) {
                        Column(
                            Modifier.weight(1f).padding(horizontal = 4.dp).background(C.cream, RoundedCornerShape(16.dp)).padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(v, style = T.head(20, weight = W700))
                            Text(l, style = T.body(11, color = C.muted, weight = W800))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth().background(C.paleBlue, RoundedCornerShape(20.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(50.dp).background(C.blueBtn, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.WorkspacePremium, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("New badge: ${s.theme} Star", style = T.head(16))
                        Text("For finishing “${s.title}”", style = T.body(13, color = C.muted))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = C.cardBorder)
                if (quiz == null) {
                    Text("You answered $score of ${s.questions.size} questions on the first try!", textAlign = TextAlign.Center, style = T.head(19))
                    Spacer(Modifier.height(6.dp))
                    Text(if (score == s.questions.size) "You really understood the story!" else "Read it again to remember more.", style = T.body(14, color = C.muted))
                } else {
                    Tag("Question ${q + 1} of ${s.questions.size}", bg = C.cream2, fg = C.navy)
                    Spacer(Modifier.height(8.dp))
                    Text(quiz.question, textAlign = TextAlign.Center, style = T.head(20))
                    Spacer(Modifier.height(12.dp))
                    Wrap(center = true, spacing = 10.dp, runSpacing = 10.dp, modifier = Modifier.fillMaxWidth()) {
                        quiz.options.forEachIndexed { i, o ->
                            val chosen = picked == i
                            Column(
                                Modifier
                                    .width(104.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (chosen) (if (o.correct) C.paleGreen else Color(0xFFFDEEEA)) else Color.White)
                                    .border(3.dp, if (chosen) (if (o.correct) C.green else Color(0xFFE76F51)) else C.cardBorder, RoundedCornerShape(20.dp))
                                    .clickable { pick(i) }
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(Modifier.size(56.dp).background(Color(o.color), CircleShape))
                                Spacer(Modifier.height(8.dp))
                                Text(o.label, textAlign = TextAlign.Center, style = T.head(14))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (picked == null) "" else if (right) "Yes! That’s right!" else "Not quite. Try again!",
                        style = T.body(15, weight = W800, color = if (right) C.greenText else Color(0xFFB5442A)),
                    )
                    if (right) Btn(if (q + 1 < s.questions.size) "Next question" else "See my score", icon = Icons.AutoMirrored.Filled.ArrowForward, onClick = ::next)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Wrap(center = true, spacing = 10.dp, runSpacing = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Btn("Read again", kind = BtnKind.White, onClick = { nav.go("books/${s.id}") })
            Btn("Next story", kind = BtnKind.Blue, onClick = { nav.go("books") })
            Btn("Back to Home", onClick = { nav.go("home") })
        }
    }
}

package ph.basakita.app.screens

import android.Manifest
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.rememberModelInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.basakita.app.data.decks
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.services.Permissions
import ph.basakita.app.services.Sounds
import ph.basakita.app.services.Speech
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.MicButton
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ScrollSeg
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.tile

private val cardSteps = listOf("Sound", "Syllables", "Word", "Say it!")

/** A flashcard's 3D object that turns slowly by itself and can be turned with a finger. */
@Composable
fun Model3D(card: String, label: String, modifier: Modifier = Modifier) {
    val spin = rememberInfiniteTransition(label = "spin")
    val angle by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    SceneView(
        modifier = modifier.semantics { contentDescription = "3D model of $label" },
        surfaceType = SurfaceType.TextureSurface,
        isOpaque = false,
    ) {
        rememberModelInstance(modelLoader, "models/$card.glb")?.let { instance ->
            ModelNode(modelInstance = instance, scaleToUnits = 2.0f, centerOrigin = Position(0f, 0f, 0f), rotation = Rotation(y = angle))
        }
    }
}

/**
 * Marungko flashcards. Each card has a 3D object you can turn with your finger,
 * and an AR button that puts the object in the room through the phone's camera.
 */
@Composable
fun FlashcardsScreen(startCard: String?) {
    val app = LocalApp.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    val mic = rememberMic("fil-PH")
    val start = remember(startCard) {
        decks.withIndex().firstNotNullOfOrNull { (d, deck) -> deck.cards.indexOfFirst { it.fil == startCard }.takeIf { it >= 0 }?.let { d to it } } ?: (0 to 0)
    }
    var deckIdx by remember(startCard) { mutableIntStateOf(start.first) }
    var cardIdx by remember(startCard) { mutableIntStateOf(start.second) }
    var step by remember { mutableIntStateOf(0) }
    var stars by remember { mutableStateOf(setOf<String>()) }

    val unlocked = app.lessonsDone + 1 // every learned sound plus the one being learned now
    val deck = decks[deckIdx]
    val card = deck.cards[cardIdx]
    val key = "${deck.letter}-${card.fil}"
    val sound = deck.letter.takeLast(1)
    val saidIt = mic.heard.contains(normalizeWord(card.fil))
    LaunchedEffect(saidIt, key) {
        if (saidIt && key !in stars) {
            stars = stars + key
            Sounds.play("good")
        }
    }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { Speech.stop() } }

    fun say(t: String, rate: Double = .8) = Speech.speak(t, "fil-PH", rate)
    fun goStep(i: Int) {
        step = i
        mic.reset()
        if (i == 0) say(sound, .7)
        if (i == 2) say(card.fil)
    }
    fun pickCard(i: Int) {
        mic.reset()
        cardIdx = i
        step = 0
    }
    fun pickDeck(i: Int) {
        mic.reset()
        deckIdx = i
        cardIdx = 0
        step = 0
    }

    AppPage(banner = "Flashcards") {
        Panel {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                decks.forEachIndexed { i, d ->
                    val locked = i >= unlocked
                    Box(
                        Modifier
                            .size(width = 56.dp, height = 52.dp)
                            .shadow(3.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (locked) C.locked else if (i == deckIdx) C.orange else Color.White)
                            .clickable(enabled = !locked) { pickDeck(i) }
                            .semantics { contentDescription = if (locked) "${d.letter} deck, locked. Learn the sound first." else "${d.letter} deck" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (locked) Icon(Icons.Filled.Lock, null, tint = C.muted, modifier = Modifier.size(18.dp)) else Text(d.letter, style = T.head(20, weight = W700))
                    }
                }
            }
            if (unlocked <= decks.size) {
                Text("Practice more sounds to unlock decks", style = T.body(13, color = C.blueBtn, weight = W800), modifier = Modifier.padding(top = 6.dp).clickable { nav.go("learn") })
            }
            Spacer(Modifier.height(10.dp))
            ScrollSeg(listOf(0, 1, 2, 3), step, { "${it + 1} · ${cardSteps[it]}" }, ::goStep)
            Spacer(Modifier.height(12.dp))
            WhiteCard(border = C.yellow, padding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 16.dp)) {
                Box(Modifier.fillMaxWidth().height(260.dp)) {
                    key(card.fil) { Model3D(card.fil, card.eng, Modifier.fillMaxSize()) }
                    IconButton(
                        onClick = { nav.push("ar/${card.fil}") },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).shadow(3.dp, CircleShape).background(Color.White, CircleShape),
                    ) { Icon(Icons.Filled.ViewInAr, "See ${card.eng} in your room (AR)", tint = C.ink) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ViewInAr, null, tint = C.blueBtn, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Turn it with your finger. Tap the AR button to see it in your room.", textAlign = TextAlign.Center, style = T.body(12, color = C.muted, weight = W700))
                }
                Spacer(Modifier.height(12.dp))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    when (step) {
                        0 -> {
                            Box(
                                Modifier.size(width = 110.dp, height = 90.dp).tile(C.orange, radius = 24).clickable { say(sound, .7) },
                                contentAlignment = Alignment.Center,
                            ) { Text(deck.letter, style = T.head(48, weight = W700)) }
                            Spacer(Modifier.height(6.dp))
                            Text("The letter ${sound.uppercase()} says /$sound/", style = T.body(15, color = C.orangeText, weight = W800))
                        }
                        1 -> {
                            Wrap(center = true, spacing = 10.dp, runSpacing = 10.dp, modifier = Modifier.fillMaxWidth()) {
                                for (s in card.syl) {
                                    Box(Modifier.defaultMinSize(70.dp, 66.dp).tile(radius = 18).clickable { say(s, .7) }.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                        Text(s, style = T.head(32, weight = W700))
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Tap each part to hear it, then blend them!", style = T.body(14, color = C.muted))
                        }
                        2 -> {
                            Text(card.fil, style = T.head(44, weight = W700), modifier = Modifier.clickable { say(card.fil) })
                            Text(card.syl.joinToString(" · "), style = T.body(15, color = C.muted, weight = W800))
                            Text(card.eng, style = T.body(14, color = C.muted))
                        }
                        else -> {
                            Text(card.fil, style = T.head(44, weight = W700, color = if (saidIt) C.green else C.navy))
                            Text(
                                if (saidIt) "Great job! You said it!" else if (mic.listening) "Listening… say the word now!" else "Tap the microphone and say the word.",
                                style = T.body(14, color = C.muted, weight = W800),
                            )
                            Spacer(Modifier.height(10.dp))
                            MicButton(mic.listening, "Say it!") { scope.launch { mic.listen(listOf(card.fil)) } }
                            if (mic.notice.isNotEmpty()) Text(mic.notice, textAlign = TextAlign.Center, style = T.body(13, color = C.red, weight = W800))
                            if (!saidIt) Btn("I said it!", icon = Icons.Filled.Check, kind = BtnKind.White, onClick = { mic.markAll(listOf(card.fil)) })
                            if (saidIt && cardIdx < deck.cards.size - 1) Btn("Next card", icon = Icons.AutoMirrored.Filled.ArrowForward, onClick = { pickCard(cardIdx + 1) })
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Deck ${deck.letter}", style = T.head(18))
            Spacer(Modifier.height(8.dp))
            deck.cards.forEachIndexed { i, c ->
                Row(
                    Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (i == cardIdx) C.paleBlue else Color.White)
                        .border(2.dp, if (i == cardIdx) C.blueBtn else C.cardBorder, RoundedCornerShape(18.dp))
                        .clickable { pickCard(i) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(28.dp).background(Color(c.color), CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.fil, style = T.body(16, weight = W800))
                        Text(c.eng, style = T.body(12, color = C.muted))
                    }
                    if ("${deck.letter}-${c.fil}" in stars) Icon(Icons.Filled.Star, "Said", tint = C.yellow2, modifier = Modifier.size(22.dp))
                }
            }
            if (step < 3) Btn("Next step", icon = Icons.AutoMirrored.Filled.ArrowForward, expand = true, onClick = { goStep(step + 1) })
            Spacer(Modifier.height(4.dp))
            Btn("Scan a flashcard", icon = Icons.Filled.PhotoCamera, kind = BtnKind.Blue, expand = true, onClick = { nav.push("scan-card") })
        }
    }
}

/**
 * AR: shows the flashcard's object in the room through the camera (ARCore).
 * The object appears on the first floor or table the phone finds.
 */
@Composable
fun ArScreen(cardWord: String) {
    val nav = LocalNav.current
    val context = LocalContext.current
    val card = decks.flatMap { it.cards }.firstOrNull { it.fil == cardWord }
    var status by remember { mutableStateOf("checking") } // checking | ready | no-camera | unsupported
    var anchor by remember { mutableStateOf<Anchor?>(null) }

    LaunchedEffect(Unit) {
        var availability = ArCoreApk.getInstance().checkAvailability(context)
        while (availability.isTransient) {
            delay(200)
            availability = ArCoreApk.getInstance().checkAvailability(context)
        }
        status = when {
            !availability.isSupported -> "unsupported"
            !Permissions.request(context, Manifest.permission.CAMERA) -> "no-camera"
            else -> "ready"
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (status == "ready" && card != null) {
            ARSceneView(
                modifier = Modifier.fillMaxSize(),
                planeRenderer = true,
                onSessionUpdated = { _, frame ->
                    if (anchor == null) {
                        anchor = frame.getUpdatedPlanes()
                            .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                            ?.let { it.createAnchorOrNull(it.centerPose) }
                    }
                },
            ) {
                anchor?.let { a ->
                    AnchorNode(anchor = a) {
                        rememberModelInstance(modelLoader, "models/${card.fil}.glb")?.let { ModelNode(modelInstance = it, scaleToUnits = 0.3f) }
                    }
                }
            }
        }
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp)) {
            IconButton(onClick = { nav.back() }, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = C.ink)
            }
            Spacer(Modifier.weight(1f))
            val message = when {
                card == null -> "This flashcard wasn’t found."
                status == "checking" -> "Getting the camera ready…"
                status == "unsupported" -> "This phone can’t show AR. You can still turn the 3D ${card.eng} on the Flashcards page."
                status == "no-camera" -> "Please allow the camera to see the ${card.eng} in your room."
                anchor == null -> "Point the camera at the floor or a table and move the phone slowly."
                else -> "Here is the ${card.eng}! Say it: ${card.fil}"
            }
            Text(
                message,
                textAlign = TextAlign.Center,
                style = T.body(16, weight = W800, color = C.navy),
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(16.dp),
            )
        }
    }
}

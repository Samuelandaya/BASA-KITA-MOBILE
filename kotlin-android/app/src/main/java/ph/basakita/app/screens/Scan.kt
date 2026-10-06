package ph.basakita.app.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import ph.basakita.app.data.FlashCard
import ph.basakita.app.services.BookMatch
import ph.basakita.app.services.Permissions
import ph.basakita.app.services.recognizeBook
import ph.basakita.app.services.recognizeFlashCard
import ph.basakita.app.services.recognizeText
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ReadAloudText
import ph.basakita.app.ui.T
import ph.basakita.app.ui.W700
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import java.io.File

/** A new file for the camera to save a photo into. */
fun newPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "scans").apply { mkdirs() }
    val file = File(dir, "scan-${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
}

/**
 * Scan a storybook page, or (with [cardMode]) a flashcard on its own page, with the phone's camera.
 * The words are read on the phone (Google ML Kit), matched to the library, and read aloud.
 */
@Composable
fun ScanScreen(cardMode: Boolean) {
    val app = LocalApp.current
    val nav = LocalNav.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<Uri?>(null) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }
    var reading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var match by remember { mutableStateOf<BookMatch?>(null) }
    var cardMatch by remember { mutableStateOf<FlashCard?>(null) }
    var done by remember { mutableStateOf(false) }

    fun reset() {
        text = ""
        match = null
        cardMatch = null
        done = false
        error = ""
    }

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            reset()
            image = pendingPhoto
        }
    }
    val choosePicture = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            reset()
            image = uri
        }
    }

    fun camera() = scope.launch {
        if (!Permissions.request(context, Manifest.permission.CAMERA)) {
            error = "Please allow the camera, or choose a picture of the ${if (cardMode) "card" else "page"} instead."
            return@launch
        }
        try {
            val uri = newPhotoUri(context)
            pendingPhoto = uri
            takePhoto.launch(uri)
        } catch (_: Exception) {
            error = "Please allow the camera, or choose a picture of the page instead."
        }
    }

    fun readPage() {
        val uri = image ?: return
        reading = true
        error = ""
        scope.launch {
            try {
                val t = recognizeText(context, uri)
                if (t.isEmpty()) {
                    error = "I couldn’t find any words in this picture. Try a brighter, closer photo."
                } else {
                    text = t
                    if (cardMode) cardMatch = recognizeFlashCard(t) else match = recognizeBook(t)
                    done = true
                }
            } catch (e: Exception) {
                error = "I couldn’t read that picture. Try another one."
            } finally {
                reading = false
            }
        }
    }

    val lang = match?.story?.lang ?: if (app.settings.language == "Filipino") "fil-PH" else "en-US"
    AppPage(back = true, banner = if (cardMode) "Scan a Flashcard" else "Scan a Book") {
        Text(
            if (cardMode) "Show me one of your flashcards and I’ll find it for you!" else "Show me a page from your storybook and I’ll read it with you!",
            style = T.body(15, color = C.muted),
        )
        Spacer(Modifier.height(12.dp))
        Panel {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFFBEF))
                    .border(3.dp, C.yellow2, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (image != null) {
                    AsyncImage(image, "The picture you added", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = Color(0xFF6B7A89), modifier = Modifier.size(54.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(if (cardMode) "Point the camera at a flashcard" else "Take a picture of the page", style = T.body(16, weight = W800, color = C.muted))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Wrap(spacing = 10.dp, runSpacing = 6.dp) {
                Btn("Take photo", icon = Icons.Filled.PhotoCamera, kind = BtnKind.Blue, onClick = { camera() })
                Btn("Choose picture", icon = Icons.Outlined.PhotoLibrary, kind = BtnKind.White, onClick = {
                    choosePicture.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                })
                if (image != null) Btn("Remove", kind = BtnKind.White, onClick = { image = null; reset() })
            }
            if (image != null) {
                Spacer(Modifier.height(8.dp))
                Btn(
                    if (reading) "Reading…" else if (cardMode) "Find this card" else "Read this page",
                    icon = Icons.AutoMirrored.Filled.MenuBook, expand = true, big = true,
                    onClick = if (reading) null else ::readPage,
                )
            }
            if (reading) LinearProgressIndicator(Modifier.padding(top = 8.dp).fillMaxWidth().height(8.dp), color = C.orange)
            if (error.isNotEmpty()) Text(error, style = T.body(14, color = C.red, weight = W800), modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(14.dp))
        if (!done) {
            WhiteCard {
                Text("How it works", style = T.head(18))
                Spacer(Modifier.height(6.dp))
                val steps = if (cardMode) {
                    listOf("Take or choose a picture of one flashcard.", "Tap Find this card.", "I read the word, find the card and open it for you.")
                } else {
                    listOf("Take or choose a picture of one page.", "Tap Read this page.", "I read the words, find the book and read it to you.")
                }
                for (l in steps) Text("•  $l", style = T.body(14, color = C.muted), modifier = Modifier.padding(bottom = 4.dp))
            }
        } else {
            val found = if (cardMode) cardMatch != null else match != null
            if (found) {
                WhiteCard(color = C.paleGreen, border = C.green) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = C.green, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(12.dp))
                        if (cardMode) {
                            val c = cardMatch!!
                            Text("Card found! Open it to see it in AR.", style = T.head(17), modifier = Modifier.weight(1f))
                            Btn("Open card", onClick = { nav.go("cards?card=${c.fil}") })
                        } else {
                            val m = match!!
                            Column(Modifier.weight(1f)) {
                                Text("Book found!", style = T.body(13, color = C.greenText, weight = W800))
                                Text("${m.story.title} · page ${m.page + 1}", style = T.head(18))
                            }
                            Btn("Open this book", onClick = { nav.push("books/${m.story.id}") })
                        }
                    }
                }
            } else {
                WhiteCard {
                    Text(
                        if (cardMode) "I could not find a flashcard in this picture. Try a closer photo of the card." else "This page is not in my library yet, but I can still read it to you.",
                        style = T.body(14, color = C.muted, weight = W700),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            WhiteCard { ReadAloudText(text.replace(Regex("\\s*\\n\\s*"), " "), lang, app.settings.speed, fontSize = 24) }
        }
    }
}

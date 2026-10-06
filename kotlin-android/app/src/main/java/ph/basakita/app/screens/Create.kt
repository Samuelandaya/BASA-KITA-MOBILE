package ph.basakita.app.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ph.basakita.app.state.Draft
import ph.basakita.app.ui.AppPage
import ph.basakita.app.ui.Btn
import ph.basakita.app.ui.BtnKind
import ph.basakita.app.ui.C
import ph.basakita.app.ui.Field
import ph.basakita.app.ui.LocalApp
import ph.basakita.app.ui.LocalNav
import ph.basakita.app.ui.Panel
import ph.basakita.app.ui.ReadAloudText
import ph.basakita.app.ui.Seg
import ph.basakita.app.ui.T
import ph.basakita.app.ui.Tag
import ph.basakita.app.ui.W800
import ph.basakita.app.ui.WhiteCard
import ph.basakita.app.ui.Wrap
import ph.basakita.app.ui.textSizeFor
import java.io.ByteArrayOutputStream

private val characters = listOf("Mia", "Duck", "Puppy", "Cat")
private val places = listOf("Park", "School", "Home", "Farm")

/** A picture shrunk to 640 px wide and saved as a small base64 JPEG, so saved stories stay small. */
private fun shrink(context: android.content.Context, uri: Uri): String? = try {
    val src = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return null
    val scale = minOf(1f, 640f / src.width)
    val bmp = if (scale < 1f) Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true) else src
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 75, out)
    Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
} catch (_: Exception) {
    null
}

@Composable
fun Base64Image(b64: String, description: String, height: Int) {
    val bmp = remember(b64) {
        try {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
    if (bmp != null) Image(bmp, description, Modifier.height(height.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
}

@Composable
private fun Choice(label: String, on: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = T.body(14, weight = W800),
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (on) C.paleBlue else Color.White)
            .border(2.dp, if (on) C.blueBtn else C.cardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
    )
}

/**
 * Write a story page by page, add a picture to each page, and save it to My Stories.
 * Opening Create always starts a new story; `create?story=<id>` edits a saved one.
 */
@Composable
fun CreateScreen(editId: String?) {
    val app = LocalApp.current
    val context = LocalContext.current
    val existing = remember(editId) { app.drafts.firstOrNull { it.id == editId } }
    val draftId = remember { existing?.id ?: "draft-${System.currentTimeMillis()}" }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var character by remember { mutableStateOf(existing?.character ?: "Cat") }
    var place by remember { mutableStateOf(existing?.place ?: "Home") }
    var language by remember { mutableStateOf(existing?.language ?: "Filipino") }
    val pages = remember { mutableStateListOf(*(existing?.texts ?: listOf("", "", "")).toTypedArray()) }
    val images = remember { mutableStateListOf(*(existing?.images ?: emptyList()).toTypedArray()) }
    var current by remember { mutableIntStateOf(0) }
    var saved by remember { mutableStateOf("") }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val b64 = uri?.let { shrink(context, it) } ?: return@rememberLauncherForActivityResult
        while (images.size <= current) images.add("")
        images[current] = b64
        saved = ""
    }

    fun save() {
        while (images.size < pages.size) images.add("")
        app.saveDraft(
            Draft(
                id = draftId,
                title = title.trim().ifEmpty { "My Story" },
                language = language,
                character = character,
                place = place,
                texts = pages.toList(),
                images = images.toList(),
            ),
        )
        saved = "Saved! Find it in My Stories."
    }

    val img = images.getOrElse(current) { "" }
    AppPage(back = true, title = "Create Your Own Story", subtitle = "Pick a friend, pick a place, and write your story one page at a time.") {
        Panel {
            WhiteCard {
                Text("Story title", style = T.body(14, weight = W800))
                Spacer(Modifier.height(6.dp))
                Field(title, { title = it; saved = "" }, hint = "My Story")
                Spacer(Modifier.height(14.dp))
                Text("Main character", style = T.body(14, weight = W800))
                Spacer(Modifier.height(6.dp))
                Wrap { for (c in characters) Choice(c, character == c) { character = c } }
                Spacer(Modifier.height(14.dp))
                Text("Place", style = T.body(14, weight = W800))
                Spacer(Modifier.height(6.dp))
                Wrap { for (p in places) Choice(p, place == p) { place = p } }
                Spacer(Modifier.height(14.dp))
                Text("Language", style = T.body(14, weight = W800))
                Spacer(Modifier.height(6.dp))
                Seg(listOf("Filipino", "English", "Bilingual"), language, { it }) { language = it }
            }
            Spacer(Modifier.height(14.dp))
            WhiteCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Page ${current + 1}", style = T.head(20), modifier = Modifier.weight(1f))
                    Text("${pages.size} pages", style = T.body(13, color = C.muted, weight = W800))
                }
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFFBEF))
                        .border(3.dp, C.yellow2, RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (img.isNotEmpty()) {
                        Base64Image(img, "Picture for page ${current + 1}", 180)
                    } else {
                        Icon(Icons.Outlined.Image, null, tint = Color(0xFF6B7A89), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("Choose a picture for $character at the ${place.lowercase()}", textAlign = TextAlign.Center, style = T.body(14, weight = W800, color = C.muted))
                    }
                    Spacer(Modifier.height(8.dp))
                    Wrap(center = true) {
                        Btn(
                            if (img.isEmpty()) "Add picture" else "Change picture",
                            icon = Icons.Outlined.AddPhotoAlternate, kind = BtnKind.White,
                            onClick = { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        )
                        if (img.isNotEmpty()) Btn("Remove", kind = BtnKind.White, onClick = { images[current] = "" })
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("What happens on this page?", style = T.body(14, weight = W800))
                Spacer(Modifier.height(6.dp))
                Field(pages[current], { pages[current] = it; saved = "" }, hint = "$character goes to the ${place.lowercase()}...", lines = 4)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in pages.indices) {
                        Box(
                            Modifier
                                .size(width = 64.dp, height = 54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(2.dp, if (current == i) C.orange else C.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { current = i },
                            contentAlignment = Alignment.Center,
                        ) { Text("${i + 1}", style = T.body(16, weight = W800, color = C.navy)) }
                    }
                    Box(
                        Modifier
                            .size(width = 64.dp, height = 54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(2.dp, C.cardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                pages.add("")
                                current = pages.size - 1
                            },
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Add, "Add a page", tint = C.muted) }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (saved.isNotEmpty()) Text(saved, style = T.body(14, color = C.greenText, weight = W800), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            Btn("Save story", icon = Icons.Filled.Check, expand = true, big = true, onClick = ::save)
        }
    }
}

/** Reads a story the learner wrote, with narration and highlighted words. */
@Composable
fun MadeStoryScreen(id: String) {
    val app = LocalApp.current
    val nav = LocalNav.current
    var page by remember { mutableIntStateOf(0) }
    val draft = app.drafts.firstOrNull { it.id == id }
    if (draft == null) {
        AppPage(back = true, title = "Story not found") { Btn("Back to My Stories", onClick = { nav.back() }) }
        return
    }
    // Keep each page's picture with its text, and skip pages with no words.
    val slides = draft.texts.mapIndexedNotNull { i, t -> if (t.isBlank()) null else t.trim() to draft.images.getOrElse(i) { "" } }
    val pages = slides.ifEmpty { listOf("This story has no words yet. Edit it to add some!" to "") }
    val (text, image) = pages[page.coerceIn(0, pages.size - 1)]
    val lang = if (draft.language == "English") "en-US" else "fil-PH"
    val last = page >= pages.size - 1
    AppPage(back = true, title = draft.title, subtitle = "A story you made · ${draft.language}") {
        Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tag("${draft.character} · ${draft.place}", bg = C.cream2, fg = C.navy)
                Tag("Page ${page + 1} of ${pages.size}", bg = C.cream2, fg = C.navy)
            }
            Spacer(Modifier.height(12.dp))
            WhiteCard(padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
                if (image.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) { Base64Image(image, "Picture for page ${page + 1}", 200) }
                }
                ReadAloudText(text, lang, app.settings.speed, fontSize = textSizeFor(app.settings.textSize), highlight = app.settings.highlight)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Btn("", icon = Icons.AutoMirrored.Filled.ArrowBack, kind = BtnKind.White, onClick = if (page == 0) null else ({ page-- }))
                Btn("Edit story", icon = Icons.Filled.Edit, kind = BtnKind.White, onClick = { nav.push("create?story=${draft.id}") })
                if (last) Btn("Finish ✓", onClick = { nav.back() }) else Btn("▶", onClick = { page++ })
            }
        }
    }
}

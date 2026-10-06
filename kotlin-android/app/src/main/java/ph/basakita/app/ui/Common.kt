package ph.basakita.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ph.basakita.app.data.Story
import ph.basakita.app.state.percentRead
import kotlin.math.PI
import kotlin.math.sin

/** An image bundled with the app, e.g. "images/logo.png" or "covers/lost-puppy.svg". */
fun asset(path: String) = "file:///android_asset/$path"

/** Yellow and green waves at the top, green and blue at the bottom (like the website). */
@Composable
fun Waves(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        fun wave(y: Float, amp: Float, phase: Float, top: Boolean = true): Path = Path().apply {
            moveTo(0f, if (top) 0f else size.height)
            lineTo(0f, y)
            var x = 0f
            while (x <= size.width) {
                lineTo(x, y + sin(x / size.width * PI.toFloat() * 2 + phase) * amp)
                x += 6f
            }
            lineTo(size.width, if (top) 0f else size.height)
            close()
        }
        val d = density
        drawPath(wave(34 * d, 10 * d, 0.3f), C.yellow)
        drawPath(wave(18 * d, 8 * d, 2.2f), C.green)
        drawPath(wave(size.height - 26 * d, 9 * d, 1.0f, top = false), C.green)
        drawPath(wave(size.height - 12 * d, 7 * d, 3.0f, top = false), C.blue)
    }
}

/** The page frame: cream background, waves, an optional banner or title, and the body. */
@Composable
fun AppPage(
    title: String? = null,
    subtitle: String? = null,
    banner: String? = null,
    back: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().background(C.cream)) {
        Waves(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        ) {
            val hasHeader = banner != null || title != null || subtitle != null
            if (!back && banner == null && hasHeader) Spacer(Modifier.height(38.dp))
            if (back) BackLink()
            if (banner != null) TitleBanner(banner, top = if (back) 4.dp else 52.dp)
            if (title != null) {
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = T.head(26, weight = W700, height = 1.1), modifier = Modifier.weight(1f))
                    trailing?.invoke()
                }
            }
            if (subtitle != null) Text(subtitle, style = T.body(14, color = C.muted), modifier = Modifier.padding(top = 4.dp))
            if (hasHeader) Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun BackLink(label: String = "Back") {
    val nav = LocalNav.current
    Row(
        Modifier
            .padding(top = 40.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { nav.back() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = C.blueBtn, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = T.body(14, color = C.blueBtn, weight = W800))
    }
}

/** Orange banner title with a blue shadow behind it. */
@Composable
fun TitleBanner(text: String, top: Dp = 52.dp) {
    Box(Modifier.padding(top = top, bottom = 6.dp).fillMaxWidth().height(70.dp)) {
        Box(
            Modifier
                .padding(start = 8.dp, end = 30.dp, top = 10.dp)
                .fillMaxWidth()
                .height(56.dp)
                .rotate(-1.7f)
                .background(C.blue, RoundedCornerShape(34.dp)),
        )
        Box(
            Modifier
                .padding(end = 40.dp)
                .fillMaxWidth()
                .height(58.dp)
                .rotate(-1.4f)
                .background(C.orange, RoundedCornerShape(32.dp))
                .border(2.dp, Color(0xFFFFC9A0), RoundedCornerShape(32.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = T.head(if (text.length > 18) 22 else 28, color = Color.White, weight = W700), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** The rounded gradient panel that holds a page's content. */
@Composable
fun Panel(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(16.dp), content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF1BE), Color(0xFFE2F5CF), Color(0xFFE2F2FD))))
            .border(1.dp, Color(0xFFF6D9B3), shape)
            .padding(padding),
        content = content,
    )
}

@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    color: Color = Color.White,
    border: Color = C.cardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, ambientColor = Color(0x14142938), spotColor = Color(0x14142938))
            .clip(shape)
            .background(color)
            .border(1.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

enum class BtnKind { Orange, Blue, White, Danger }

/** Pill buttons with the website's bottom edge. */
@Composable
fun Btn(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    kind: BtnKind = BtnKind.Orange,
    big: Boolean = false,
    expand: Boolean = false,
    onClick: (() -> Unit)?,
) {
    val (bg, fg, edge) = when (kind) {
        BtnKind.Orange -> Triple(C.orange, C.ink, C.orangeDark)
        BtnKind.Blue -> Triple(C.blueBtn, Color.White, C.blueDark)
        BtnKind.Danger -> Triple(C.red, Color.White, Color(0xFF8E211C))
        BtnKind.White -> Triple(Color.White, C.navy, C.cardBorder)
    }
    val shape = RoundedCornerShape(30.dp)
    Box(modifier.padding(bottom = 4.dp).then(if (expand) Modifier.fillMaxWidth() else Modifier).alpha(if (onClick != null) 1f else 0.5f)) {
        Box(Modifier.matchParentSize().offset(y = 4.dp).background(edge, shape))
        Row(
            Modifier
                .then(if (expand) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = if (big) 58.dp else 46.dp)
                .clip(shape)
                .background(bg)
                .then(if (kind == BtnKind.White) Modifier.border(2.dp, C.cardBorder, shape) else Modifier)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = if (big) 28.dp else 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = if (label.isEmpty()) iconLabel(icon) else null, tint = fg, modifier = Modifier.size(if (big) 24.dp else 20.dp))
                if (label.isNotEmpty()) Spacer(Modifier.width(8.dp))
            }
            if (label.isNotEmpty()) Text(label, textAlign = TextAlign.Center, style = T.body(if (big) 18 else 15, color = fg, weight = W800))
        }
    }
}

private fun iconLabel(icon: ImageVector): String = icon.name.substringAfterLast('.').replace(Regex("([a-z])([A-Z])"), "$1 $2")

/** Small rounded label. */
@Composable
fun Tag(text: String, bg: Color = C.yellow, fg: Color = C.ink, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        maxLines = 1,
        softWrap = false,
        style = T.body(11, color = fg, weight = W800).copy(letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp)),
        modifier = modifier.background(bg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
fun Bar(pct: Int, color: Color = C.blueBtn, height: Dp = 10.dp, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { pct.coerceIn(0, 100) / 100f },
        modifier = modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(height)),
        color = color,
        trackColor = C.track,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

fun levelBg(l: String) = when (l) {
    "Growing" -> Color(0xFFDDF3E6)
    "Confident" -> Color(0xFFE8F1FA)
    else -> Color(0xFFFFF3D6)
}

fun levelFg(l: String) = when (l) {
    "Growing" -> Color(0xFF1D6B42)
    "Confident" -> Color(0xFF1F4F7A)
    else -> C.navy
}

/** The story's cover picture. */
@Composable
fun StoryCover(story: Story, modifier: Modifier = Modifier, height: Dp? = null, radius: Dp = 22.dp) {
    AsyncImage(
        model = asset(story.cover),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier)
            .clip(RoundedCornerShape(radius))
            .background(Color(story.color)),
    )
}

/** Heart that saves a storybook. */
@Composable
fun SaveHeart(story: Story) {
    val app = LocalApp.current
    val on = app.saved.contains(story.id)
    Box(
        Modifier
            .size(44.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(if (on) Color(0xFFFFE3EA) else Color.White)
            .border(2.dp, if (on) Color(0xFFE86A8A) else C.cardBorder, CircleShape)
            .clickable { app.toggleSaved(story.id) }
            .semantics { contentDescription = if (on) "Remove ${story.title} from saved stories" else "Save ${story.title}" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (on) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (on) Color(0xFFE86A8A) else Color(0xFFC9506B), modifier = Modifier.size(22.dp))
    }
}

/** A pill-shaped storybook row (cover, title, tags, progress, heart). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookRow(story: Story) {
    val app = LocalApp.current
    val nav = LocalNav.current
    val entry = app.progress[story.id]
    val pct = percentRead(story, entry)
    val shape = RoundedCornerShape(34.dp)
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape, ambientColor = Color(0x14142938), spotColor = Color(0x14142938))
                .clip(shape)
                .background(Color(0xFFFFF8E3))
                .border(1.dp, Color(0xFFF6E3B8), shape)
                .clickable { nav.push("books/${story.id}") }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StoryCover(story, Modifier.size(104.dp), radius = 26.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f).padding(end = 40.dp)) {
                Text(story.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = T.head(19, height = 1.15))
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(story.language)
                    if (story.shownLevel != story.language) Tag(story.shownLevel, bg = C.orange)
                }
                if (entry != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Bar(pct, height = 7.dp, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(if (entry.finished) "Done" else "$pct%", style = T.body(11, color = C.muted, weight = W800))
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) { SaveHeart(story) }
    }
}

/** A square-ish story card for horizontal lists. */
@Composable
fun StoryCard(story: Story, note: String? = null, pct: Int? = null, width: Dp = 170.dp) {
    val nav = LocalNav.current
    Box(Modifier.width(width)) {
        WhiteCard(padding = PaddingValues(10.dp), onClick = { nav.push("books/${story.id}") }) {
            StoryCover(story, height = 96.dp, radius = 16.dp)
            Spacer(Modifier.height(8.dp))
            Text(story.title, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, style = T.head(15, height = 1.15))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Tag(story.language, bg = C.cream2)
                Text("${story.minutes} min", style = T.body(11, color = C.muted, weight = W800))
            }
            if (pct != null) {
                Spacer(Modifier.height(8.dp))
                Bar(pct, height = 7.dp)
            }
        }
        if (note != null) {
            Text(
                note,
                style = T.body(10, weight = W800, color = C.navy),
                modifier = Modifier.padding(8.dp).shadow(2.dp, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { SaveHeart(story) }
    }
}

@Composable
fun SectionTitle(text: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.padding(top = 6.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = T.head(20), modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = T.body(14, color = C.blueBtn, weight = W800), modifier = Modifier.clickable { onAction?.invoke() })
    }
}

/** Segmented pill control. */
@Composable
fun <V> Seg(options: List<V>, value: V, label: (V) -> String, modifier: Modifier = Modifier, onChange: (V) -> Unit) {
    Row(
        modifier
            .background(Color.White, RoundedCornerShape(30.dp))
            .border(1.dp, C.cardBorder, RoundedCornerShape(30.dp))
            .padding(4.dp),
    ) {
        for (o in options) {
            Text(
                label(o),
                style = T.body(13, color = if (o == value) Color.White else C.muted, weight = W800),
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (o == value) C.blueBtn else Color.Transparent)
                    .clickable { onChange(o) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            )
        }
    }
}

/** Horizontally scrollable [Seg] for many options. */
@Composable
fun <V> ScrollSeg(options: List<V>, value: V, label: (V) -> String, onChange: (V) -> Unit) {
    Box(Modifier.horizontalScroll(rememberScrollState())) { Seg(options, value, label, onChange = onChange) }
}

@Composable
fun FormError(text: String) {
    if (text.isNotEmpty()) Text(text, textAlign = TextAlign.Center, style = T.body(14, color = C.red, weight = W800), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
}

/** A friendly pop-up with one button. */
@Composable
fun MessageDialog(title: String, message: String, button: String = "OK", onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, style = T.head(22), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = { Text(message, style = T.body(15, color = C.muted), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Btn(button, onClick = onDismiss) } },
    )
}

/** A two-button question, e.g. "Leaving already?". */
@Composable
fun ConfirmDialog(title: String, message: String, yes: String, no: String, onYes: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, style = T.head(22), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = { Text(message, style = T.body(15, color = C.muted), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                Btn(no, kind = BtnKind.White, onClick = onDismiss)
                Btn(yes, onClick = onYes)
            }
        },
    )
}

/** A white rounded text box, like the website's inputs. */
@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    password: Boolean = false,
    keyboard: KeyboardType = KeyboardType.Text,
    lines: Int = 1,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    label: String? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = T.body(16),
        placeholder = hint?.let { { Text(it, style = T.body(16, color = C.muted)) } },
        label = label?.let { { Text(it, style = T.body(14, color = C.muted)) } },
        singleLine = lines == 1,
        minLines = lines,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        leadingIcon = leading,
        trailingIcon = trailing,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = C.blueBtn,
            unfocusedBorderColor = C.track,
            cursorColor = C.blueBtn,
        ),
    )
}

/** Lays out [content] in rows that wrap, centered. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Wrap(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    runSpacing: Dp = 8.dp,
    center: Boolean = false,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier,
        horizontalArrangement = if (center) Arrangement.spacedBy(spacing, Alignment.CenterHorizontally) else Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(runSpacing),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** A round colored icon badge. */
@Composable
fun IconCircle(icon: ImageVector, bg: Color, fg: Color, size: Dp = 60.dp, iconSize: Dp = 28.dp) {
    Box(Modifier.size(size).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(iconSize))
    }
}

/** Fills the rest of a [BoxScope] with a centered message, used for missing items. */
@Composable
fun BoxScope.CenterNote(text: String) {
    Text(text, style = T.body(15, color = C.muted, weight = W800), textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
}

package ph.basakita.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ph.basakita.app.R

/** Colors from the BASA-KITA design system (same tokens as the website). */
object C {
    val cream = Color(0xFFFFF9ED)
    val cream2 = Color(0xFFFFF3D6)
    val orange = Color(0xFFFF7A22)
    val orangeDark = Color(0xFFD9600F)
    val orangeText = Color(0xFFB4520F)
    val yellow = Color(0xFFFFE588)
    val yellow2 = Color(0xFFF6C85F)
    val green = Color(0xFF46B97B)
    val greenText = Color(0xFF1D6B42)
    val blue = Color(0xFF3E89C9)
    val blueBtn = Color(0xFF2F6FA8)
    val blueDark = Color(0xFF1F4F7A)
    val navy = Color(0xFF183B5B)
    val ink = Color(0xFF14293A)
    val muted = Color(0xFF566879)
    val border = Color(0xFFF3EEE3)
    val cardBorder = Color(0xFFF2E4C4)
    val red = Color(0xFFC7302A)
    val white = Color.White
    val track = Color(0xFFD9E2EC)
    val paleBlue = Color(0xFFEAF3FB)
    val paleGreen = Color(0xFFEAF8F0)
    val locked = Color(0xFFECE8DF)
}

/** Fredoka for headings, Nunito for text, Chewy for fun labels, Luckiest Guy for pills. Bundled, so they work offline. */
private val fredoka = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)
private val nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)
private val chewy = FontFamily(Font(R.font.chewy_regular))
private val luckiest = FontFamily(Font(R.font.luckiestguy_regular))

object T {
    fun head(size: Int, color: Color = C.navy, weight: FontWeight = FontWeight.SemiBold, height: Double? = null) =
        TextStyle(fontFamily = fredoka, fontSize = size.sp, color = color, fontWeight = weight, lineHeight = height?.em ?: TextUnit.Unspecified)

    fun body(size: Int, color: Color = C.ink, weight: FontWeight = FontWeight.SemiBold, height: Double? = null) =
        TextStyle(fontFamily = nunito, fontSize = size.sp, color = color, fontWeight = weight, lineHeight = height?.em ?: TextUnit.Unspecified)

    fun fun_(size: Int, color: Color = C.orange) = TextStyle(fontFamily = chewy, fontSize = size.sp, color = color)

    fun pill(size: Int, color: Color = C.ink) = TextStyle(fontFamily = luckiest, fontSize = size.sp, color = color, letterSpacing = 0.5.sp)
}

/** Bold and extra bold, named like the Flutter app's w800 / w700. */
val W800 = FontWeight.ExtraBold
val W700 = FontWeight.Bold

@Composable
fun BasaKitaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.orange,
            onPrimary = C.ink,
            secondary = C.blueBtn,
            background = C.cream,
            surface = C.cream,
            surfaceContainer = Color.White,
        ),
        content = content,
    )
}

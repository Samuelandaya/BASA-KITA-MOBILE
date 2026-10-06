package ph.basakita.app.services

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Reads the words in a picture on the phone itself (Google ML Kit), so no internet is needed. */
suspend fun recognizeText(context: Context, image: Uri): String {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        val input = InputImage.fromFilePath(context, image)
        val text = suspendCancellableCoroutine { cont ->
            recognizer.process(input)
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        return text.replace(Regex("\\s+\\n"), "\n").trim()
    } finally {
        recognizer.close()
    }
}

package ph.basakita.app.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ph.basakita.app.data.normalizeWord

/**
 * Listens through the microphone and reports which target words were heard,
 * with how clearly the phone understood them (used for the pronunciation score).
 */
class MicCheck(private val context: Context, private val lang: String) {
    var listening by mutableStateOf(false)
        private set
    var heard by mutableStateOf<Set<String>>(emptySet())
        private set
    var notice by mutableStateOf("")
        private set
    var confidence by mutableStateOf<Double?>(null)
        private set

    private var recognizer: SpeechRecognizer? = null
    private var targets: List<String> = emptyList()

    private fun recognizer(): SpeechRecognizer? {
        recognizer?.let { return it }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return null
        return SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) {
            listening = false
            notice = when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Please allow the microphone so BASA-KITA can hear you."
                SpeechRecognizer.ERROR_CLIENT -> ""
                else -> "I couldn’t hear you. Let’s try again!"
            }
        }

        override fun onPartialResults(partialResults: Bundle?) = take(partialResults, final = false)

        override fun onResults(results: Bundle?) {
            take(results, final = true)
            listening = false
        }
    }

    private fun take(b: Bundle?, final: Boolean) {
        val said = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
        val words = said.split(Regex("\\s+")).map(::normalizeWord).toSet()
        heard = targets.filter(words::contains).toSet()
        val c = b.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
        if (final && c != null && c > 0) confidence = c.toDouble()
    }

    fun reset() {
        recognizer?.cancel()
        heard = emptySet()
        notice = ""
        confidence = null
        listening = false
    }

    suspend fun listen(targetWords: List<String>) {
        if (listening) {
            recognizer?.stopListening()
            listening = false
            return
        }
        if (!Permissions.request(context, Manifest.permission.RECORD_AUDIO)) {
            notice = "Please allow the microphone so BASA-KITA can hear you."
            return
        }
        val r = recognizer()
        if (r == null) {
            notice = "This phone can’t listen yet. Say it to a grown-up and tap “I said it!”."
            return
        }
        targets = targetWords.map(::normalizeWord)
        notice = ""
        listening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    /** For grown-ups on phones without speech recognition: mark the words as read. */
    fun markAll(targetWords: List<String>) {
        recognizer?.cancel()
        listening = false
        heard = targetWords.map(::normalizeWord).toSet()
    }

    fun dispose() {
        recognizer?.destroy()
        recognizer = null
    }
}

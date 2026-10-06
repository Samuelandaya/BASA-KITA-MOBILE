package ph.basakita.app.services

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import ph.basakita.app.data.tokenize
import java.util.Locale

/**
 * Reading voices. The phone supplies the real voices; each choice picks the most natural
 * voice installed for the language and shapes it with its own pitch and pace.
 */
data class VoiceProfile(
    val id: String,
    val name: String,
    val blurb: String,
    val who: String, // female | male
    val rank: Int,
    val pitch: Float,
    val rate: Float,
    val prefer: List<String>,
    val sample: String,
    val sampleFil: String,
)

val voiceProfiles = listOf(
    VoiceProfile("ana", "Teacher Ana", "Warm and friendly woman", "female", 0, 1.0f, 1.0f, emptyList(), "Hi! I’m Teacher Ana. Let’s read a story together!", "Kumusta! Ako si Teacher Ana. Magbasa tayo ng kuwento!"),
    VoiceProfile("ben", "Teacher Ben", "Clear and calm man", "male", 0, 0.97f, 1.0f, emptyList(), "Hello! I’m Teacher Ben. Are you ready to read?", "Kumusta! Ako si Teacher Ben. Handa ka na bang magbasa?"),
    VoiceProfile("maya", "Teen Maya", "Teenage girl, upbeat and casual", "female", 0, 1.08f, 1.05f, listOf("jenny", "michelle", "aria", "ava", "emma", "samantha", "sonia", "libby"), "Hey! I’m Maya. Want to read this one with me? It’s going to be fun!", "Hi! Ako si Maya. Basahin natin ito nang sabay, tiyak na masaya!"),
    VoiceProfile("jay", "Teen Jay", "Teenage boy, easygoing", "male", 0, 1.1f, 1.05f, listOf("eric", "ryan", "davis", "jason", "tony", "guy", "alex"), "Hey, I’m Jay. Let’s read this together, it’s a good one!", "Uy, ako si Jay. Basahin natin ito nang magkasama, maganda ito!"),
    VoiceProfile("mia", "Little Mia", "Bright and cheerful girl", "female", 0, 1.15f, 1.03f, listOf("ana", "jenny", "aria"), "Yay! I’m Mia. Reading is so much fun!", "Yehey! Ako si Mia. Masaya ang magbasa!"),
    VoiceProfile("lola", "Lola Rosa", "Slow and gentle storyteller", "female", 2, 0.93f, 0.9f, emptyList(), "Come close, little one. Let me tell you a story.", "Halika, apo. Magkukuwento si Lola."),
)

fun voiceProfile(id: String): VoiceProfile = voiceProfiles.firstOrNull { it.id == id } ?: voiceProfiles.first()

private val femaleNames = Regex("\\b(female|woman|aria|jenny|samantha|zira|ava|ana|blessica|clara|leah|allison|susan|karen|moana|rosa|emma|michelle|sara|serena|victoria|fiona|joanna|libby|sonia|natasha|elsa)\\b", RegexOption.IGNORE_CASE)
private val maleNames = Regex("\\b(male|man|guy|davis|tony|alex|daniel|fred|ryan|eric|christopher|andrew|brandon|roger|david|mark|angelo|james|thomas|george|jason|brian)\\b", RegexOption.IGNORE_CASE)

private fun gender(v: Voice): String {
    val features = v.features.orEmpty().joinToString(" ")
    val text = "${v.name} $features"
    return when {
        femaleNames.containsMatchIn(text) -> "female"
        maleNames.containsMatchIn(text) -> "male"
        else -> "unknown"
    }
}

/** Higher means a more natural sounding voice. */
private fun quality(v: Voice): Int {
    val n = v.name.lowercase()
    var q = 0
    if (v.quality >= Voice.QUALITY_VERY_HIGH) q += 100 else if (v.quality >= Voice.QUALITY_HIGH) q += 60
    if (Regex("natural|neural|online|network").containsMatchIn(n)) q += 40
    if (!v.isNetworkConnectionRequired) q += 10 // works offline
    if (Regex("espeak|compact").containsMatchIn(n)) q -= 100
    return q
}

fun isNaturalVoice(v: Voice?): Boolean = v != null && quality(v) >= 60

/** Text to speech with word-by-word progress. */
object Speech {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: (() -> Unit)? = null
    private val main = Handler(Looper.getMainLooper())

    var voiceId = "ana"
    var volume = 1f

    private var onWord: ((Int, Int) -> Unit)? = null
    private var onEnd: (() -> Unit)? = null
    private var gotWord = false
    private var utterance = 0
    private val fallback = mutableListOf<Runnable>()

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            main.post {
                ready = status == TextToSpeech.SUCCESS
                if (!ready) Log.w("Speech", "Text to speech is not available")
                tts?.setOnUtteranceProgressListener(listener)
                pending?.invoke()
                pending = null
            }
        }
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(id: String?) {}
        override fun onDone(id: String?) {
            main.post { if (id == utterance.toString()) finish() }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(id: String?) {
            main.post { if (id == utterance.toString()) finish() }
        }
        override fun onRangeStart(id: String?, start: Int, end: Int, frame: Int) {
            main.post {
                if (id != utterance.toString()) return@post
                gotWord = true
                cancelFallback()
                onWord?.invoke(start, end)
            }
        }
    }

    private fun finish() {
        cancelFallback()
        val cb = onEnd
        onEnd = null
        onWord = null
        cb?.invoke()
    }

    private fun cancelFallback() {
        fallback.forEach(main::removeCallbacks)
        fallback.clear()
    }

    val voices: List<Voice> get() = try { tts?.voices?.toList().orEmpty() } catch (_: Exception) { emptyList() }

    /** The most natural installed voice for a language and a voice choice. */
    fun pickVoice(lang: String, id: String? = null): Voice? {
        val p = voiceProfile(id ?: voiceId)
        val code = lang.lowercase().replace('_', '-')
        fun tag(v: Voice) = v.locale.toLanguageTag().lowercase()
        var pool = voices.filter { tag(it) == code && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
        if (pool.isEmpty()) pool = voices.filter { tag(it).startsWith(code.take(2)) || (code.startsWith("fil") && tag(it).startsWith("tl")) }
        if (pool.isEmpty()) return null
        pool = pool.sortedByDescending(::quality)
        val wanted = pool.filter { gender(it) == p.who }
        val from = wanted.ifEmpty { pool }
        val top = quality(from.first())
        val tier = from.filter { quality(it) >= top - 40 }
        val liked = mutableListOf<Voice>()
        for (n in p.prefer) {
            val re = Regex("\\b$n\\b", RegexOption.IGNORE_CASE)
            tier.filter { re.containsMatchIn(it.name) && it !in liked }.forEach(liked::add)
        }
        val list = liked.ifEmpty { tier }
        return list[p.rank % list.size]
    }

    /** Reads [text] aloud. [onWord] gets the start and end position of each word as it is spoken. */
    fun speak(
        text: String,
        lang: String = "en-US",
        rate: Double = 1.0,
        onWord: ((Int, Int) -> Unit)? = null,
        onEnd: (() -> Unit)? = null,
        voiceId: String? = null,
    ) {
        if (!ready) {
            if (tts == null) {
                onEnd?.invoke()
                return
            }
            pending = { speak(text, lang, rate, onWord, onEnd, voiceId) }
            return
        }
        stop()
        val engine = tts ?: return
        val p = voiceProfile(voiceId ?: this.voiceId)
        this.onWord = onWord
        this.onEnd = onEnd
        gotWord = false
        utterance++
        try {
            val v = pickVoice(lang, p.id)
            if (v != null) engine.voice = v else engine.language = Locale.forLanguageTag(lang)
            engine.setSpeechRate((rate * p.rate).toFloat().coerceIn(0.5f, 1.8f))
            engine.setPitch(p.pitch.coerceIn(0.5f, 2f))
            // Some voices send no word events, so estimate the timing from the text length.
            if (onWord != null) {
                val start = Runnable {
                    if (gotWord) return@Runnable
                    val perChar = 75 / (rate * p.rate)
                    for (t in tokenize(text)) {
                        val r = Runnable { if (!gotWord) this.onWord?.invoke(t.start, t.end) }
                        fallback.add(r)
                        main.postDelayed(r, (t.start * perChar - 700).coerceAtLeast(0.0).toLong())
                    }
                }
                fallback.add(start)
                main.postDelayed(start, 700)
            }
            val params = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume) }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utterance.toString())
        } catch (e: Exception) {
            Log.w("Speech", "speak failed", e)
            finish()
        }
    }

    fun stop() {
        cancelFallback()
        onWord = null
        onEnd = null
        pending = null
        utterance++
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }
}

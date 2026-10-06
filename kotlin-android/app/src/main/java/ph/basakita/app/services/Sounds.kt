package ph.basakita.app.services

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Sound effects and soft background music, synthesized in code so the app needs no audio files.
 * Both follow the Settings: Sound effects, Background music and Volume.
 */
object Sounds {
    private const val RATE = 22050
    private var volume = 0.7f
    private var effects = true
    private var musicOn = false
    private var music: AudioTrack? = null
    private var fx: AudioTrack? = null
    private val cache = mutableMapOf<String, ShortArray>()

    private class Note(val freq: Double, val start: Double, val dur: Double, val triangle: Boolean = false, val gain: Double = 1.0)

    fun configure(volume: Int, effects: Boolean, music: Boolean) {
        this.volume = (volume / 100f).coerceIn(0f, 1f)
        this.effects = effects
        this.music?.setVolume(this.volume * 0.35f)
        if (music != musicOn) {
            musicOn = music
            if (music) startMusic() else stopMusic()
        }
    }

    /** kinds: good (right answer, word said), star (story finished), oops (try again), tap */
    fun play(kind: String) {
        if (!effects || volume == 0f) return
        val samples = cache.getOrPut(kind) { effect(kind) }
        try {
            fx?.release()
            fx = track(samples, loop = false).apply {
                setVolume(volume)
                play()
            }
        } catch (e: Exception) {
            Log.w("Sounds", "sound failed", e)
        }
    }

    private fun startMusic() {
        if (music != null) return
        try {
            music = track(musicLoop(), loop = true).apply {
                setVolume(volume * 0.35f)
                play()
            }
        } catch (e: Exception) {
            music = null
            Log.w("Sounds", "music failed", e)
        }
    }

    private fun stopMusic() {
        music?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            it.release()
        }
        music = null
    }

    private fun track(samples: ShortArray, loop: Boolean): AudioTrack {
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()
        t.write(samples, 0, samples.size)
        if (loop) t.setLoopPoints(0, samples.size, -1)
        return t
    }

    // --- synthesis
    private fun effect(kind: String): ShortArray {
        val notes = when (kind) {
            "good" -> listOf(Note(660.0, 0.0, .2), Note(880.0, .12, .32))
            "star" -> List(4) { i -> Note(listOf(523.0, 659.0, 784.0, 1047.0)[i], i * .11, .4) }
            "oops" -> listOf(Note(330.0, 0.0, .2, triangle = true), Note(262.0, .14, .32, triangle = true))
            else -> listOf(Note(520.0, 0.0, .12))
        }
        return pcm(mix(notes, 1.0, 0.5))
    }

    private fun musicLoop(): ShortArray {
        val scale = listOf(262.0, 294.0, 330.0, 392.0, 440.0, 523.0, 587.0)
        val notes = mutableListOf<Note>()
        for (i in 0 until 18) {
            val f = scale[(i * 3 + (i % 3)) % scale.size]
            notes.add(Note(f, i * .9, 1.6))
            if (i % 4 == 0) notes.add(Note(f / 2, i * .9, 2.4, triangle = true, gain = .7))
        }
        return pcm(mix(notes, 18 * .9 + .2, 0.35))
    }

    private fun mix(notes: List<Note>, minSeconds: Double, master: Double): DoubleArray {
        val end = notes.fold(minSeconds) { m, n -> max(m, n.start + n.dur + .05) }
        val s = DoubleArray(ceil(end * RATE).toInt())
        for (n in notes) {
            val from = floor(n.start * RATE).toInt()
            val len = floor(n.dur * RATE).toInt()
            var k = 0
            while (k < len && from + k < s.size) {
                val t = k.toDouble() / RATE
                val env = min(1.0, t / .02) * exp(-3.2 * t / n.dur)
                val ph = 2 * PI * n.freq * t
                val wave = if (n.triangle) (2 / PI) * asin(sin(ph)) else sin(ph)
                s[from + k] += wave * env * n.gain * master * .5
                k++
            }
        }
        return s
    }

    private fun pcm(samples: DoubleArray): ShortArray = ShortArray(samples.size) { (samples[it].coerceIn(-1.0, 1.0) * 32767).roundToInt().toShort() }
}

package ph.basakita.app.data

private const val VOWELS = "aeiouáéíóú"

// English words don't follow simple consonant-vowel rules, so their parts are listed.
private val enParts = mapOf(
    "teacher" to listOf("tea", "cher"), "together" to listOf("to", "geth", "er"), "under" to listOf("un", "der"),
    "magic" to listOf("ma", "gic"), "sharing" to listOf("shar", "ing"), "bounced" to listOf("boun", "ced"),
    "picked" to listOf("pick", "ed"), "squirrel" to listOf("squir", "rel"), "puppy" to listOf("pup", "py"),
    "happy" to listOf("hap", "py"), "apple" to listOf("ap", "ple"), "rabbit" to listOf("rab", "bit"),
    "insect" to listOf("in", "sect"), "octopus" to listOf("oc", "to", "pus"), "umbrella" to listOf("um", "brel", "la"),
    "little" to listOf("lit", "tle"), "playtime" to listOf("play", "time"),
)

private val notLetterOrDigit = Regex("[^\\p{L}\\p{N}]")
private val notLetter = Regex("[^\\p{L}]")

/** Lowercase letters and digits only, so "Ball," matches "ball". */
fun normalizeWord(w: String): String = w.lowercase().replace(notLetterOrDigit, "")

fun vocabFor(word: String): VocabEntry? = vocab[normalizeWord(word)]

private fun isVowel(c: Char) = VOWELS.contains(c)

/**
 * Word parts for kids: Filipino uses consonant-vowel syllables ("ng" kept together);
 * English uses the list above, otherwise the whole word.
 */
fun syllables(word: String, filipino: Boolean = true): List<String> {
    val raw = word.lowercase().replace(notLetter, "")
    if (!filipino) return enParts[raw] ?: listOf(raw)
    val w = raw.replace("ng", "ŋ")
    val parts = mutableListOf<String>()
    var cur = ""
    var i = 0
    while (i < w.length) {
        cur += w[i]
        val vowel = isVowel(w[i])
        val next = w.getOrNull(i + 1)
        val afterNext = w.getOrNull(i + 2)
        if (vowel && next != null && !isVowel(next)) {
            // keep a single consonant for the next syllable; split clusters
            if (afterNext != null && !isVowel(afterNext)) {
                cur += next
                i++
            }
            parts.add(cur)
            cur = ""
        } else if (vowel && next != null && isVowel(next)) {
            parts.add(cur)
            cur = ""
        }
        i++
    }
    if (cur.isNotEmpty()) {
        if (parts.isNotEmpty() && cur.none(::isVowel)) parts[parts.size - 1] += cur else parts.add(cur)
    }
    return (if (parts.isEmpty()) listOf(w) else parts).map { it.replace("ŋ", "ng") }
}

/** A word with where it sits in the text, so narration can highlight it. */
data class Token(val word: String, val start: Int, val end: Int)

private val nonSpace = Regex("\\S+")

fun tokenize(text: String): List<Token> = nonSpace.findAll(text).map { Token(it.value, it.range.first, it.range.last + 1) }.toList()

/** The words of a sentence, split on spaces. */
fun wordsOf(text: String): List<String> = text.trim().split(Regex("\\s+"))

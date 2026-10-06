package ph.basakita.app

import ph.basakita.app.data.Story
import ph.basakita.app.data.lessons
import ph.basakita.app.data.stories
import ph.basakita.app.state.Account
import kotlin.math.roundToInt

data class Skills(
    val letterSounds: Int,
    val english: Int?,
    val filipino: Int?,
    val reading: Int?,
    val understanding: Int?,
    val finished: Int,
)

private fun avg(xs: List<Int>): Int? = if (xs.isEmpty()) null else (xs.sum().toDouble() / xs.size).roundToInt()

/** Skill percentages from what the learner has actually done. */
fun learnerSkills(a: Account): Skills {
    val all = a.scores.values.toList()
    val quiz = a.quizzes.values.toList()
    val finished = stories.count { a.progress[it.id]?.finished == true }
    val correct = quiz.sumOf { it.correct }
    val total = quiz.sumOf { it.total }
    return Skills(
        (a.lessonsDone.toDouble() / lessons.size * 100).roundToInt(),
        avg(all.filter { it.english }.map { it.score }),
        avg(all.filter { !it.english }.map { it.score }),
        avg(all.map { it.score }),
        if (total == 0) null else (correct.toDouble() / total * 100).roundToInt(),
        finished,
    )
}

data class LevelInfo(val name: String, val index: Int, val toNext: Int, val next: String?, val goal: String, val skills: Skills)

val levelNames = listOf("Beginner", "Growing", "Confident")
private val goals = listOf(
    "Finish 2 stories and learn 4 letter sounds",
    "Finish 5 stories, learn 10 sounds and read aloud at 80% or more",
    "Keep reading new stories!",
)

/** Learner level from finished stories, letter sounds learned and read-aloud scores. */
fun learnerLevel(a: Account): LevelInfo {
    val sk = learnerSkills(a)
    val goodReading = sk.reading == null || sk.reading >= 80
    var i = 0
    if (sk.finished >= 2 && a.lessonsDone >= 4) i = 1
    if (sk.finished >= 5 && a.lessonsDone >= 10 && goodReading) i = 2
    val toNext = when (i) {
        2 -> 100
        0 -> (((sk.finished.coerceIn(0, 2) / 2.0) + (a.lessonsDone.coerceIn(0, 4) / 4.0)) * 50).roundToInt()
        else -> (((sk.finished.coerceIn(0, 5) / 5.0) + (a.lessonsDone.coerceIn(0, 10) / 10.0) + (if (goodReading) 1 else 0)) / 3 * 100).roundToInt()
    }
    return LevelInfo(levelNames[i], i, toNext, if (i < 2) levelNames[i + 1] else null, goals[i], sk)
}

data class Recommendation(val story: Story, val reason: String)

/** Unfinished stories in the chosen language, practicing known sounds first. */
fun recommendStories(a: Account, level: LevelInfo, language: String, count: Int = 4): List<Recommendation> {
    val known = lessons.take(if (a.lessonsDone < 1) 1 else a.lessonsDone).map { it.letter }.toSet()
    fun fits(s: Story) = language == "Bilingual" || s.language == language || s.language == "Bilingual"
    fun score(s: Story) = (if (a.progress[s.id] != null) 0 else 2) +
        (if (fits(s)) 3 else 0) +
        (if (known.contains(s.focus.letter)) 2 else 0) +
        (if (level.index == 0 && (s.level == "Beginner" || s.level == s.language)) 1 else 0)
    fun reason(s: Story) = when {
        known.contains(s.focus.letter) -> "Practice the ${s.focus.letter} sound"
        a.progress[s.id] == null -> "New for you"
        else -> "Keep going"
    }
    return stories.filter { a.progress[it.id]?.finished != true }
        .sortedWith { x, y -> val d = score(y) - score(x); if (d != 0) d else x.minutes - y.minutes }
        .take(count)
        .map { Recommendation(it, reason(it)) }
}

/** Easiest stories first, in the chosen language (Bilingual stories fit both). */
fun starterStories(language: String): List<Story> =
    stories.filter { language == "Bilingual" || it.language == language || it.language == "Bilingual" }
        .sortedWith(compareBy<Story> { it.minutes }.thenBy { it.pages.size })

/** Score after the child reads aloud: pronunciation (English) or word reading accuracy (Filipino). */
data class ReadingScore(val score: Int, val correct: Int, val total: Int, val english: Boolean)

fun scoreReading(words: List<String>, heard: Set<String>, confidence: Double?, lang: String): ReadingScore {
    val total = if (words.isEmpty()) 1 else words.size
    val correct = words.count { heard.contains(ph.basakita.app.data.normalizeWord(it)) }
    val accuracy = correct.toDouble() / total
    val english = !lang.startsWith("fil")
    val s = if (english && confidence != null) accuracy * (0.6 + 0.4 * confidence) else accuracy
    return ReadingScore((s * 100).roundToInt(), correct, total, english)
}

package ph.basakita.app.state

import kotlinx.serialization.Serializable

// Per-account data, with the same shape as the website's saved state.

@Serializable
data class Progress(
    var page: Int = 1,
    var finished: Boolean = false,
    var stars: Int? = null,
    var lastRead: Long? = null, // milliseconds since epoch
)

@Serializable
data class ScoreEntry(val score: Int, val english: Boolean)

@Serializable
data class QuizResult(val correct: Int, val total: Int)

/** A story the learner wrote. Pictures are small base64 JPEGs, one per page. */
@Serializable
data class Draft(
    val id: String,
    var title: String = "My Story",
    var language: String = "Filipino",
    var character: String = "Cat",
    var place: String = "Home",
    var texts: List<String> = listOf(""),
    var images: List<String> = emptyList(),
) {
    val pages: Int get() = texts.size
}

@Serializable
data class Account(
    val progress: MutableMap<String, Progress> = mutableMapOf(),
    var lessonsDone: Int = 0,
    val drafts: MutableList<Draft> = mutableListOf(),
    val saved: MutableList<String> = mutableListOf(),
    val scores: MutableMap<String, ScoreEntry> = mutableMapOf(), // "storyId:page"
    val quizzes: MutableMap<String, QuizResult> = mutableMapOf(),
    var sample: Boolean = false,
)

@Serializable
data class User(
    var firstName: String = "Reader",
    var lastName: String = "",
    var username: String = "",
    var email: String = "",
    var role: String = "learner", // learner | admin | pending
    var returning: Boolean = false,
    var school: String = "",
    var teacher: String = "",
)

@Serializable
data class Settings(
    var language: String = "Bilingual", // Filipino | English | Bilingual
    var languageChosen: Boolean = false,
    var speed: Double = 1.0,
    var textSize: String = "Big", // Normal | Big | Bigger
    var highlight: Boolean = true,
    var narration: Boolean = true,
    var effects: Boolean = true,
    var music: Boolean = false,
    var volume: Int = 70, // 0-100
    var parentLock: Boolean = false,
    var pin: String = "",
    var reminder: Boolean = false,
    var voice: String = "ana", // ana | ben | maya | jay | mia | lola
)

/** Everything saved on the phone. */
@Serializable
data class SavedState(
    val user: User? = null,
    val avatarColor: Long = 0xFFFFB27A,
    val settings: Settings = Settings(),
    val accounts: Map<String, Account> = emptyMap(),
)

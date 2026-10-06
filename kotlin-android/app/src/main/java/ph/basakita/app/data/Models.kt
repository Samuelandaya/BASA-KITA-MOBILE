package ph.basakita.app.data

// Plain data types shared by the stories, lessons, vocabulary and flashcards.
// Colors are ARGB values like 0xFF3E89C9.

data class QuizOption(val label: String, val color: Long, val correct: Boolean)

data class Quiz(val question: String, val options: List<QuizOption>)

data class StoryPage(val text: String, val word: String) // word: the word to highlight in Read and Learn

data class Focus(
    val letter: String,
    val sound: String,
    val example: String,
    val exampleFil: String,
    val syllables: List<String>,
    val parts: List<String>,
    val partsFil: List<String>,
)

data class Story(
    val id: String,
    val title: String,
    val language: String, // Filipino | English | Bilingual
    val lang: String, // speech locale, e.g. en-US
    val level: String,
    val minutes: Int,
    val ages: String,
    val theme: String,
    val color: Long,
    val cover: String, // asset path, e.g. covers/mia-red-ball.svg
    val summary: String,
    val blurb: String,
    val focus: Focus,
    val pages: List<StoryPage>,
    val questions: List<Quiz>, // three comprehension questions
) {
    val isFilipino: Boolean get() = lang.startsWith("fil")
    val shownLevel: String get() = if (level == language) "Beginner" else level
}

data class Lesson(val id: Int, val letter: String, val fil: String, val eng: String)

data class VocabEntry(val pair: String, val meaning: String)

data class FlashCard(val fil: String, val shape: String, val eng: String, val syl: List<String>, val color: Long)

data class Deck(val letter: String, val cards: List<FlashCard>)

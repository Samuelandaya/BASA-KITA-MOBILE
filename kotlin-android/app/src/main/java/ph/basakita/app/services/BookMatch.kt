package ph.basakita.app.services

import ph.basakita.app.data.FlashCard
import ph.basakita.app.data.Story
import ph.basakita.app.data.decks
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.data.stories

data class BookMatch(val story: Story, val page: Int, val score: Double) // page is zero based

private fun words(t: String): List<String> = t.split(Regex("\\s+")).map(::normalizeWord).filter { it.isNotEmpty() }

/** Book recognition: finds the storybook page whose words best match the text read from a picture. */
fun recognizeBook(text: String): BookMatch? {
    val found = words(text).toSet()
    if (found.isEmpty()) return null
    var best: BookMatch? = null
    for (story in stories) {
        val titleWords = words(story.title)
        val titleHit = titleWords.count(found::contains).toDouble() / titleWords.size
        story.pages.forEachIndexed { page, p ->
            val pw = words(p.text)
            val score = pw.count(found::contains).toDouble() / pw.size
            val total = maxOf(score, titleHit * 0.9)
            if (best == null || total > best!!.score) best = BookMatch(story, page, total)
        }
    }
    return best?.takeIf { it.score >= 0.5 }
}

/** Flashcard scanning: finds the flashcard whose word appears in the text read from a picture. */
fun recognizeFlashCard(text: String): FlashCard? {
    val found = words(text).toSet()
    return decks.flatMap { it.cards }.firstOrNull { found.contains(normalizeWord(it.fil)) }
}

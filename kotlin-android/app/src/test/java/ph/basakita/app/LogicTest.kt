package ph.basakita.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.basakita.app.data.decks
import ph.basakita.app.data.lessons
import ph.basakita.app.data.normalizeWord
import ph.basakita.app.data.stories
import ph.basakita.app.data.syllables
import ph.basakita.app.services.recognizeBook
import ph.basakita.app.services.recognizeFlashCard
import ph.basakita.app.state.Account
import ph.basakita.app.state.AppState
import ph.basakita.app.state.Draft
import ph.basakita.app.state.MemoryStore
import ph.basakita.app.state.User
import ph.basakita.app.state.continueId
import ph.basakita.app.state.isNewReader
import ph.basakita.app.state.percentRead
import ph.basakita.app.state.sampleAccount
import ph.basakita.app.state.storyById

class LogicTest {
    // --- content
    @Test fun `has 6 stories with 3 questions each, 20 lessons and 6 flashcard decks`() {
        assertEquals(6, stories.size)
        assertTrue(stories.all { it.questions.size == 3 })
        assertEquals(20, lessons.size)
        assertEquals(6, decks.size)
    }

    @Test fun `every flashcard has its own 3D model name`() {
        assertEquals(18, decks.flatMap { it.cards }.map { it.fil }.toSet().size)
    }

    // --- syllables
    @Test fun `Filipino consonant-vowel parts keep ng together`() {
        assertEquals(listOf("sa", "rang", "go", "la"), syllables("saranggola"))
        assertEquals(listOf("ma", "ma"), syllables("mama"))
    }

    @Test fun `English words use the listed parts`() {
        assertEquals(listOf("tea", "cher"), syllables("teacher", filipino = false))
        assertEquals(listOf("ball"), syllables("ball", filipino = false))
    }

    @Test fun `normalizeWord strips punctuation`() = assertEquals("ball", normalizeWord("Ball,"))

    // --- book recognition
    @Test fun `finds the right story and page from text read off a picture`() {
        val m = recognizeBook("Mia saw a red ball under a big tree.")
        assertEquals("mia-and-the-red-ball", m?.story?.id)
        assertEquals(0, m?.page)
    }

    @Test fun `returns null when nothing matches`() = assertNull(recognizeBook("zzz qqq"))

    @Test fun `finds a flashcard by its word`() {
        assertNotNull(recognizeFlashCard("this is mama"))
        assertEquals("mesa", recognizeFlashCard("  MESA\n table ")?.fil)
        assertNull(recognizeFlashCard("quarterly tax invoice"))
    }

    // --- scores
    @Test fun `English score uses confidence, Filipino uses accuracy`() {
        val words = listOf("Mia", "saw", "a", "ball")
        val heard = words.map(::normalizeWord).toSet()
        assertEquals(88, scoreReading(words, heard, 0.7, "en-US").score)
        assertEquals(100, scoreReading(words, heard, 0.7, "fil-PH").score)
        assertEquals(50, scoreReading(words, setOf("mia", "saw"), null, "fil-PH").score)
    }

    // --- learner level
    @Test fun `a new account is a Beginner`() = assertEquals("Beginner", learnerLevel(Account()).name)

    @Test fun `the sample account is Growing`() = assertEquals("Growing", learnerLevel(sampleAccount()).name)

    @Test fun `recommendations skip finished stories`() {
        val a = sampleAccount()
        val recs = recommendStories(a, learnerLevel(a), "Bilingual")
        assertTrue(recs.all { a.progress[it.story.id]?.finished != true })
    }

    // --- app state
    @Test fun `a new learner starts empty and progress is saved per account`() {
        val s = AppState(MemoryStore())
        s.login(User(firstName = "Kid", username = "kid", email = "kid@x.ph", role = "learner"))
        assertTrue(isNewReader(s.progress))
        s.openStory("the-lost-puppy")
        s.savePage("the-lost-puppy", 3)
        assertEquals(40, percentRead(storyById("the-lost-puppy")!!, s.progress["the-lost-puppy"]))
        s.finishStory("the-lost-puppy", 3)
        assertFalse(isNewReader(s.progress))
        s.logout()
        s.login(User(firstName = "Other", username = "other", email = "other@x.ph", role = "learner"))
        assertTrue(isNewReader(s.progress))
    }

    @Test fun `hearts, history and own stories work and survive a restart`() {
        val store = MemoryStore()
        val s = AppState(store)
        s.login(User(firstName = "Sam", username = "sam", email = "sam@basakita.ph", role = "learner", returning = true))
        s.toggleSaved("the-lost-puppy")
        assertTrue(s.saved.contains("the-lost-puppy"))
        s.toggleSaved("the-lost-puppy")
        assertFalse(s.saved.contains("the-lost-puppy"))
        s.saveDraft(Draft(id = "d2", title = "Aso", language = "Filipino", texts = listOf("Si Mia ay may aso."), images = listOf("")))
        val again = AppState(store)
        assertEquals("sam@basakita.ph", again.user?.email)
        assertTrue(again.drafts.any { it.id == "d2" })
        assertEquals("mia-and-the-red-ball", continueId(again.progress))
    }
}

package ph.basakita.app.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import ph.basakita.app.Lock
import ph.basakita.app.data.Story
import ph.basakita.app.data.stories

private const val KEY = "basa-kita:state:v2"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/** Where the app's state is saved. On the phone this is SharedPreferences; tests use memory. */
interface Store {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

class PrefsStore(context: Context) : Store {
    private val prefs = context.getSharedPreferences("basa-kita", Context.MODE_PRIVATE)
    override fun read(key: String): String? = prefs.getString(key, null)
    override fun write(key: String, value: String) = prefs.edit().putString(key, value).apply()
}

class MemoryStore : Store {
    val data = mutableMapOf<String, String>()
    override fun read(key: String): String? = data[key]
    override fun write(key: String, value: String) {
        data[key] = value
    }
}

private fun daysAgo(n: Double): Long = System.currentTimeMillis() - (n * 86_400_000).toLong()

/** Demo "returning student" so the regular home page can be shown. */
fun sampleAccount(): Account = Account(
    progress = mutableMapOf(
        "mia-and-the-red-ball" to Progress(page = 3, lastRead = daysAgo(0.1)),
        "the-magic-kite" to Progress(page = 1, lastRead = daysAgo(1.0)),
        "the-lost-puppy" to Progress(page = 5, finished = true, stars = 3, lastRead = daysAgo(2.0)),
        "ang-munting-puno" to Progress(page = 5, finished = true, stars = 3, lastRead = daysAgo(4.0)),
        "my-first-school-day" to Progress(page = 5, finished = true, stars = 2, lastRead = daysAgo(6.0)),
    ),
    lessonsDone = 5,
    saved = mutableListOf("the-magic-kite", "ang-munting-puno"),
    scores = mutableMapOf(
        "the-lost-puppy:1" to ScoreEntry(78, true),
        "the-lost-puppy:2" to ScoreEntry(64, true),
        "ang-munting-puno:1" to ScoreEntry(100, false),
        "ang-munting-puno:3" to ScoreEntry(86, false),
        "mia-and-the-red-ball:1" to ScoreEntry(72, true),
    ),
    quizzes = mutableMapOf(
        "the-lost-puppy" to QuizResult(3, 3),
        "ang-munting-puno" to QuizResult(2, 3),
        "my-first-school-day" to QuizResult(2, 3),
    ),
    drafts = mutableListOf(
        Draft(
            id = "draft-1",
            title = "Ang Pusa ni Sam",
            language = "Filipino",
            texts = listOf("Si Sam ay may pusa.", "Ang pusa ay puti at malambot.", "Mahal ni Sam ang kanyang pusa."),
        ),
    ),
    sample = true,
)

/**
 * The app's single source of truth: signed-in user, settings and each learner's progress,
 * saved on the phone. (The backend will keep this data later.)
 *
 * Screens read it like plain properties; every read goes through [revision], so Compose
 * redraws whatever used the state after each change.
 */
class AppState(private val store: Store) {
    private var revision by mutableIntStateOf(0)

    private var _user: User? = null
    private var _avatarColor: Long = 0xFFFFB27A
    private var _settings = Settings()
    private val _accounts = mutableMapOf<String, Account>()

    val user: User? get() = revision.let { _user }
    val avatarColor: Long get() = revision.let { _avatarColor }
    val settings: Settings get() = revision.let { _settings }
    val accounts: Map<String, Account> get() = revision.let { _accounts }

    init {
        store.read(KEY)?.let { raw ->
            try {
                val s = json.decodeFromString(SavedState.serializer(), raw)
                _user = s.user
                _avatarColor = s.avatarColor
                _settings = s.settings
                _accounts.putAll(s.accounts)
            } catch (_: Exception) {
                // A corrupt save starts fresh instead of crashing the app.
            }
        }
    }

    private val accountKey: String
        get() = (_user?.email?.takeIf { it.isNotEmpty() } ?: _user?.username ?: "guest").lowercase()

    val account: Account get() = revision.let { _accounts[accountKey] ?: Account() }
    private fun mutable(): Account = _accounts.getOrPut(accountKey) { Account() }

    val progress: Map<String, Progress> get() = account.progress
    val lessonsDone: Int get() = account.lessonsDone
    val drafts: List<Draft> get() = account.drafts
    val saved: List<String> get() = account.saved
    val scores: Map<String, ScoreEntry> get() = account.scores
    val quizzes: Map<String, QuizResult> get() = account.quizzes
    val sampleData: Boolean get() = account.sample
    val isAdmin: Boolean get() = user?.role == "admin"
    val displayName: String get() = user?.firstName?.takeIf { it.isNotEmpty() } ?: "Reader"

    private fun save() {
        revision++
        store.write(KEY, json.encodeToString(SavedState.serializer(), SavedState(_user, _avatarColor, _settings, _accounts.toMap())))
    }

    // --- account
    fun login(u: User) {
        _user = u
        _accounts.getOrPut(accountKey) { if (u.returning) sampleAccount() else Account() }
        save()
    }

    fun logout() {
        Lock.relock()
        _user = null
        save()
    }

    fun updateUser(change: (User) -> Unit) {
        val u = _user ?: return
        change(u)
        save()
    }

    fun setAvatarColor(c: Long) {
        _avatarColor = c
        save()
    }

    fun updateSettings(change: (Settings) -> Unit) {
        change(_settings)
        save()
    }

    // --- reading
    /** Opening a story adds it to Reading History (and keeps the saved page). */
    fun openStory(id: String) {
        mutable().progress.getOrPut(id) { Progress() }.lastRead = System.currentTimeMillis()
        save()
    }

    fun savePage(id: String, page: Int) {
        val p = mutable().progress.getOrPut(id) { Progress() }
        p.page = page
        p.lastRead = System.currentTimeMillis()
        save()
    }

    fun finishStory(id: String, stars: Int) {
        val p = mutable().progress.getOrPut(id) { Progress() }
        p.finished = true
        p.stars = stars
        p.lastRead = System.currentTimeMillis()
        save()
    }

    fun toggleSaved(id: String) {
        val a = mutable()
        if (a.saved.contains(id)) a.saved.remove(id) else a.saved.add(0, id)
        save()
    }

    fun completeLesson() {
        mutable().lessonsDone += 1
        save()
    }

    fun recordScore(storyId: String, page: Int, score: Int, english: Boolean) {
        mutable().scores["$storyId:$page"] = ScoreEntry(score, english)
        save()
    }

    fun recordQuiz(storyId: String, correct: Int, total: Int) {
        mutable().quizzes[storyId] = QuizResult(correct, total)
        save()
    }

    fun saveDraft(d: Draft) {
        val a = mutable()
        a.drafts.removeAll { it.id == d.id }
        a.drafts.add(0, d)
        save()
    }

    fun deleteDraft(id: String) {
        mutable().drafts.removeAll { it.id == id }
        save()
    }
}

// --- helpers shared by screens

fun storyById(id: String): Story? = stories.firstOrNull { it.id == id }

/** How much of a story has been read, 0-100. */
fun percentRead(story: Story, p: Progress?): Int {
    if (p == null) return 0
    if (p.finished) return 100
    val page = p.page.coerceIn(1, story.pages.size)
    return Math.round((page - 1).toDouble() / story.pages.size * 100).toInt()
}

/** Story ids, most recently read first. */
fun historyIds(progress: Map<String, Progress>): List<String> =
    progress.entries.filter { it.value.lastRead != null }.sortedByDescending { it.value.lastRead }.map { it.key }

/** The story to pick up again: the most recent one that is not finished. */
fun continueId(progress: Map<String, Progress>): String? = historyIds(progress).firstOrNull { progress[it]?.finished != true }

/** A learner is "new" until they finish their first story. */
fun isNewReader(progress: Map<String, Progress>): Boolean = progress.values.none { it.finished }

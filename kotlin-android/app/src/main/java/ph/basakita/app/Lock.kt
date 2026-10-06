package ph.basakita.app

/**
 * Remembers which account has entered the parent PIN since the app opened.
 * Logging out locks Settings again, and so does closing the app.
 */
object Lock {
    private var unlockedFor: String? = null
    fun isUnlocked(email: String?): Boolean = !email.isNullOrEmpty() && unlockedFor == email
    fun unlock(email: String?) {
        unlockedFor = email
    }
    fun relock() {
        unlockedFor = null
    }
}

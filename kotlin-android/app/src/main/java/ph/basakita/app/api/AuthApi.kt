package ph.basakita.app.api

import kotlinx.coroutines.delay
import ph.basakita.app.state.User

/** A sign-in problem to show the user, e.g. "Please enter a valid email address." */
class AuthException(message: String) : Exception(message)

val emailPattern = Regex("^\\S+@\\S+\\.\\S+$")

/**
 * Account service. The backend decides each account's role ("learner" or "admin");
 * the app only reads `user.role` and opens the matching home screen.
 *
 * Demo stand-ins until a backend exists:
 *  - admin@basakita.ph signs in as an admin
 *  - sam@basakita.ph is a returning learner with sample reading history
 *  - any other email is a brand-new learner with no history
 *
 * To connect a server, replace the bodies below with HTTP calls that return the same [User].
 */
object AuthApi {
    private val adminEmails = listOf("admin@basakita.ph")
    private val returningEmails = listOf("sam@basakita.ph")

    private suspend fun pause() = delay(300)

    private fun niceName(handle: String): String {
        val first = handle.split(Regex("[._-]")).first()
        return if (first.isEmpty()) "Reader" else first.replaceFirstChar { it.uppercase() }
    }

    suspend fun signIn(email: String, password: String): User {
        val clean = email.trim().lowercase()
        if (clean.isEmpty() || password.isEmpty()) throw AuthException("Please enter your email and password.")
        if (!emailPattern.matches(clean)) throw AuthException("Please enter a valid email address.")
        pause()
        val handle = clean.substringBefore('@')
        if (clean in adminEmails) return User(firstName = "Ana", lastName = "Reyes", username = handle, email = clean, role = "admin")
        if (clean in returningEmails) return User(firstName = "Sam", lastName = "Dela Cruz", username = handle, email = clean, role = "learner", returning = true)
        return User(firstName = niceName(handle), username = handle, email = clean, role = "learner")
    }

    suspend fun signUp(name: String, username: String, email: String, password: String): User {
        pause()
        val parts = name.trim().split(Regex("\\s+"))
        return User(
            firstName = parts.first(),
            lastName = parts.drop(1).joinToString(" "),
            username = username,
            email = email.trim().lowercase(),
            role = "pending",
        )
    }

    /** Demo: any 6-digit code works. */
    suspend fun verifyEmail(user: User, code: String): User {
        pause()
        return user.copy(role = "learner")
    }

    suspend fun resendCode(email: String) = pause()
    suspend fun requestPasswordReset(email: String) = pause()
    suspend fun resetPassword(token: String?, password: String) = pause()
}

package ph.basakita.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import ph.basakita.app.services.Permissions
import ph.basakita.app.services.Speech
import ph.basakita.app.state.AppState
import ph.basakita.app.state.PrefsStore

class MainActivity : ComponentActivity() {
    /** The token from a password reset link (basakita://reset?token=...). */
    private val resetToken = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Permissions.register(this)
        Speech.init(this)
        handle(intent)
        val app = AppState(PrefsStore(this))
        setContent { BasaKitaApp(app, resetToken) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "basakita" && uri.host == "reset") resetToken.value = uri.getQueryParameter("token") ?: ""
    }
}

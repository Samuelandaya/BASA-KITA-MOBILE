package ph.basakita.app.services

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

/**
 * Asks for Android permissions (camera, microphone, notifications) from anywhere in the app.
 * MainActivity calls [register] once when it starts.
 */
object Permissions {
    private var launcher: ActivityResultLauncher<String>? = null
    private var waiting: CompletableDeferred<Boolean>? = null

    fun register(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            waiting?.complete(ok)
            waiting = null
        }
    }

    fun has(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** Returns true when the permission is (or becomes) granted. */
    suspend fun request(context: Context, permission: String): Boolean {
        if (has(context, permission)) return true
        val l = launcher ?: return false
        waiting?.complete(false)
        val d = CompletableDeferred<Boolean>()
        waiting = d
        l.launch(permission)
        return d.await()
    }
}

package ru.sapozhnikov.aiagent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.app.McpOAuthSessionManager
import javax.inject.Inject

/** Принимает OAuth redirect и завершает авторизацию MCP-сервера. */
@AndroidEntryPoint
internal class McpOAuthCallbackActivity : ComponentActivity() {

    @Inject
    lateinit var oauthSessionManager: McpOAuthSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        if (uri == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            oauthSessionManager.handleCallback(uri)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri: Uri = intent.data ?: return
        lifecycleScope.launch {
            oauthSessionManager.handleCallback(uri)
            finish()
        }
    }
}

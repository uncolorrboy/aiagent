package ru.sapozhnikov.aiagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.sapozhnikov.aiagent.navigation.NavigationStack
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiAgentTheme {
                NavigationStack(modifier = Modifier)
            }
        }
    }
}

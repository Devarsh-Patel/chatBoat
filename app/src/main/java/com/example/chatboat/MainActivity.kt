package com.example.chatboat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.chatboat.ui.auth.AuthScreen
import com.example.chatboat.ui.chat.ChatViewModel
import com.example.chatboat.ui.chat.ChatViewModelFactory
import com.example.chatboat.ui.main.MainScreen
import com.example.chatboat.ui.navigation.NavRoute
import com.example.chatboat.ui.theme.ChatBoatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatBoatTheme {
                ChatBoatApp()
            }
        }
    }
}

@Composable
fun ChatBoatApp() {
    val backStack = rememberNavBackStack(NavRoute.Auth)
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(context))

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
        entryProvider = { key ->
            when (key) {
                NavRoute.Auth -> NavEntry(key) {
                    AuthScreen(
                        onAuthenticated = dropUnlessResumed {
                            backStack.removeAt(backStack.size - 1)
                            backStack.add(NavRoute.Main)
                        }
                    )
                }

                NavRoute.Main -> NavEntry(key) {
                    MainScreen(
                        viewModel = chatViewModel,
                        onLogout = dropUnlessResumed {
                            backStack.removeAt(backStack.size - 1)
                            backStack.add(NavRoute.Auth)
                        }
                    )
                }

                else -> NavEntry(key) {
                    // Fallback
                }
            }
        }
    )
}

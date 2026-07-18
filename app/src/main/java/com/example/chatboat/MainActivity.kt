package com.example.chatboat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.chatboat.data.DataModule
import com.example.chatboat.data.session.SessionManager
import com.example.chatboat.ui.auth.AuthScreen
import com.example.chatboat.ui.auth.AuthViewModel
import com.example.chatboat.ui.auth.AuthViewModelFactory
import com.example.chatboat.ui.chat.ChatViewModel
import com.example.chatboat.ui.chat.ChatViewModelFactory
import com.example.chatboat.ui.main.MainScreen
import com.example.chatboat.ui.navigation.NavRoute
import com.example.chatboat.ui.theme.ChatBoatTheme
import com.example.chatboat.util.NetworkMonitor

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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val networkMonitor = remember { NetworkMonitor(context) }
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)
    
    if (isLoggedIn == null) {
        // Prevent flickering while session is loading from DataStore
        return
    }

    val backStack = rememberNavBackStack(if (isLoggedIn == true) NavRoute.Main else NavRoute.Auth)
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(context))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(DataModule.provideAuthRepository(), sessionManager, networkMonitor))

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
        entryProvider = { key ->
            when (key) {
                NavRoute.Auth -> NavEntry(key) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthenticated = dropUnlessResumed {
                            backStack.removeAt(backStack.size - 1)
                            backStack.add(NavRoute.Main)
                        }
                    )
                }

                NavRoute.Main -> NavEntry(key) {
                    MainScreen(
                        viewModel = chatViewModel,
                        sessionManager = sessionManager,
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

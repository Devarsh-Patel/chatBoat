package com.example.chatboat.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatboat.data.local.ChatMessageEntity
import com.example.chatboat.data.local.ChatSessionEntity
import com.example.chatboat.data.session.SessionManager
import com.example.chatboat.ui.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel,
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val sessions by viewModel.sessions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("chatBoat") },
                actions = {
                    IconButton(onClick = { viewModel.startNewChat("New Chat ${sessions.size + 1}") }) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "New Chat")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            sessionManager.clearSession()
                            onLogout()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        ListDetailPaneScaffold(
            modifier = Modifier.padding(innerPadding),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                SessionList(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    onSessionSelected = {
                        viewModel.selectSession(it.id)
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        }
                    },
                    onDeleteSession = { viewModel.deleteSession(it.id) }
                )
            },
            detailPane = {
                ChatDetail(
                    messages = messages,
                    isLoading = isLoading,
                    onSendMessage = { viewModel.sendMessage(it) }
                )
            }
        )
    }
}

@Composable
fun SessionList(
    sessions: List<ChatSessionEntity>,
    currentSessionId: Long?,
    onSessionSelected: (ChatSessionEntity) -> Unit,
    onDeleteSession: (ChatSessionEntity) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sessions) { session ->
            Card(
                onClick = { onSessionSelected(session) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (session.id == currentSessionId)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = session.title, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { onDeleteSession(session) }) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetail(
    messages: List<ChatMessageEntity>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit
) {
    var textState by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatMessageItem(message)
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask chatBoat...") },
                shape = MaterialTheme.shapes.extraLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textState.isNotBlank()) {
                        onSendMessage(textState)
                        textState = ""
                    }
                },
                enabled = !isLoading && textState.isNotBlank()
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

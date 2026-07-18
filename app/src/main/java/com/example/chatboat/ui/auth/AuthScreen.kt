package com.example.chatboat.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatboat.data.auth.AuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthenticated()
        }
    }

    BackHandler(enabled = uiState !is AuthUiState.Landing) {
        viewModel.goBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (uiState !is AuthUiState.Landing) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = uiState,
                label = "AuthFlow"
            ) { state ->
                when (state) {
                    is AuthUiState.Landing -> LandingContent(
                        onProviderSelect = { viewModel.selectProvider(it) }
                    )
                    is AuthUiState.InputIdentifier -> InputIdentifierContent(
                        provider = state.provider,
                        isLoading = isLoading,
                        error = error,
                        onSubmit = { viewModel.requestVerificationCode(it) }
                    )
                    is AuthUiState.Verification -> VerificationContent(
                        provider = state.provider,
                        identifier = state.identifier,
                        isLoading = isLoading,
                        error = error,
                        onVerify = { viewModel.verifyCode(it) }
                    )
                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun LandingContent(onProviderSelect: (AuthProvider) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "chatBoat",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your AI Search Companion",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        AuthButton(
            text = "Continue with Google",
            icon = Icons.Rounded.Email,
            onClick = { onProviderSelect(AuthProvider.GOOGLE) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthButton(
            text = "Continue with Apple",
            icon = Icons.Rounded.Lock,
            onClick = { onProviderSelect(AuthProvider.APPLE) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onProviderSelect(AuthProvider.PHONE) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Guest / Phone Login", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun InputIdentifierContent(
    provider: AuthProvider,
    isLoading: Boolean,
    error: String?,
    onSubmit: (String) -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(countries[0]) }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val title = when (provider) {
        AuthProvider.GOOGLE -> "Google Login"
        AuthProvider.APPLE -> "Apple Login"
        AuthProvider.PHONE -> "Phone Login"
    }

    val hint = when (provider) {
        AuthProvider.GOOGLE -> "Enter your Gmail"
        AuthProvider.APPLE -> "Enter your Apple-connected Email"
        AuthProvider.PHONE -> "Phone Number"
    }

    val keyboardType = if (provider == AuthProvider.PHONE) KeyboardType.Phone else KeyboardType.Email

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We'll send a verification code to your ${if (provider == AuthProvider.PHONE) "phone" else "email"}.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (provider == AuthProvider.PHONE) {
                OutlinedCard(
                    onClick = { showCountryPicker = true },
                    modifier = Modifier
                        .height(56.dp)
                        .padding(end = 8.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${selectedCountry.flag} ${selectedCountry.code}")
                    }
                }
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text(hint) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                isError = error != null,
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val fullIdentifier = if (provider == AuthProvider.PHONE) {
                    "${selectedCountry.code}$identifier"
                } else {
                    identifier
                }
                onSubmit(fullIdentifier)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = identifier.isNotBlank() && !isLoading,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Send Code")
        }
    }

    if (showCountryPicker) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredCountries = countries.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery)
        }

        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = { Text("Select Country") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text("Search country...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(filteredCountries) { country ->
                            ListItem(
                                headlineContent = { Text(country.name) },
                                leadingContent = { Text(country.flag) },
                                trailingContent = { Text(country.code) },
                                modifier = Modifier.clickable {
                                    selectedCountry = country
                                    showCountryPicker = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun VerificationContent(
    provider: AuthProvider,
    identifier: String,
    isLoading: Boolean,
    error: String?,
    onVerify: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Verify your account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter the 6-digit code sent to $identifier",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it },
            label = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error != null,
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onVerify(code) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = code.length == 6 && !isLoading,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Verify & Continue")
        }
    }
}

@Composable
fun AuthButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

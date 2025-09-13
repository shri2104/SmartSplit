package com.example.smartsplit.screens.Profile

import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.data.DarkModeViewModel
import com.google.firebase.auth.FirebaseAuth
import darkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmailScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    // Dark mode state
    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDark = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }

    // Dark mode colors
    val darkBackground = Color(0xFF121212)
    val darkCard = Color(0xFF1E1E1E)
    val darkText = Color(0xFFFFFFFF)
    val darkSecondaryText = Color(0xFFB3B3B3)
    val darkFieldBorder = Color.White

    val lightPrimaryColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), Color.White)
    )

    val darkGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), darkBackground)
    )

    val user by viewModel.user.observeAsState()
    var email by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        email = user?.email ?: ""
    }

    val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) darkText else lightPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val newEmail = email.trim()
                        viewModel.updateUserEmail(
                            newEmail,
                            onSuccess = {
                                dialogMessage = "Verification email has been sent to $newEmail.\nPlease verify before logging in again."
                                showDialog = true
                            },
                            onFailure = { e ->
                                dialogMessage = "Failed to send verification email: ${e.message}"
                                showDialog = true
                            }
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) lightPrimaryColor else lightPrimaryColor
                    )
                ) {
                    Text("Next", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isDark) Brush.linearGradient(
                        colors = listOf(darkBackground, darkBackground) // Solid dark background
                    ) else lightGradientBrush
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Update Email Address",
                color = if (isDark) darkText else Color.Black,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "This will change the email address you use\n" +
                        "to log in...",
                color = if (isDark) darkSecondaryText else Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                placeholder = {
                    Text(
                        "name@example.com",
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { email = "" }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = if (isDark) darkText else lightPrimaryColor
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.1f),
                    cursorColor = if (isDark) darkText else lightPrimaryColor,
                    focusedTextColor = if (isDark) darkText else Color.Black,
                    unfocusedTextColor = if (isDark) darkText else Color.Black
                )
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Email Update",
                    color = if (isDark) darkText else Color.Black
                )
            },
            text = {
                Text(
                    dialogMessage,
                    color = if (isDark) darkText else Color.Black
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (dialogMessage.startsWith("Verification email")) {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("Welcomscreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("OK", color = if (isDark) darkText else lightPrimaryColor)
                }
            },
            containerColor = if (isDark) darkCard else Color.White
        )
    }
}
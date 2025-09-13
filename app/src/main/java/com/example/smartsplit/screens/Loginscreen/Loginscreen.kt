package com.example.smartsplit.screens.Loginscreen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.data.DarkModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loading by viewModel.loading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current



    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDark = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
    val primaryColor = Color(0xFF2196F3)
    val accentColor = primaryColor
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val darkBackground = Color.Black
    val darkText = Color.White
    val darkFieldBorder = Color.White
    val darkButtonBg = Color.White
    val darkButtonText = Color.Black

    // Control animation visibility
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) showErrorDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDark) {
                    Modifier.background(color = darkBackground)
                } else {
                    Modifier.background(brush = gradientBrush)
                }
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        val currentTextColor = if (isDark) darkText else accentColor

        // Back Arrow
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = currentTextColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Log in",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = currentTextColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Enter your email and password to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) darkText else Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Animate Email
        AnimatedVisibility(
            visible = animateIn,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Animate Password
        AnimatedVisibility(
            visible = animateIn,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = if (isDark) darkText else Color.Gray)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        // Forgot password (animate too if you want)
        AnimatedVisibility(
            visible = animateIn,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 })
        ) {
            Text(
                text = "Forgot your password?",
                color = currentTextColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        Log.d("Login", "Forgot password clicked for email=$email")
                        if (email.isNotBlank()) {
                            viewModel.resetPassword(email) { success ->
                                Toast.makeText(
                                    context,
                                    if (success) "Check your inbox to reset password" else "Failed to send reset email",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(context, "Enter your email first", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Log in Button with animation
        AnimatedVisibility(
            visible = animateIn,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it })
        ) {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.signInWithEmailAndPassword(email, password) {
                            navController.navigate("onboardscreen1?isSignup=false") {
                                popUpTo("loginscreen") { inclusive = true }
                            }
                        }
                    } else {
                        viewModel.clearError()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) darkButtonBg else accentColor
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (isDark) darkButtonText else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Log in",
                        color = if (isDark) darkButtonText else Color.White
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = if (isDark) Color.White else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

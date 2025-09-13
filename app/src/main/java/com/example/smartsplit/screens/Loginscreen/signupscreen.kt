package com.example.smartsplit.screens.Loginscreen


import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.data.DarkModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var currency by remember { mutableStateOf("INR (₹)") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    val showMoreFields = fullName.isNotEmpty()
    val context = LocalContext.current
    val loading by viewModel.loading.observeAsState(false)


    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDark = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
    // Light mode colors
    val primaryColor = Color(0xFF2196F3)
    val accentColor = primaryColor
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    // Dark mode colors
    val darkBackground = Color.Black
    val darkText = Color.White
    val darkFieldBorder = Color.White
    val darkButtonBg = Color.White
    val darkButtonText = Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDark) Modifier.background(color = darkBackground)
                else Modifier.background(brush = gradientBrush)
            )
            .padding(24.dp)
    ) {
        val textColor = if (isDark) darkText else accentColor
        val fieldBorderColor = if (isDark) darkFieldBorder else accentColor
        val buttonBg = if (isDark) darkButtonBg else accentColor
        val buttonText = if (isDark) darkButtonText else Color.White
        val darkBackground = Color.Black
        val darkText = Color.White
        val darkFieldBorder = Color.White
        val darkButtonBg = Color.White
        val darkButtonText = Color.Black
        // 🔙 Back Arrow
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // 📝 Title
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Sign up to get started with your expenses.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) darkText else Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // 👤 Full Name + Camera
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                    unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                    cursorColor = if (isDark) darkText else Color.Black
                )
            )

            Spacer(Modifier.width(8.dp))

            // 📷 Circle Camera Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDark) darkButtonBg else Color.White)
                    .clickable { Log.d("Signup", "Camera clicked for profile photo") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add Photo",
                    tint = if (isDark) darkButtonText else Color.Black
                )
            }
        }

        // ✨ Animate rest when full name is typed
        AnimatedVisibility(
            visible = showMoreFields,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                // Email
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

                Spacer(Modifier.height(12.dp))

                // Password
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

                Text(
                    text = "Must be at least 8 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) darkText else Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // 📱 Country code + Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = {},
                        label = { Text("Code") },
                        textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                            unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                            cursorColor = if (isDark) darkText else Color.Black
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                                phone = it
                                phoneError = false
                            }
                        },
                        label = { Text("Phone number") },
                        textStyle = LocalTextStyle.current.copy(color = if (isDark) darkText else Color.Black),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (isDark) darkFieldBorder else accentColor,
                            unfocusedBorderColor = if (isDark) darkFieldBorder else Color.Gray,
                            cursorColor = if (isDark) darkText else Color.Black
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

//                // 💰 Currency Selection
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "Your default currency is $currency.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = if (isDark) darkText else Color.Gray
//                    )
//                    Spacer(Modifier.width(4.dp))
//                    Text(
//                        text = "Change »",
//                        color = if (isDark) darkText else Color.Gray,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.clickable {
//                            Log.d("Signup", "Currency picker clicked")
//                        }
//                    )
//                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Sign up today to explore SmartSplit and easily manage shared expenses with friends.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (isDark) darkText else Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(16.dp))

                // ⚪ Sign Up Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.length >= 8) {
                            viewModel.createUserWithEmailAndPassword(
                                email = email,
                                password = password,
                                fullName = fullName,
                                phone = phone,
                                countryCode = countryCode,
                                currency = currency,
                                onWaitingForVerification = {
                                    Toast.makeText(
                                        context,
                                        "Check your inbox and verify your email to continue",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onVerified = {
                                    navController.navigate("onboardscreen1?isSignup=true")
                                },
                                onFailure = { errorMsg ->
                                    Toast.makeText(context, "Signup failed: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Enter valid email & password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !loading
                ) {
                    if (loading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = buttonText,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Waiting for verification...", color = buttonText)
                        }
                    } else {
                        Text("Sign Up", color = buttonText)
                    }
                }
            }
        }
    }
}

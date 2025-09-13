package com.example.smartsplit.screens.Profile

import accentColor
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.data.DarkModeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneNumberScreen(
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

    val lightPrimaryColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), Color.White)
    )

    val darkGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), darkBackground)
    )

    val user by viewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }

    var phoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        phoneNumber = user?.phone?.takeIf { it.isNotBlank() } ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) Brush.linearGradient(
                    colors = listOf(darkBackground, darkBackground) // Solid dark background
                ) else lightGradientBrush
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 48.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) darkText else lightPrimaryColor
                )
            }
        }

        // Profile icon
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = if (isDark) darkSecondaryText else Color.LightGray.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile",
                tint = if (isDark) darkText else Color.DarkGray,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Change Phone Number",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) darkText else Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phone number input
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            placeholder = {
                Text(
                    "Enter your phone number",
                    color = if (isDark) darkSecondaryText else Color.Gray
                )
            },
            trailingIcon = {
                if (phoneNumber.isNotEmpty()) {
                    IconButton(onClick = { phoneNumber = "" }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = if (isDark) darkText else lightPrimaryColor
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.1f),
                cursorColor = if (isDark) darkText else lightPrimaryColor,
                focusedTextColor = if (isDark) darkText else Color.Black,
                unfocusedTextColor = if (isDark) darkText else Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                val updated = user!!.copy(
                    phone = phoneNumber,
                )
                viewModel.updateUserData(updated)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) lightPrimaryColor else Color(0xFF1E88E5)
            )
        ) {
            Text("Save", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
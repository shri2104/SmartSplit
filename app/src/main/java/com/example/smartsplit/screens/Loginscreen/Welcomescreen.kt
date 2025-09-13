package com.example.smartsplit.screens.Loginscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smartsplit.R
import com.example.smartsplit.data.DarkModeViewModel

@Composable
fun Welcomscreen(
    navController: NavController,

) {
    val primaryColor = Color(0xFF2196F3)

    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDarkMode = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
    // Light mode gradient
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.White)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isDarkMode) Modifier.background(Color.Black)
                else Modifier.background(brush = gradientBrush)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.chatgpt_image_aug_22__2025__10_29_43_pm),
                contentDescription = "App Logo",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Sign up button
            Button(
                onClick = { navController.navigate("Signup") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color.White else primaryColor
                )
            ) {
                Text(
                    "Sign up",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.Black else Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log in button
            OutlinedButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, if (isDarkMode) Color.White else primaryColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkMode) Color.White else primaryColor
                )
            ) {
                Text(
                    "Log in",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google sign-in button
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, if (isDarkMode) Color.LightGray else Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDarkMode) Color.Black else Color.White,
                    contentColor = if (isDarkMode) Color.White else Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Sign in with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

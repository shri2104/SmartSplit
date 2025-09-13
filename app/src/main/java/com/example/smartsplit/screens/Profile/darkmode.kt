package com.example.smartsplit.screens.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smartsplit.data.DarkModeViewModel
import com.example.smartsplit.screens.Groups.accentColor

@Composable
fun DarkModeSettingsScreen(
    navController: NavController,
    viewModel: DarkModeViewModel = hiltViewModel()
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

    val options = listOf("Automatic", "On", "Off")
    val selectedOption by viewModel.darkModeLiveData.observeAsState("Automatic")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) darkBackground else Color.White)
            .padding(18.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.height(20.dp))
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) darkText else lightPrimaryColor
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
            ) {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = if (isDark) darkText else lightPrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Radio options
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) darkCard else Color(0xFF98bad5))
                    .clickable {
                        viewModel.setDarkMode(option)
                    }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = {
                        viewModel.setDarkMode(option)
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (isDark) darkText else Color(0xFF1976D2),
                        unselectedColor = if (isDark) darkSecondaryText else Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = option,
                    fontSize = 16.sp,
                    color = if (isDark) darkText else Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
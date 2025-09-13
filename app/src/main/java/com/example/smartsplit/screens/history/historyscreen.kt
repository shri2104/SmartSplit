package com.example.smartsplit.screens.history


import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.HistoryViewModel
import com.example.smartsplit.data.DarkModeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = viewModel()
) {
    val historyList by viewModel.history.collectAsState()

    // Dark mode toggle (later replace with isSystemInDarkTheme())
    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDark = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }

    // Colors
    val primaryColor = Color(0xFF2196F3)
    val accentColor = primaryColor
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val darkBackground = Color.Black
    val darkCardBg = Color(0xFF1E1E1E)
    val darkText = Color.White
    val darkSecondaryText = Color.LightGray
    val darkNavBar = Color(0xFF121212)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = if (isDark) darkNavBar else Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("Group") },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups", color = if (isDark) darkText else Color.Black) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("friends") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Friends") },
                    label = { Text("Friends", color = if (isDark) darkText else Color.Black) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Activity") },
                    label = { Text("History", color = if (isDark) darkText else accentColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account", color = if (isDark) darkText else Color.Black) }
                )
            }
        },
        containerColor = if (isDark) darkBackground else Color(0xFFE6F2FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (isDark) Modifier.background(darkBackground)
                    else Modifier.background(gradientBrush)
                )
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) darkText else accentColor,
                        modifier = Modifier.padding(start = 7.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = if (isDark) darkText else accentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 17.dp)
                )
            }

            // History List
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (historyList.isEmpty()) {
                    item {
                        Text(
                            text = "No activity yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) darkSecondaryText else Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(historyList) { historyItem ->
                        HistoryItemCard(historyItem, isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryItem, isDark: Boolean) {
    val icon = when (item.type) {
        ActionType.ADD -> Icons.Default.AttachMoney
        ActionType.DELETE -> Icons.Default.Delete
        ActionType.UPDATE -> Icons.Default.Restore
        ActionType.CREATE -> Icons.Default.Group
    }

    val bgColor = when (item.type) {
        ActionType.ADD -> if (isDark) Color(0xFF1565C0) else Color(0xFFBBDEFB)
        ActionType.DELETE -> if (isDark) Color(0xFFD32F2F) else Color(0xFFFFCDD2)
        ActionType.UPDATE -> if (isDark) Color(0xFFFBC02D) else Color(0xFFFFF9C4)
        ActionType.CREATE -> if (isDark) Color(0xFF388E3C) else Color(0xFFC8E6C9)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isDark) Color.White else Color.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.LightGray else Color.Gray
                )
                Text(
                    item.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.Gray else Color.DarkGray
                )
            }
        }
    }
}

data class HistoryItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: ActionType = ActionType.CREATE
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(timestamp))
    }
}

enum class ActionType { ADD, DELETE, UPDATE, CREATE }


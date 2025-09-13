package com.example.smartsplit.screens.Friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.data.DarkModeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

// Define dark mode colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF90CAF9)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnPrimary = Color(0xFF000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController,
                  viewModel: FriendsViewModel = viewModel(),
                  ) {
    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDarkMode = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }

    val primaryColor = if (isDarkMode) DarkPrimary else Color(0xFF2196F3)
    val backgroundColor = if (isDarkMode) DarkBackground else Color.White
    val surfaceColor = if (isDarkMode) DarkSurface else Color.White
    val onSurfaceColor = if (isDarkMode) DarkOnSurface else Color.Black
    val onPrimaryColor = if (isDarkMode) DarkOnPrimary else Color.White

    val gradientBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.15f),
                DarkBackground
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.15f),
                Color.White
            )
        )
    }

    val expenseViewModel: ExpenseViewModel = viewModel()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val friendBalances by expenseViewModel.friendBalances.observeAsState(emptyList())

    // Add debug logging
    LaunchedEffect(friendBalances) {
        println("DEBUG: UI Friend balances updated: ${friendBalances.size} items")
        friendBalances.forEach { balance ->
            println("DEBUG: UI Friend ${balance.friendName} - Balance: ${balance.totalBalance}")
        }
    }

    val friends by viewModel.friends.collectAsState()

    // Add more debug logging
    LaunchedEffect(currentUserId) {
        println("DEBUG: Current user ID: $currentUserId")
        expenseViewModel.fetchFriendBalances(currentUserId)
        viewModel.fetchFriends(currentUserId)
        expenseViewModel.testFirestoreConnection()
    }

    // Log friends list changes
    LaunchedEffect(friends) {
        println("DEBUG: Friends list updated: ${friends.size} friends")
        friends.forEach { friend ->
            println("DEBUG: Friend: ${friend.name} (${friend.uid})")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkMode) DarkSurface else Color.White
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("group") },
                    icon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Groups",
                            tint = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    },
                    label = {
                        Text(
                            "Groups",
                            color = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Friends",
                            tint = primaryColor
                        )
                    },
                    label = {
                        Text(
                            "Friends",
                            color = primaryColor
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history")},
                    icon = {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "History",
                            tint = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    },
                    label = {
                        Text(
                            "History",
                            color = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile")},
                    icon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    },
                    label = {
                        Text(
                            "Account",
                            color = if (isDarkMode) DarkOnSurface else Color.Black
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addFriend") },
                containerColor = primaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add friend", tint = onPrimaryColor)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isDarkMode) {
                        Modifier.background(DarkBackground)
                    } else {
                        Modifier.background(gradientBrush)
                    }
                )
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Debug info panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor,
                        modifier = Modifier.padding(start = 7.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 17.dp)
                )
            }
            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(12.dp))

            if (friends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "No friends",
                            tint = primaryColor,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No friends yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isDarkMode) DarkOnSurface else Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "But ${friendBalances.size} balance entries found",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) DarkOnSurface.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                }
            } else {
                if (friendBalances.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "No balances",
                                tint = if (isDarkMode) DarkOnSurface.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No expense balances yet",
                                color = if (isDarkMode) DarkOnSurface.copy(alpha = 0.7f) else Color.Gray
                            )
                            Text(
                                text = "Add expenses with friends to see balances",
                                color = if (isDarkMode) DarkOnSurface.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(friendBalances) { balance ->
                            FriendBalanceCard(
                                friendID = balance.friendName,
                                balance = balance.totalBalance,
                                primaryColor = primaryColor,
                                surfaceColor = surfaceColor,
                                onSurfaceColor = onSurfaceColor,
                                isDarkMode = isDarkMode,
                                onSettle = {
                                    println("DEBUG: Settle clicked for ${balance.friendName}")
                                },
                                onClick = {
                                    println("DEBUG: Friend clicked: ${balance.friendName}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendBalanceCard(
    friendID: String,
    balance: Double,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    isDarkMode: Boolean,
    onSettle: () -> Unit,
    onClick: () -> Unit,

) {
    val groupViewModel: GroupViewModel = viewModel()

    // State to hold the friend's name
    var friendName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(friendID) {
        groupViewModel.getUserNameFromUid1(friendID) { name ->
            friendName = name
        }
    }

    val amountText = when {
        abs(balance) < 0.01 -> "All settled up"
        balance > 0 -> "Owes you ₹${"%.2f".format(balance)}"
        else -> "You owe ₹${"%.2f".format(-balance)}"
    }

    val amountColor = when {
        abs(balance) < 0.01 -> if (isDarkMode) Color.LightGray else Color.Gray
        balance > 0 -> Color(0xFF4CAF50) // Green
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp) // 👈 space between cards
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar/Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(primaryColor.copy(alpha = if (isDarkMode) 0.3f else 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
            }

            Spacer(Modifier.width(16.dp))

            // Friend info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friendName ?: friendID, // 👈 fallback to ID if name not fetched yet
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = amountColor
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    navController: NavController,
    viewModel: FriendsViewModel = viewModel()
) {
    // Check if dark mode is enabled
    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDarkMode = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }

    // Choose colors based on mode
    val primaryColor = if (isDarkMode) DarkPrimary else Color(0xFF2196F3)
    val backgroundColor = if (isDarkMode) DarkBackground else Color.White
    val surfaceColor = if (isDarkMode) DarkSurface else Color.White
    val onSurfaceColor = if (isDarkMode) DarkOnSurface else Color.Black
    val onPrimaryColor = if (isDarkMode) DarkOnPrimary else Color.White

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val gradientBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.15f),
                DarkBackground
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.15f),
                Color.White
            )
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isDarkMode) {
                        Modifier.background(DarkBackground)
                    } else {
                        Modifier.background(gradientBrush)
                    }
                )
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(5.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Add Friends",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        "Friend's Email ID",
                        color = if (isDarkMode) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray
                    )
                },
                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedLabelColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedBorderColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    containerColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = primaryColor
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.sendFriendRequest(email) { success, msg ->
                        message = msg
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                )
            ) {
                Text("Send Request")
            }
            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    it,
                    color = if (it.contains("success")) Color.Green else Color.Red
                )
            }
        }
    }
}
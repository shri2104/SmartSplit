package com.example.smartsplit.screens.Homescreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.data.DarkModeViewModel
import com.example.smartsplit.screens.Friends.DarkOnSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSectionScreen(
    navController: NavHostController,
    viewModel: GroupViewModel = viewModel()
) {
    val myGroups by viewModel.myGroups.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Dark mode state (you can replace this with your app's theme state)
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
    val darkSurface = Color(0xFF1E1E1E)
    val darkText = Color(0xFFFFFFFF)
    val darkSecondaryText = Color(0xFFB0B0B0)
    val darkPrimary = Color(0xFFBB86FC)
    val darkSecondary = Color(0xFF03DAC6)
    val darkCard = Color(0xFF2D2D2D)
    val darkFieldBorder = Color.White
    val darkButtonBg = Color.White
    val darkButtonText = Color.Black

    // Light mode colors
    val lightPrimary = Color(0xFF0077CC)
    val lightBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE6F2FF),
            Color(0xFFCCE5FF)
        )
    )
    val lightCard = Color.White
    val lightText = Color(0xFF222222)
    val lightSecondaryText = Color.Gray

    // Current theme colors
    val primaryColor = if (isDark) Color.White else lightPrimary
    val backgroundColor = if (isDark) darkBackground else lightBackground
    val cardColor = if (isDark) darkCard else lightCard
    val textColor = if (isDark) darkText else lightText
    val secondaryTextColor = if (isDark) darkSecondaryText else lightSecondaryText
    val navBarColor = if (isDark) darkSurface else Color.White
    val fabColor = if (isDark) darkPrimary else lightPrimary

    LaunchedEffect(Unit) {
        viewModel.fetchMyGroups()
    }

    // Filter groups based on search query
    val filteredGroups = myGroups.filter { group ->
        searchQuery.isEmpty() ||
                group.name.contains(searchQuery, ignoreCase = true) ||
                group.createdBy.contains(searchQuery, ignoreCase = true) ||
                group.type.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            if (isSearching) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCloseSearch = {
                        isSearching = false
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester,
                    isDark = isDark,
                    textColor = textColor,
                    primaryColor = primaryColor,
                    fieldBorderColor = if (isDark) darkFieldBorder else primaryColor
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "SmartSplit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            isSearching = true
                            coroutineScope.launch {
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = primaryColor
                            )
                        }
                        IconButton(onClick = { navController.navigate("creategroup") }) {
                            Icon(
                                imageVector = Icons.Filled.Group,
                                contentDescription = "addgroup",
                                tint = primaryColor
                            )
                        }
                        IconButton(onClick = { navController.navigate("notification") }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "addgroup",
                                tint = primaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = navBarColor) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = "Groups",
                            tint = if (isDark) darkText else primaryColor
                        )
                    },
                    label = {
                        Text(
                            "Groups",
                            color = if (isDark) darkText else primaryColor
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("friends") },
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Friends",
                            tint = if (isDark) darkSecondaryText else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Friends",
                            color = if (isDark) darkSecondaryText else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("grocery") }, // Create a GroceryScreen route
                    icon = {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Grocery",
                            tint = if (isDark) darkSecondaryText else primaryColor
                        )
                    },
                    label = {
                        Text(
                            "Grocery",
                            color = if (isDark) darkSecondaryText else primaryColor
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history") },
                    icon = {
                        Icon(
                            Icons.Filled.List,
                            contentDescription = "Activity",
                            tint = if (isDark) darkSecondaryText else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "History",
                            color = if (isDark) darkSecondaryText else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Account",
                            tint = if (isDark) darkSecondaryText else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Account",
                            color = if (isDark) darkSecondaryText else Color.Gray
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            Column (
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("smartsplitai") },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "SmartSplit AI",
                        tint = if (isDark) darkButtonText else Color.Black
                    )
                }
                FloatingActionButton(
                    onClick = { navController.navigate("addexpense") },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Expense",
                        tint = if (isDark) darkButtonText else Color.Black
                    )
                }
            }
        }

    ) { innerPadding ->
        if (filteredGroups.isEmpty() && searchQuery.isNotEmpty()) {
            // Show no results found when searching
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDark) {
                            Modifier.background(darkBackground)
                        } else {
                            Modifier.background(brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE6F2FF),
                                    Color(0xFFCCE5FF)
                                )
                            ))
                        }
                    )
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No groups found",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No groups match your search for \"$searchQuery\"",
                    fontSize = 16.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 20.sp
                )
            }
        } else if (filteredGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDark) {
                            Modifier.background(darkBackground)
                        } else {
                            Modifier.background(brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE6F2FF),
                                    Color(0xFFCCE5FF)
                                )
                            ))
                        }
                    )
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No groups yet",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first group to split expenses with friends!",
                    fontSize = 16.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("creategroup") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = if (isDark) darkButtonText else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Create Group", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("addFriend")},
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text(text = "Invite Friends", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "💡 Tip: Use groups to manage trips, events, and shared expenses.",
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDark) {
                            Modifier.background(darkBackground)
                        } else {
                            Modifier.background(brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE6F2FF),
                                    Color(0xFFCCE5FF)
                                )
                            ))
                        }
                    )
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                items(filteredGroups) { group ->
                    GroupCard(
                        group = group,
                        onClick = {
                            navController.navigate("GroupOverview/${group.id}")
                        },
                        isDark = isDark,
                        cardColor = cardColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    focusRequester: FocusRequester,
    isDark: Boolean,
    textColor: Color,
    primaryColor: Color,
    fieldBorderColor: Color
) {
    val focusManager = LocalFocusManager.current
    val onSurfaceColor = if (isDark) DarkOnSurface else Color.Black
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Find groups by name, admin, or type",
                        color = if (isDark) textColor.copy(alpha = 0.7f) else Color.Gray
                    )
                },
                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    containerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    isDark: Boolean,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    primaryColor: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = if (isDark) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (group.type.lowercase()) {
                    "travel" -> Icons.Default.Flight
                    "work" -> Icons.Default.Work
                    "friends" -> Icons.Default.Person
                    "family" -> Icons.Default.Home
                    else -> Icons.Default.List
                }

                Icon(
                    imageVector = icon,
                    contentDescription = group.type,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.type,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Admin : ${group.createdBy}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Go to group",
                tint = secondaryTextColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
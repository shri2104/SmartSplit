package com.example.smartsplit.screens.Groups

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ActivityTypes
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.Friend
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupMember
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.Viewmodel.logActivity
import com.example.smartsplit.data.DarkModeViewModel
import com.google.firebase.auth.FirebaseAuth
import darkBackground
import kotlin.math.abs

val primaryColor = Color(0xFF2196F3)
val accentColor = Color(0xFF2196F3)
val gradientBrush = Brush.verticalGradient(
    colors = listOf(
        primaryColor.copy(alpha = 0.15f),
        Color.White
    )
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController? = null,
    friendsViewModel: FriendsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
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
    val DarkSurface = Color(0xFFFFFFFF)
    val onSurfaceColor = if (isDark) DarkSurface else Color.White
    // Dark mode colors
    val darkBackground = Color(0xFF121212)
    val darkCard = Color(0xFF1E1E1E)
    val darkText = Color(0xFFFFFFFF)
    val darkSecondaryText = Color(0xFFB3B3B3)
    val darkFieldBorder = Color.White

    val lightPrimaryColor = Color(0xFF2196F3)
    val lightAccentColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), Color.White)
    )

    val darkGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), darkBackground)
    )

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var splitBy by remember { mutableStateOf("Equally") }

    var paidByUid by remember { mutableStateOf(currentUserId) }
    var paidByLabel by remember { mutableStateOf("You") }

    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    var showWithDialog by remember { mutableStateOf(false) }
    var showPaidByDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }

    val friends by friendsViewModel.friends.collectAsState()
    val groups by groupViewModel.myGroups.observeAsState(emptyList())

    LaunchedEffect(currentUserId) {
        friendsViewModel.fetchFriends(currentUserId)
        groupViewModel.fetchMyGroups()
    }

    val groupMembers by groupViewModel.groupMembers.observeAsState(emptyList())
    LaunchedEffect(selectedGroup) {
        selectedGroup?.let { groupViewModel.fetchGroupMembers(it.id) }
    }

    var splitInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var showSaveBtn by remember { mutableStateOf(false) }
    LaunchedEffect(description, amount, selectedFriend, selectedGroup) {
        showSaveBtn = description.isNotBlank() &&
                amount.isNotBlank() &&
                (selectedFriend != null || selectedGroup != null)
    }

    // Reset split inputs when selection changes
    LaunchedEffect(selectedFriend, selectedGroup, splitBy) {
        splitInputs = emptyMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) Brush.linearGradient(
                    colors = listOf(darkBackground, darkBackground) // Solid dark background
                ) else lightGradientBrush
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) darkText else lightPrimaryColor
                )
            }

        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = if (isDark) darkText else lightPrimaryColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // With who?
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "With you and:",
                color = if (isDark) darkText else Color.Black
            )
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = { showWithDialog = true },
                label = {
                    Text(
                        when {
                            selectedGroup != null -> "Group: ${selectedGroup!!.name}"
                            selectedFriend != null -> "Friend: ${selectedFriend!!.name}"
                            else -> "Choose"
                        },
                        color = if (isDark) darkText else Color.Black
                    )
                },
                leadingIcon = {
                    Icon(
                        if (selectedGroup != null) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDark) darkText else lightPrimaryColor
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isDark) darkCard else Color.White
                )
            )
        }

        Spacer(Modifier.height(16.dp))


        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor,
                containerColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    amount = it
                }
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor,
                containerColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = { showPaidByDialog = true },
                label = {
                    Text(
                        "Paid by: $paidByLabel",
                        color = if (isDark) darkText else Color.Black
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isDark) darkCard else Color.White
                )
            )
            AssistChip(
                onClick = { showSplitDialog = true },
                label = {
                    Text(
                        "Split: $splitBy",
                        color = if (isDark) darkText else Color.Black
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isDark) darkCard else Color.White
                )
            )
        }

        // Show split summary
        if (showSaveBtn && (splitBy == "By shares" || splitBy == "By percentage")) {
            Spacer(Modifier.height(16.dp))
            val members = when {
                selectedGroup != null -> groupMembers
                selectedFriend != null -> listOf(
                    GroupMember(uid = currentUserId, email = "You", accepted = true),
                    GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
                )
                else -> emptyList()
            }

            val amt = amount.toDoubleOrNull() ?: 0.0
            Column {
                Text(
                    "Split Summary:",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) darkText else lightPrimaryColor
                )
                Spacer(Modifier.height(8.dp))
                members.forEach { member ->
                    val share = when (splitBy) {
                        "By percentage" -> {
                            val percent = splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0
                            amt * percent / 100
                        }
                        "By shares" -> {
                            val shareVal = splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0
                            val totalShares = members.sumOf { splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0 }
                            if (totalShares > 0) amt * shareVal / totalShares else 0.0
                        }
                        else -> 0.0
                    }
                    Text(
                        "${if (member.uid == currentUserId) "You" else member.email}: ₹${"%.2f".format(share)}",
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = showSaveBtn) {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: run {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedFriend != null) {
                        // Handle friend expense
                        val splits = calculateFriendSplits(
                            amount = amt,
                            splitBy = splitBy,
                            currentUserId = currentUserId,
                            friendId = selectedFriend!!.uid,
                            splitInputs = splitInputs
                        )

                        if (splits != null) {
                            expenseViewModel.addFriendExpense(
                                description = description,
                                amount = amt,
                                paidBy = paidByUid,
                                friendId = selectedFriend!!.uid,
                                splits = splits
                            )

                            // Log friend expense activity
                            logActivity(
                                type = ActivityTypes.EXPENSE_ADDED,
                                description = "Added expense with ${selectedFriend!!.name}: $description - $${amt}",
                                userId = currentUserId
                            )

                            Toast.makeText(context, "Expense added with friend!", Toast.LENGTH_SHORT).show()
                            navController?.popBackStack()
                        }
                    } else if (selectedGroup != null) {
                        // Handle group expense
                        val membersList = groupMembers
                        val splits = calculateGroupSplits(
                            amount = amt,
                            splitBy = splitBy,
                            members = membersList,
                            splitInputs = splitInputs
                        )

                        if (splits != null) {
                            expenseViewModel.addExpense(
                                description = description,
                                amount = amt,
                                paidBy = paidByUid,
                                splitBy = splitBy,
                                members = membersList,
                                groupId = selectedGroup?.id,
                                splitInputs = splitInputs
                            )
                            logActivity(
                                type = ActivityTypes.EXPENSE_ADDED,
                                description = "Added group expense: $description - $${amt}",
                                relatedGroupId = selectedGroup?.id,
                                userId = currentUserId
                            )

                            Toast.makeText(context, "Group expense added!", Toast.LENGTH_SHORT).show()
                            navController?.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) lightPrimaryColor else lightAccentColor
                )
            ) {
                Text("Save Expense", color = Color.White)
            }
        }
    }

    // Friend or Group dialog
    if (showWithDialog) {
        SelectFriendOrGroupScreen(
            friends = friends,
            groups = groups,
            onFriendSelected = { friend ->
                selectedFriend = friend
                selectedGroup = null
                paidByUid = currentUserId
                paidByLabel = "You"
                showWithDialog = false
            },
            onGroupSelected = { group ->
                selectedGroup = group
                selectedFriend = null
                paidByUid = currentUserId
                paidByLabel = "You"
                showWithDialog = false
            },
            onClose = { showWithDialog = false },
            isDark = isDark,
            darkCard = darkCard,
            darkText = darkText,
            darkSecondaryText = darkSecondaryText
        )
    }

    // Paid By dialog
    if (showPaidByDialog) {
        val members = when {
            selectedGroup != null -> groupMembers
            selectedFriend != null -> listOf(
                GroupMember(uid = currentUserId, email = "You", accepted = true),
                GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
            )
            else -> emptyList()
        }

        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = {
                Text(
                    "Select Payer",
                    color = if (isDark) darkText else Color.Black
                )
            },
            text = {
                Column {
                    members.forEach { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    paidByUid = member.uid
                                    paidByLabel = if (member.uid == currentUserId) "You" else (member.name ?: "Friend")
                                    showPaidByDialog = false
                                }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (paidByUid == member.uid)
                                    lightPrimaryColor.copy(alpha = 0.1f)
                                else if (isDark) darkCard else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isDark) darkText else lightPrimaryColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (member.uid == currentUserId) "You" else (member.name ?: "Friend"),
                                    color = if (isDark) darkText else Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaidByDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) lightPrimaryColor else lightAccentColor
                    )
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = if (isDark) darkCard else Color.White
        )
    }

    // Split dialog
    if (showSplitDialog) {
        val members = when {
            selectedGroup != null -> groupMembers
            selectedFriend != null -> listOf(
                GroupMember(uid = currentUserId, email = "You", accepted = true),
                GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
            )
            else -> emptyList()
        }

        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = {
                Text(
                    "Select Split Method",
                    color = if (isDark) darkText else Color.Black
                )
            },
            text = {
                Column {
                    // Split method selection
                    listOf("Equally", "By shares", "By percentage").forEach { method ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    splitBy = method
                                    if (method == "Equally") {
                                        showSplitDialog = false
                                    }
                                }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (splitBy == method)
                                    lightPrimaryColor.copy(alpha = 0.1f)
                                else if (isDark) darkCard else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (method) {
                                        "By shares" -> Icons.Default.PieChart
                                        "By percentage" -> Icons.Default.Percent
                                        else -> Icons.Default.Equalizer
                                    },
                                    contentDescription = null,
                                    tint = if (isDark) darkText else lightPrimaryColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    method,
                                    color = if (isDark) darkText else Color.Black
                                )
                            }
                        }
                    }

                    // Split inputs
                    if (splitBy == "By shares" || splitBy == "By percentage") {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (splitBy == "By shares") "Enter shares:" else "Enter percentages:",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) darkText else Color.Black
                        )

                        members.forEach { member ->
                            OutlinedTextField(
                                value = splitInputs[member.uid] ?: "",
                                onValueChange = { value ->
                                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        splitInputs = splitInputs.toMutableMap().apply {
                                            put(member.uid, value)
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        if (member.uid == currentUserId) "You" else "Friend",
                                        color = if (isDark) darkSecondaryText else Color.Gray
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                                    unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor,
                                    containerColor = Color.Transparent
                                )
                            )
                        }

                        // Validation message
                        if (splitBy == "By percentage") {
                            val total = members.sumOf { splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0 }
                            Text(
                                "Total: ${"%.1f".format(total)}%",
                                color = if (abs(total - 100.0) < 0.1) Color.Green else Color.Red
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = { showSplitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) lightPrimaryColor else lightAccentColor
                        )
                    ) {
                        Text("Done", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showSplitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) darkSecondaryText else Color.Gray
                        )
                    ) {
                        Text("Cancel", color = if (isDark) darkText else Color.White)
                    }
                }
            },
            containerColor = if (isDark) darkCard else Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendOrGroupScreen(
    friends: List<Friend>,
    groups: List<Group>,
    onFriendSelected: (Friend) -> Unit,
    onGroupSelected: (Group) -> Unit,
    onClose: () -> Unit,
    isDark: Boolean = false,
    darkCard: Color = Color(0xFF1E1E1E),
    darkText: Color = Color.White,
    darkSecondaryText: Color = Color(0xFFB3B3B3)
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            friends.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }
    val filteredGroups = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            groups.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Friend or Group",
                        color = if (isDark) darkText else Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) darkText else Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) darkCard else Color.White
                )
            )
        },
        containerColor = if (isDark) darkCard else Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 🔍 Styled Search Input
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        "Search friends or groups",
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = if (isDark) darkText else Color.Black
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = if (isDark) darkCard else Color.White.copy(alpha = 0.1f),
                    cursorColor = if (isDark) darkText else Color.Black,
                    focusedTextColor = if (isDark) darkText else Color.Black,
                    unfocusedTextColor = if (isDark) darkText else Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFriends.isEmpty() && filteredGroups.isEmpty() && searchQuery.isNotBlank()) {
                Text(
                    "No results found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) darkSecondaryText else Color.Gray
                )
            }

            LazyColumn {
                if (filteredFriends.isNotEmpty()) {
                    item {
                        Text(
                            "Friends",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) darkText else Color.Black
                        )
                    }
                    items(filteredFriends) { friend ->
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isDark) darkText else Color.Black
                                )
                            },
                            headlineContent = {
                                Text(
                                    friend.name,
                                    color = if (isDark) darkText else Color.Black
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFriendSelected(friend) }
                        )
                    }
                }

                if (filteredGroups.isNotEmpty()) {
                    item {
                        Text(
                            "Groups",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) darkText else Color.Black
                        )
                    }
                    items(filteredGroups) { group ->
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    tint = if (isDark) darkText else Color.Black
                                )
                            },
                            headlineContent = {
                                Text(
                                    group.name,
                                    color = if (isDark) darkText else Color.Black
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupSelected(group) }
                        )
                    }
                }
            }
        }
    }
}

private fun calculateFriendSplits(
    amount: Double,
    splitBy: String,
    currentUserId: String,
    friendId: String,
    splitInputs: Map<String, String>
): Map<String, Double>? {
    return when (splitBy) {
        "Equally" -> {
            val share = amount / 2
            mapOf(
                currentUserId to share,
                friendId to share
            )
        }
        "By percentage" -> {
            val userPercent = splitInputs[currentUserId]?.toDoubleOrNull() ?: 0.0
            val friendPercent = splitInputs[friendId]?.toDoubleOrNull() ?: 0.0

            if (abs(userPercent + friendPercent - 100.0) > 0.1) {
                return null // Invalid percentages
            }

            mapOf(
                currentUserId to (amount * userPercent / 100),
                friendId to (amount * friendPercent / 100)
            )
        }
        "By shares" -> {
            val userShare = splitInputs[currentUserId]?.toDoubleOrNull() ?: 0.0
            val friendShare = splitInputs[friendId]?.toDoubleOrNull() ?: 0.0
            val totalShares = userShare + friendShare

            if (totalShares <= 0) {
                return null // Invalid shares
            }

            mapOf(
                currentUserId to (amount * userShare / totalShares),
                friendId to (amount * friendShare / totalShares)
            )
        }
        else -> null
    }
}

private fun calculateGroupSplits(
    amount: Double,
    splitBy: String,
    members: List<GroupMember>,
    splitInputs: Map<String, String>
): Map<String, Double>? {
    return when (splitBy) {
        "Equally" -> {
            val share = amount / members.size
            members.associate { it.uid to share }
        }
        "By percentage" -> {
            val totalPercent = members.sumOf { splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0 }
            if (abs(totalPercent - 100.0) > 0.1) {
                return null
            }
            members.associate { member ->
                val percent = splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0
                member.uid to (amount * percent / 100)
            }
        }
        "By shares" -> {
            val totalShares = members.sumOf { splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0 }
            if (totalShares <= 0) {
                return null
            }
            members.associate { member ->
                val share = splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0
                member.uid to (amount * share / totalShares)
            }
        }
        else -> null
    }
}


//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun AddExpenseScreenPreview() {
//    AddExpenseScreen()
//}

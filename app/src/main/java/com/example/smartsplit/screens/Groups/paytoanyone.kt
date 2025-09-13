package com.example.smartsplit.screens.Groups



import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
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
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun paytonanyone(
    navController: NavController? = null,
    friendsViewModel: FriendsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
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
            .padding(24.dp)
    ) {
        IconButton(onClick = { navController?.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Pay to anyone",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // With who?
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select friend", color = Color.Black)
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
                        color = Color.Black
                    )
                },
                leadingIcon = {
                    Icon(
                        if (selectedGroup != null) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = primaryColor
                    )
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        Text("Paid by: You", fontWeight = FontWeight.Bold, color = primaryColor)


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
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Save Expense", color = Color.White)
            }
        }
    }

    // Friend or Group dialog
    if (showWithDialog) {
        SelectFriendOrGroupScreen1(
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
            onClose = { showWithDialog = false }
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
            title = { Text("Select Payer") },
            text = {
                Column {
                    members.forEach { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    paidByUid = member.uid
                                    paidByLabel = if (member.uid == currentUserId) "You" else (member.email ?: "Friend")
                                    showPaidByDialog = false
                                }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (paidByUid == member.uid)
                                    primaryColor.copy(alpha = 0.1f) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
                                Spacer(Modifier.width(8.dp))
                                Text(if (member.uid == currentUserId) "You" else (member.email ?: "Friend"))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPaidByDialog = false }) {
                    Text("Cancel")
                }
            }
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
            title = { Text("Select Split Method") },
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
                                    primaryColor.copy(alpha = 0.1f) else Color.White
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
                                    tint = primaryColor
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(method)
                            }
                        }
                    }

                    // Split inputs
                    if (splitBy == "By shares" || splitBy == "By percentage") {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (splitBy == "By shares") "Enter shares:" else "Enter percentages:",
                            fontWeight = FontWeight.Bold
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
                                label = { Text(if (member.uid == currentUserId) "You" else "Friend") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
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
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Done")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showSplitDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

// Helper functions for calculating splits
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendOrGroupScreen1(
    friends: List<Friend>,
    groups: List<Group>,
    onFriendSelected: (Friend) -> Unit,
    onGroupSelected: (Group) -> Unit,
    onClose: () -> Unit
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
                title = { Text("Select Friend or Group") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
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
                placeholder = { Text("Search friends or groups", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFriends.isEmpty() && filteredGroups.isEmpty() && searchQuery.isNotBlank()) {
                Text("No results found", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn {
                if (filteredFriends.isNotEmpty()) {
                    item { Text("Friends", style = MaterialTheme.typography.titleMedium) }
                    items(filteredFriends) { friend ->
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            headlineContent = { Text(friend.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFriendSelected(friend) }
                        )
                    }
                }

                if (filteredGroups.isNotEmpty()) {
                    item { Text("Groups", style = MaterialTheme.typography.titleMedium) }
                    items(filteredGroups) { group ->
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Group, contentDescription = null)
                            },
                            headlineContent = { Text(group.name) },
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


@Composable
fun SelectListItem1(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview1() {
    paytonanyone()
}

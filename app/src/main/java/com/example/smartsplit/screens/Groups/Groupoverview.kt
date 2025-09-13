
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ActivityTypes
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.Friend
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.Viewmodel.formatDate
import com.example.smartsplit.Viewmodel.logActivity
import com.example.smartsplit.data.DarkModeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.atan2


val groupTypes = listOf(
    "Travel" to Icons.Default.Flight,
    "Family" to Icons.Default.Home,
    "Friends" to Icons.Default.Group,
    "Work" to Icons.Default.Work,
    "Grocery" to Icons.Default.ShoppingCart,
    "Other" to Icons.Default.NoteAdd
)
val primaryColor = Color(0xFF2196F3)
val accentColor = Color(0xFF2196F3)
val darkBackground = Color(0xFF121212)
val darkCard = Color(0xFF1E1E1E)
val darkText = Color(0xFFFFFFFF)
val darkSecondaryText = Color(0xFFB3B3B3)
val darkFieldBorder = Color.White
val DarkOnSurface = Color(0xFFFFFFFF)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
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

    val lightPrimaryColor = Color(0xFF2196F3)
    val lightAccentColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), Color.White)
    )

    val darkGradientBrush = Brush.verticalGradient(
        colors = listOf(lightPrimaryColor.copy(alpha = 0.15f), darkBackground)
    )

    // UI States
    var selectedTab by remember { mutableStateOf("Members") }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message by viewModel.message.observeAsState("")
    val members by viewModel.groupMembers.observeAsState(emptyList())
    val pendingInvites by viewModel.pendingInvites.observeAsState(emptyList())
    var group by remember { mutableStateOf<Group?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
    var showUpiDialog by remember { mutableStateOf(false) }
    var upiIdInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var selectedSettlement by remember { mutableStateOf<com.example.smartsplit.Viewmodel.Settlement?>(null) }
    val context = LocalContext.current
    val expenses by expenseViewModel.groupExpenses.observeAsState(emptyList())
    val settlements by expenseViewModel.settlements.observeAsState(emptyList())

    // Add these state variables at the top of your composable
    var modifiedAmounts by remember { mutableStateOf(mapOf<String, Double>()) }
    var showModificationDialog by remember { mutableStateOf(false) }
    var currentlyEditingSettlement by remember { mutableStateOf<com.example.smartsplit.Viewmodel.Settlement?>(null) }
    var amountInput by remember { mutableStateOf("") }

    LaunchedEffect(groupId) {
        expenseViewModel.fetchGroupExpenses(groupId)
        expenseViewModel.fetchSettlements(groupId)
        // Reset modified amounts when data is refreshed
        modifiedAmounts = emptyMap()
    }

    // Friends list
    val friends by friendsViewModel.friends.collectAsState()

    // Calculate total spending per member for PieChart
    val memberExpenses = members.associate { member ->
        val total = expenses.filter { it.paidBy == member.uid }.sumOf { it.amount }.toFloat()
        val name = member.name ?: member.email ?: member.uid // Use name, fallback to email, then uid
        name to total
    }.filter { it.value > 0 }

    // Fetch data
    LaunchedEffect(groupId) {
        viewModel.fetchGroupDetails(groupId) { fetchedGroup ->
            group = fetchedGroup
        }
        viewModel.fetchGroupMembers(groupId)
        friendsViewModel.fetchFriends(currentUserId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isDark) Brush.linearGradient(
                        colors = listOf(darkBackground, darkBackground) // Solid dark background
                    ) else lightGradientBrush
                )
                .padding(padding)
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top Row with Back, Theme Toggle & Delete/Leave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = accentColor)
                }

                if (group?.createdByUid == currentUserId) { // Changed from createdBy to createdByUid
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Delete", tint = Color.Red)
                    }
                } else {
                    TextButton(onClick = { showLeaveDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Leave", tint = Color.Blue)
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            // Group Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) darkCard else Color(0xFFE3F2FD)
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDark) lightPrimaryColor.copy(alpha = 0.3f)
                                else Color(0xFF1976D2).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Group",
                            tint = if (isDark) darkText else Color(0xFF1976D2),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            group?.name ?: "Loading...",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) darkText else Color.Black
                            )
                        )

                        Text(
                            text = "Type: ${group?.type ?: "..."}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) darkSecondaryText else Color.DarkGray
                            )
                        )

                        Text(
                            text = "Created by: ${group?.createdBy ?: "..."}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) darkSecondaryText else Color.Gray
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Settle up", "Balance", "Total", "Members").forEach { tab ->
                    item {
                        GroupChip(
                            text = tab,
                            isSelected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            isDark = isDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tab Content
            when (selectedTab) {
                "Members" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) darkCard else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Members",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isDark) darkText else Color.Black
                            )

                            members.forEachIndexed { index, m ->
                                MemberRow(
                                    name = m.name ?: m.email ?: m.uid,
                                    isPending = false,
                                    isDark = isDark
                                )
                                if (index != members.lastIndex) {
                                    Divider(
                                        thickness = 0.5.dp,
                                        color = if (isDark) darkSecondaryText else Color.LightGray
                                    )
                                }
                            }

                            if (pendingInvites.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Pending Invites",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = if (isDark) darkSecondaryText else Color.Gray
                                )

                                pendingInvites.forEach { m ->
                                    MemberRow(
                                        name = m.name ?: m.email ?: m.uid,
                                        isPending = true,
                                        isDark = isDark
                                    )
                                }
                            }
                        }
                    }

                    if (group?.createdByUid == currentUserId) { // Changed from createdBy to createdByUid
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { showInviteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Invite Members", color = Color.White)
                            }
                        }
                    }
                }
                "Settle up" -> {
                    val netSettlements = expenseViewModel.calculateNetSettlements(expenses, settlements)
                    val mySettlements = netSettlements.filter { it.from == currentUserId }

                    if (mySettlements.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) darkCard else Color(0xFFE8F5E9)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Settled",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "You are all settled up! 🎉",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) darkText else Color(0xFF388E3C)
                                )
                                Text(
                                    text = "No pending settlements left.",
                                    color = if (isDark) darkSecondaryText else Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) darkCard else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Your pending settlements:",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) darkText else Color.Black
                                )

                                mySettlements.forEach { settlement ->
                                    val toName = members.find { it.uid == settlement.to }?.name ?:
                                    members.find { it.uid == settlement.to }?.email ?: settlement.to
                                    val originalAmount = settlement.amount
                                    val modifiedAmount = modifiedAmounts[settlement.id] ?: originalAmount
                                    val balance = originalAmount - modifiedAmount

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Pay to $toName",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) darkText else Color.Black
                                            )

                                            // Show both amounts
                                            if (modifiedAmount != originalAmount) {
                                                Text(
                                                    "₹${"%.2f".format(modifiedAmount)} / ₹${"%.2f".format(originalAmount)}",
                                                    color = Color.Red
                                                )
                                                Text(
                                                    "Balance: ₹${"%.2f".format(balance)}",
                                                    color = if (isDark) darkSecondaryText else Color.Gray,
                                                    fontSize = 12.sp
                                                )
                                            } else {
                                                Text(
                                                    "₹${"%.2f".format(originalAmount)}",
                                                    color = Color.Red
                                                )
                                            }
                                        }

                                        // Pay button only - remove modify button
                                        Button(
                                            onClick = {
                                                selectedSettlement = settlement
                                                upiIdInput = "" // Reset UPI input
                                                nameInput = "" // Reset name input
                                                showUpiDialog = true
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2196F3)
                                            ),
                                            enabled = modifiedAmount > 0
                                        ) {
                                            Text("Pay", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "Balance" -> {
                    // Calculate net settlements considering partial payments
                    val netSettlements = expenseViewModel.calculateNetSettlements(expenses, settlements)

                    // Helper function to get display name
                    fun getMemberDisplayName(uid: String): String {
                        return members.find { it.uid == uid }?.name ?:
                        members.find { it.uid == uid }?.email ?: uid
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Balances",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                            color = if (isDark) darkText else Color.Black
                        )

                        val hasBalances = members.any { member ->
                            val uid = member.uid
                            netSettlements.any { it.from == uid || it.to == uid }
                        }

                        if (!hasBalances) {
                            // Empty state for Balance tab
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) darkCard else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "No balances",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "All balances are settled!",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) darkText else Color(0xFF388E3C)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "No one owes anyone in this group.",
                                        color = if (isDark) darkSecondaryText else Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            members.forEach { member ->
                                val uid = member.uid
                                val displayName = member.name ?: member.email ?: member.uid

                                val owes = netSettlements.filter { it.from == uid }
                                val lents = netSettlements.filter { it.to == uid }

                                if (owes.isNotEmpty() || lents.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) darkCard else Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(3.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isDark) darkSecondaryText else Color.LightGray
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            // Member header
                                            Text(
                                                displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (isDark) darkText else Color(0xFF1565C0)
                                            )
                                            Spacer(Modifier.height(8.dp))

                                            // Show owes
                                            owes.forEach { settlement ->
                                                val toName = getMemberDisplayName(settlement.to)
                                                Text(
                                                    "• Owes to $toName: ₹${"%.2f".format(settlement.amount)}",
                                                    color = Color(0xFFD32F2F),
                                                    fontSize = 14.sp
                                                )
                                            }

                                            // Show lents
                                            lents.forEach { settlement ->
                                                val fromName = getMemberDisplayName(settlement.from)
                                                Text(
                                                    "• Lent from $fromName: ₹${"%.2f".format(settlement.amount)}",
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Total" -> {
                    val totalSpending = expenses.sumOf { it.amount }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Expense Summary",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                            color = if (isDark) darkText else Color.Black
                        )

                        if (expenses.isEmpty()) {
                            // Empty state for Total tab
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) darkCard else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = "No expenses",
                                        tint = if (isDark) darkSecondaryText else Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "No expenses yet",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) darkText else Color(0xFF2196F3)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Add your first expense to see the summary here.",
                                        color = if (isDark) darkSecondaryText else Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            if (memberExpenses.isNotEmpty()) {
                                Text(
                                    "Spending Distribution",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = if (isDark) darkText else Color.Black
                                )
                                val yourShare = expenses
                                    .flatMap { expense ->
                                        expense.splits.entries.filter { it.key == currentUserId }.map { it.value }
                                    }
                                    .sum()
                                    .toFloat()

                                InteractivePieChart(
                                    data = memberExpenses,
                                    yourShare = yourShare,
                                    totalSpending = totalSpending.toFloat(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    isDark = isDark
                                )
                            }

                            expenses.forEach { expense ->
                                val payer = members.find { it.uid == expense.paidBy }?.name ?:
                                members.find { it.uid == expense.paidBy }?.email ?: expense.paidBy

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) darkCard else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) darkSecondaryText else Color.LightGray
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {

                                        // 🔹 Date Row with Icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Date",
                                                tint = if (isDark) darkSecondaryText else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = formatDate(expense.createdAt),
                                                fontSize = 13.sp,
                                                color = if (isDark) darkSecondaryText else Color.Gray
                                            )
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        // 🔹 Expense Description
                                        Text(
                                            expense.description,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = if (isDark) darkText else Color.Black
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        // 🔹 Amount
                                        Text(
                                            "₹${"%.2f".format(expense.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF4CAF50)
                                        )

                                        Spacer(Modifier.height(2.dp))

                                        // 🔹 Payer
                                        Text(
                                            "Paid by $payer",
                                            fontSize = 13.sp,
                                            color = if (isDark) darkSecondaryText else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("addexpense") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) lightPrimaryColor else lightAccentColor
                ),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Add expense", color = Color.White)
            }
        }

        // Add this dialog after the UPI dialog
        if (showInviteDialog && group?.createdByUid == currentUserId) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                title = {
                    Text(
                        "Invite Friend to Group",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) darkText else Color.Black
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp) // 🔥 Scrollable limit
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Select a friend to invite:",
                            fontSize = 16.sp,
                            color = if (isDark) darkSecondaryText else Color.Gray
                        )

                        val eligibleFriends = friends.filter { friend ->
                            members.none { it.uid == friend.uid } &&
                                    pendingInvites.none { it.uid == friend.uid }
                        }

                        if (eligibleFriends.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No eligible friends to invite", color = if (isDark) darkSecondaryText else Color.Gray)
                            }
                        } else {
                            eligibleFriends.forEach { friend ->
                                FriendItem(
                                    friend = friend,
                                    isSelected = selectedFriend?.uid == friend.uid,
                                    onClick = { selectedFriend = friend },
                                    isDark = isDark
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = selectedFriend != null,
                        onClick = {
                            selectedFriend?.let { friend ->
                                friendsViewModel.sendGroupInvite(
                                    groupId = groupId,
                                    groupName = group?.name ?: "Group",
                                    toUserId = friend.uid
                                ) { _, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            }
                            showInviteDialog = false
                        }
                    ) {
                        Text("Send Invite", color = if (isDark) darkText else Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Cancel", color = if (isDark) darkSecondaryText else Color.Gray)
                    }
                },
                containerColor = if (isDark) darkCard else Color.White
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Delete Group?",
                        color = if (isDark) darkText else Color.Black
                    )
                },
                text = {
                    Text(
                        "This action cannot be undone. Are you sure you want to delete this group?",
                        color = if (isDark) darkSecondaryText else Color.Black
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false

                            // Log the group deletion activity
                            logActivity(
                                type = ActivityTypes.GROUP_DELETED,
                                description = "Deleted group: ${group?.name ?: "Loading..."}",
                                relatedGroupId = groupId,
                                userId = FirebaseAuth.getInstance().currentUser?.uid
                            )

                            viewModel.deleteGroup(groupId)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = if (isDark) darkSecondaryText else Color.Gray)
                    }
                },
                containerColor = if (isDark) darkCard else Color.White
            )
        }

        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = {
                    Text(
                        "Leave Group?",
                        color = if (isDark) darkText else Color.Black
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to leave this group?",
                        color = if (isDark) darkSecondaryText else Color.Black
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLeaveDialog = false

                            // Log the group leave activity
                            logActivity(
                                type = ActivityTypes.GROUP_LEFT,
                                description = "Left group: ${group?.name ?: "Loading..."}",
                                relatedGroupId = groupId,
                                userId = FirebaseAuth.getInstance().currentUser?.uid
                            )

                            viewModel.leaveGroup(groupId)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Leave", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text("Cancel", color = if (isDark) darkSecondaryText else Color.Gray)
                    }
                },
                containerColor = if (isDark) darkCard else Color.White
            )
        }
    }

    if (showUpiDialog && selectedSettlement != null) {
        val settlement = selectedSettlement!!
        val toName = members.find { it.uid == settlement.to }?.name ?:
        members.find { it.uid == settlement.to }?.email ?: settlement.to
        val originalAmount = settlement.amount
        val currentModifiedAmount = modifiedAmounts[settlement.id] ?: originalAmount

        var amountToPay by remember { mutableStateOf(currentModifiedAmount.toString()) }

        AlertDialog(
            onDismissRequest = { showUpiDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = if (isDark) darkCard else Color(0xFFF5F5F5),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Settle Up",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) darkText else Color(0xFF2196F3)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You owe ₹${"%.2f".format(originalAmount)} to $toName",
                        fontSize = 14.sp,
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                    if (currentModifiedAmount != originalAmount) {
                        Text(
                            "Previous partial payment: ₹${"%.2f".format(originalAmount - currentModifiedAmount)}",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Amount input field
                    // For all OutlinedTextField components, use the correct color parameters:
                    val onSurfaceColor = if (isDark) DarkOnSurface else Color.Black
                    OutlinedTextField(
                        value = amountToPay,
                        onValueChange = {
                            if (it.isBlank() || it.toDoubleOrNull() != null) {
                                amountToPay = it
                            }
                        },
                        label = { Text("Amount to pay (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter amount to pay") },
                        // Correct color parameters:
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                            unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            containerColor = Color.Transparent
                        )
                    )

                    // Validation message
                    val enteredAmount = amountToPay.toDoubleOrNull() ?: 0.0
                    if (enteredAmount > currentModifiedAmount) {
                        Text(
                            "Amount cannot exceed remaining balance",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        "Enter UPI ID:",
                        fontSize = 14.sp,
                        color = if (isDark) darkSecondaryText else Color.DarkGray
                    )

                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = { Text("UPI ID (e.g. user@upi)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedLabelColor = if (isDark) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                            unfocusedBorderColor = if (isDark) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor,
                            containerColor = Color.Transparent
                        )
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Receiver Name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            val paymentAmount = amountToPay.toDoubleOrNull() ?: 0.0
                            if (paymentAmount > 0 && paymentAmount <= currentModifiedAmount && upiIdInput.isNotBlank()) {
                                // Launch UPI payment
                                val uri = Uri.parse("upi://pay").buildUpon()
                                    .appendQueryParameter("pa", upiIdInput.trim())
                                    .appendQueryParameter("pn", nameInput.ifBlank { toName })
                                    .appendQueryParameter("tn", "Group settlement")
                                    .appendQueryParameter("am", "%.2f".format(paymentAmount))
                                    .appendQueryParameter("cu", "INR")
                                    .build()

                                val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                                }

                                // Record partial payment
                                expenseViewModel.markSettlementPartiallyPaid(
                                    settlement = settlement,
                                    groupId = groupId,
                                    amountPaid = paymentAmount
                                ) { success ->
                                    if (success) {
                                        // Update local state
                                        val newRemaining = currentModifiedAmount - paymentAmount
                                        modifiedAmounts = if (newRemaining > 0) {
                                            modifiedAmounts + (settlement.id to newRemaining)
                                        } else {
                                            modifiedAmounts - settlement.id
                                        }
                                    }
                                }
                                showUpiDialog = false
                            } else {
                                Toast.makeText(context, "Please enter valid amount and UPI ID", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Pay via UPI", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val paymentAmount = amountToPay.toDoubleOrNull() ?: currentModifiedAmount
                            if (paymentAmount > 0 && paymentAmount <= currentModifiedAmount) {
                                // Mark as paid without UPI
                                expenseViewModel.markSettlementPartiallyPaid(
                                    settlement = settlement,
                                    groupId = groupId,
                                    amountPaid = paymentAmount
                                ) { success ->
                                    if (success) {
                                        // Update local state
                                        val newRemaining = currentModifiedAmount - paymentAmount
                                        modifiedAmounts = if (newRemaining > 0) {
                                            modifiedAmounts + (settlement.id to newRemaining)
                                        } else {
                                            modifiedAmounts - settlement.id
                                        }
                                    }
                                }
                                showUpiDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Mark as Paid", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpiDialog = false }
                ) {
                    Text("Cancel", color = if (isDark) darkSecondaryText else Color.Gray)
                }
            }
        )
    }

    if (message.isNotEmpty()) {
        LaunchedEffect(message) { Log.d("UI", "Message: $message") }
    }
}

// Update the GroupChip composable to support dark mode
@Composable
fun GroupChip(text: String, isSelected: Boolean, onClick: () -> Unit, isDark: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = BorderStroke(1.dp, if (isDark) Color.White else Color.Gray),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else if (isDark) Color.White else Color.Black
        )
    }
}

// Update the MemberRow composable to support dark mode
@Composable
fun MemberRow(name: String, isPending: Boolean, isDark: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Circle Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isDark) Color(0xFF37474F) else Color.LightGray, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDark) Color.White else Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                color = if (isPending) Color.Gray else if (isDark) Color.White else Color.Black,
                fontStyle = if (isPending) FontStyle.Italic else FontStyle.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isPending) {
                Text(
                    text = "Pending",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Update the FriendItem composable to support dark mode
@Composable
fun FriendItem(friend: Friend, isSelected: Boolean, onClick: () -> Unit, isDark: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                (if (isDark) Color(0xFF37474F) else Color(0xFFE3F2FD))
            else
                (if (isDark) darkCard else Color.White)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isDark) Color(0xFF37474F) else Color(0xFF90CAF9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name?.firstOrNull()?.uppercase() ?:
                    friend.email.firstOrNull()?.uppercase() ?: "?", // Use name first
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    friend.name ?: friend.email,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isDark) darkText else Color.Black
                ) // Show name first
                if (friend.name != null && friend.email != null) {
                    Text(
                        friend.email,
                        fontSize = 12.sp,
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                }
            }
        }
    }
}

// Update the InteractivePieChart composable to support dark mode
@Composable
fun InteractivePieChart(
    data: Map<String, Float>,   // member name/email -> amount
    yourShare: Float,           // Your share of the total spending
    totalSpending: Float,       // Total group spending
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val total = data.values.sum()
    var startAngle = 0f

    // State for selected slice
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    var selectedAmount by remember { mutableStateOf<Float?>(null) }
    var selectedPercentage by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Summary information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors= CardDefaults.cardColors(if (isDark) Color(0xFF1E1E1E) else Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Total Group Spending: ₹${"%.2f".format(totalSpending)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) darkText else Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your Share: ₹${"%.2f".format(yourShare)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (yourShare > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
                if (totalSpending > 0) {
                    Text(
                        "Your Percentage: ${"%.1f".format((yourShare / totalSpending) * 100)}%",
                        fontSize = 12.sp,
                        color = if (isDark) darkSecondaryText else Color.Gray
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .aspectRatio(1f) // force circle
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(true) {
                        detectTapGestures { offset ->
                            // center of canvas
                            val center = Offset(x = size.width / 2f, y = size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            // Find which slice contains this angle
                            var currentStart = 0f
                            data.entries.forEach { entry ->
                                val sweep = 360f * (entry.value / total)
                                if (angle in currentStart..(currentStart + sweep)) {
                                    selectedLabel = entry.key
                                    selectedAmount = entry.value
                                    selectedPercentage = (entry.value / total) * 100f
                                }
                                currentStart += sweep
                            }
                        }
                    }
            ) {
                // Draw the pie chart
                data.entries.forEachIndexed { index, entry ->
                    val sweepAngle = 360 * (entry.value / total)
                    val color = Color.hsv(
                        (index * 60) % 360f, // auto-generate distinct colors
                        0.7f,
                        0.9f
                    )
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = size
                    )
                    startAngle += sweepAngle
                }

                // Draw a white circle in the center to create a donut chart effect
                drawCircle(
                    color = if (isDark) darkCard else Color.White,
                    radius = size.minDimension / 4,
                    center = center
                )

                // Add text in the center showing total
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "₹${"%.2f".format(totalSpending)}",
                        center.x,
                        center.y - 10,
                        android.graphics.Paint().apply {
                            textSize = 24f
                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    drawText(
                        "Total",
                        center.x,
                        center.y + 20,
                        android.graphics.Paint().apply {
                            textSize = 14f
                            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // Show info when a slice is tapped
        selectedLabel?.let { label ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(if (isDark) darkCard else Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Paid by: $label",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) darkText else Color.Black
                    )
                    Text(
                        "Amount: ₹${"%.2f".format(selectedAmount)}",
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "Share: ${"%.1f".format(selectedPercentage)}%",
                        color = if (isDark) darkSecondaryText else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Legend
        if (data.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Legend:",
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) darkText else Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp)
            ) {
                items(data.entries.toList()) { (name, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color.hsv(
                                        (data.entries.indexOfFirst { it.key == name } * 60) % 360f,
                                        0.7f,
                                        0.9f
                                    ),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isDark) darkText else Color.Black
                        )
                        Text(
                            text = "₹${"%.2f".format(amount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) darkText else Color.Black
                        )
                    }
                }
            }
        }
    }
}

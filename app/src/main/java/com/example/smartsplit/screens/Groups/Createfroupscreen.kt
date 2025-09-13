import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.R
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.Viewmodel.logActivity
import com.example.smartsplit.data.DarkModeViewModel
import com.example.smartsplit.screens.Friends.DarkBackground
import com.example.smartsplit.screens.Friends.DarkOnSurface
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavHostController,
    groupViewModel: GroupViewModel = viewModel(),
    onBackClick: () -> Unit = {},

) {
    val darkModeViewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDarkMode = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val createdGroupId by groupViewModel.createdGroupId.observeAsState()
    val message by groupViewModel.message.observeAsState("")

    val groupTypes = listOf(
        "Travel" to Icons.Default.Flight,
        "Family" to Icons.Default.Home,
        "Friends" to Icons.Default.Group,
        "Work" to Icons.Default.Work,
        "Grocery" to Icons.Default.ShoppingCart,
        "Other" to Icons.Default.NoteAdd
    )

    // Dark mode colors
    val primaryColor = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF2196F3)
    val accentColor = if (isDarkMode) Color.White else Color(0xFF2196F3)
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFBBBBBB) else Color.Gray
    val outlineColor = if (isDarkMode) Color(0xFF444444) else Color.Gray
    val onSurfaceColor = if (isDarkMode) DarkOnSurface else Color.Black

    val gradientBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.15f),
                backgroundColor
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Back Arrow
        IconButton(onClick = { onBackClick(); navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = accentColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Create Group",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // Row with camera + group name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable { Log.d("CreateGroup", "Camera clicked") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PeopleOutline,
                    contentDescription = "Group Photo",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedLabelColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedBorderColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    containerColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group type
        Text(
            text = "Choose Type",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = textColor
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column {
            for (i in groupTypes.indices step 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    groupTypes.subList(i, minOf(i + 2, groupTypes.size)).forEach { (label, icon) ->
                        FilterChip(
                            selected = selectedType == label,
                            onClick = { selectedType = label },
                            label = { Text(label, color = if (isDarkMode && selectedType == label) Color.Black else textColor) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isDarkMode && selectedType == label) Color.Black else textColor
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                containerColor = if (isDarkMode) surfaceColor else Color.White,
                                labelColor = textColor,
                                selectedLabelColor = if (isDarkMode) Color.Black else Color.White,
                                iconColor = textColor,
                                selectedLeadingIconColor = if (isDarkMode) Color.Black else Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Travel-specific fields
        if (selectedType == "Travel") {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date (Optional)", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedLabelColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedBorderColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date (Optional)", color = secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = onSurfaceColor),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedLabelColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedBorderColor = if (isDarkMode) onSurfaceColor.copy(alpha = 0.5f) else Color.Gray,
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    containerColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Done button
        Button(
            onClick = {
                if (groupName.isNotBlank() && selectedType != null) {
                    groupViewModel.createGroup(groupName, selectedType!!)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (isDarkMode) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done")
        }

        // Show status message
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                color = if (isDarkMode) secondaryTextColor else Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Navigate only with groupId when created
    LaunchedEffect(createdGroupId, message) {
        if (message.contains("successfully", ignoreCase = true) && createdGroupId != null) {
            logActivity(
                type = "GROUP_CREATED",
                description = "Group '$groupName' was created",
                relatedGroupId = createdGroupId,
                userId = currentUserId
            )

            Log.d("CreateGroup", "Navigating with ID: $createdGroupId")
            navController.navigate("GroupOverview/$createdGroupId") {
                popUpTo("CreateGroup") { inclusive = true }
            }
        }
    }
}
package com.example.smartsplit.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.smartsplit.Viewmodel.FriendRequest
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.GroupInvite
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.data.DarkModeViewModel
import com.example.smartsplit.screens.Friends.DarkBackground
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel(),
    darkModeViewModel: DarkModeViewModel = hiltViewModel()
) {
    val darkModeOption by darkModeViewModel.darkModeLiveData.observeAsState("Automatic")
    val isDarkMode = when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
    // Dark mode colors
    val primaryColor = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF2196F3)
    val accentColor = if (isDarkMode) Color.White else Color(0xFF2196F3)
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFBBBBBB) else Color.Gray
    val outlineColor = if (isDarkMode) Color(0xFF444444) else Color.Gray

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

    val friendRequests by viewModel.friendRequests.collectAsState()
    val groupInvites by viewModel.groupInvites.collectAsState()

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.fetchFriendRequests(currentUserId)
            viewModel.fetchGroupInvites(currentUserId)
        }
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
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentColor)
                }
                Text(
                    "Requests & Invites",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Friend Requests Section
            Text(
                "Friend Requests",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )

            if (friendRequests.isEmpty()) {
                Text("No friend requests", color = secondaryTextColor)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(friendRequests) { (requestId, request) ->
                        RequestCard(
                            requestId = requestId,
                            request = request,
                            accentColor = accentColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            surfaceColor = surfaceColor,
                            onAccept = { viewModel.acceptRequest(requestId, request) },
                            onReject = { viewModel.rejectRequest(requestId) },
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Group Invites Section
            Text(
                "Group Invites",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )

            if (groupInvites.isEmpty()) {
                Text("No group invites", color = secondaryTextColor)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(groupInvites) { (inviteId, invite) ->
                        GroupInviteCard(
                            inviteId = inviteId,
                            invite = invite,
                            accentColor = accentColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            surfaceColor = surfaceColor,
                            onAccept = { viewModel.acceptGroupInvite(inviteId, invite) },
                            onReject = { viewModel.rejectGroupInvite(inviteId) },
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    requestId: String,
    request: FriendRequest,
    accentColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Friend Request",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "From User: ${request.fromUserId}",
                color = textColor
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isDarkMode) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept")
                }
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun GroupInviteCard(
    inviteId: String,
    invite: GroupInvite,
    accentColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    invite.groupName,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    "Invited by: ${invite.invitedBy}",
                    color = secondaryTextColor,
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onAccept) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = accentColor
                    )
                }
                IconButton(onClick = onReject) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
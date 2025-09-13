package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FriendRequest(
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val uid: String = "",
    val name: String = "",   // user name
    val email: String = ""   // keep email as before
)


data class GroupInvite(
    val groupId: String = "",
    val groupName: String = "",
    val invitedBy: String = "",
    val toUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class FriendsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _friendRequests = MutableStateFlow<List<Pair<String, FriendRequest>>>(emptyList())
    val friendRequests: StateFlow<List<Pair<String, FriendRequest>>> = _friendRequests

    private val _groupInvites = MutableStateFlow<List<Pair<String, GroupInvite>>>(emptyList())
    val groupInvites: StateFlow<List<Pair<String, GroupInvite>>> = _groupInvites

    private val currentUserId = auth.currentUser?.uid

    init {
        currentUserId?.let {
            fetchFriends(it)
            fetchGroupInvites(it)
        }
    }

    // ============================
    // FRIEND LOGIC (No change)
    // ============================
    fun sendFriendRequest(email: String, onResult: (Boolean, String) -> Unit) {
        if (currentUserId == null) {
            onResult(false, "User not logged in")
            return
        }

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onResult(false, "Email not registered")
                } else {
                    val toUser = query.documents.first()
                    val toUserId = toUser.id

                    val request = FriendRequest(
                        fromUserId = currentUserId,
                        toUserId = toUserId
                    )

                    db.collection("friendRequests")
                        .add(request)
                        .addOnSuccessListener {
                            onResult(true, "Request sent successfully")
                        }
                        .addOnFailureListener {
                            onResult(false, "Failed to send request")
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, "Error checking email")
            }
    }

    fun fetchFriends(userId: String) {
        db.collection("friends")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("FriendsViewModel", "Error fetching friends", error)
                    _friends.value = emptyList()
                    return@addSnapshotListener
                }

                Log.d("FriendsViewModel", "Friends snapshot size: ${snapshot.size()}")

                val tempFriends = mutableListOf<Friend>()

                snapshot.documents.forEach { doc ->
                    val members = doc.get("members") as? List<*>
                    val friendId = members?.firstOrNull { it != userId } as? String

                    if (friendId != null) {
                        db.collection("users").document(friendId).get()
                            .addOnSuccessListener { userDoc ->
                                val email = userDoc.getString("email") ?: "Unknown"
                                val name = userDoc.getString("display_name") ?: email // Get name or fallback to email
                                tempFriends.add(Friend(uid = friendId, email = email, name = name))
                                _friends.value = tempFriends.toList()
                            }
                            .addOnFailureListener {
                                Log.e("FriendsViewModel", "Failed to fetch user: $friendId", it)
                            }
                    }
                }
            }
    }

    fun fetchFriendRequests(userId: String) {
        db.collection("friendRequests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _friendRequests.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val request = doc.toObject(FriendRequest::class.java)
                    request?.let { doc.id to it }
                }
                _friendRequests.value = list
            }
    }

    fun acceptRequest(requestId: String, request: FriendRequest) {
        val friendsRef = db.collection("friends").document()

        db.runBatch { batch ->
            batch.update(
                db.collection("friendRequests").document(requestId),
                "status", "accepted"
            )
            batch.set(
                friendsRef,
                mapOf(
                    "members" to listOf(request.fromUserId, request.toUserId),
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    fun rejectRequest(requestId: String) {
        db.collection("friendRequests").document(requestId)
            .update("status", "rejected")
    }

    // ============================
    // GROUP INVITE LOGIC (NEW)
    // ============================
    fun fetchGroupInvites(userId: String) {
        db.collection("groupInvites")
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _groupInvites.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val invite = doc.toObject(GroupInvite::class.java)
                    invite?.let { doc.id to it }
                }
                _groupInvites.value = list
            }
    }

    fun sendGroupInvite(
        groupId: String,
        groupName: String,
        toUserId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in")
            return
        }

        // ✅ Check if this user is in the friends list
        val isFriend = _friends.value.any { it.uid == toUserId }
        if (!isFriend) {
            onResult(false, "You can only invite friends.")
            return
        }

        val invite = GroupInvite(
            groupId = groupId,
            groupName = groupName,
            invitedBy = currentUser.uid,
            toUserId = toUserId
        )

        db.collection("groupInvites")
            .add(invite)
            .addOnSuccessListener {
                onResult(true, "Invite sent successfully!")
            }
            .addOnFailureListener {
                onResult(false, "Failed to send invite.")
            }
    }

    fun acceptGroupInvite(inviteId: String, invite: GroupInvite) {
        val groupRef = db.collection("groups").document(invite.groupId)
        val memberRef = groupRef.collection("members").document(invite.toUserId)

        db.runBatch { batch ->
            // Add user as a member
            batch.set(memberRef, mapOf(
                "uid" to invite.toUserId,
                "role" to "member",
                "accepted" to true,
                "joinedAt" to FieldValue.serverTimestamp()
            ))

            // Delete the invite after accepting
            batch.delete(db.collection("groupInvites").document(inviteId))
        }.addOnSuccessListener {
            Log.d("GroupInvite", "User added to group successfully")
        }.addOnFailureListener { e ->
            Log.e("GroupInvite", "Failed to add user to group", e)
        }
    }

    // Add this to your FriendsViewModel class
    fun getFriend(friendId: String): StateFlow<Friend?> {
        val result = MutableStateFlow<Friend?>(null)

        db.collection("users").document(friendId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email") ?: ""
                    val name = document.getString("name") ?: email
                    result.value = Friend(uid = friendId, name = name, email = email)
                } else {
                    result.value = null
                }
            }
            .addOnFailureListener {
                result.value = null
            }

        return result
    }
    fun rejectGroupInvite(inviteId: String) {
        db.collection("groupInvites").document(inviteId).delete()
    }
}

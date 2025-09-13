package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp


data class Group(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val createdBy: String = "", // This will now store the name
    val createdByUid: String = "" // Add this to store the UID for reference
)
data class GroupMember(
    val uid: String = "",
    val role: String = "member",
    val accepted: Boolean = false,
    val invitedAt: Timestamp? = null,
    val joinedAt: Timestamp? = null,
    var email: String? = null,
    var name: String? = null // Add this field
)


class GroupViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _createdGroupId = MutableLiveData<String?>()
    val createdGroupId: LiveData<String?> = _createdGroupId

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _myGroups = MutableLiveData<List<Group>>(emptyList())
    val myGroups: LiveData<List<Group>> = _myGroups

    private val _groupMembers = MutableLiveData<List<GroupMember>>(emptyList())
    val groupMembers: LiveData<List<GroupMember>> = _groupMembers

    private val _pendingInvites = MutableLiveData<List<GroupMember>>(emptyList())
    val pendingInvites: LiveData<List<GroupMember>> = _pendingInvites

    // In GroupViewModel.kt
    data class SharedGroup(
        val id: String,
        val name: String,
        val memberCount: Int
    )
    fun getUserNameFromUid1(uid: String, onResult: (String?) -> Unit) {
        Log.d("GroupViewModel", "Fetching name for UID: $uid")

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("display_name") // 👈 use display_name
                    Log.d("GroupViewModel", "Fetched user doc: ${document.data}")
                    Log.d("GroupViewModel", "Extracted display_name: $name")
                    onResult(name)
                } else {
                    Log.w("GroupViewModel", "No user found for UID: $uid")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("GroupViewModel", "Error fetching user for UID: $uid", e)
                onResult(null)
            }
    }
    private val _sharedGroupsWithFriend = MutableLiveData<List<SharedGroup>>(emptyList())
    val sharedGroupsWithFriend: LiveData<List<SharedGroup>> = _sharedGroupsWithFriend

    fun fetchSharedGroupsWithFriend(currentUserId: String, friendId: String) {
        // Query groups where both current user AND friend are members
        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .whereArrayContains("members", friendId)
            .get()
            .addOnSuccessListener { groupsSnapshot ->
                val sharedGroups = mutableListOf<SharedGroup>()
                groupsSnapshot.documents.forEach { groupDoc ->
                    // Get the member count from the members array
                    val members = groupDoc.get("members") as? List<String> ?: emptyList()

                    sharedGroups.add(SharedGroup(
                        id = groupDoc.id,
                        name = groupDoc.getString("name") ?: "Unnamed Group",
                        memberCount = members.size
                    ))
                }
                _sharedGroupsWithFriend.value = sharedGroups
            }
            .addOnFailureListener { e ->
                _message.value = "Error fetching shared groups: ${e.message}"
            }
    }

    fun fetchGroupMembers(groupId: String) {
        db.collection("groups").document(groupId)
            .collection("members")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val members = snapshot?.documents?.mapNotNull { it.toObject(GroupMember::class.java) } ?: emptyList()

                members.forEach { member ->
                    // Get both email and name from users collection
                    getUserDetailsFromUid(member.uid) { email, name ->
                        member.email = email
                        member.name = name ?: email // Fallback to email if name is not available
                        _groupMembers.value = members.filter { it.accepted == true }
                        _pendingInvites.value = members.filter { it.accepted != true }
                    }
                }
            }
    }

    // Helper function to get both email and name
    private fun getUserDetailsFromUid(uid: String, onResult: (String?, String?) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email")
                    val name = document.getString("display_name") // Assuming you store name as "display_name"
                    onResult(email, name)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener {
                onResult(null, null)
            }
    }


    fun createGroup(groupName: String, groupType: String) {
        val currentUser = auth.currentUser ?: return
        val creatorId = currentUser.uid

        val groupData = hashMapOf(
            "name" to groupName,
            "type" to groupType,
            "createdBy" to creatorId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("groups")
            .add(groupData)
            .addOnSuccessListener { groupRef ->
                val groupId = groupRef.id
                _createdGroupId.value = groupId

                val memberRef = groupRef.collection("members").document(creatorId)
                val memberData = mapOf(
                    "uid" to creatorId,
                    "role" to "admin",
                    "accepted" to true,
                    "joinedAt" to FieldValue.serverTimestamp()
                )
                memberRef.set(memberData)
                    .addOnSuccessListener {
                        _message.value = "Group created successfully!"
                        fetchMyGroups() // Refresh group list
                    }
                    .addOnFailureListener { e ->
                        _message.value = "Failed to add creator as member: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to create group: ${e.message}"
            }
    }



    /**
     * Fetch groups created by me and map UID → Email
     */
    fun fetchMyGroups() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        db.collection("groups")
            .get()
            .addOnSuccessListener { groupDocs ->
                val groups = mutableListOf<Group>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (doc in groupDocs) {
                    val groupId = doc.id
                    val membersTask = db.collection("groups")
                        .document(groupId)
                        .collection("members")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { memberDoc ->
                            if (memberDoc.exists()) {
                                val creatorUid = doc.getString("createdBy") ?: ""
                                // Get user name instead of email
                                getUserNameFromUid(creatorUid) { name ->
                                    groups.add(
                                        Group(
                                            id = groupId,
                                            name = doc.getString("name") ?: "",
                                            type = doc.getString("type") ?: "",
                                            createdBy = name ?: "Unknown User", // Use name here
                                            createdByUid = creatorUid // Store UID for reference
                                        )
                                    )
                                    _myGroups.value = groups
                                }
                            }
                        }
                    tasks.add(membersTask)
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to fetch groups: ${e.message}"
            }
    }

    private fun getUserNameFromUid(uid: String, onResult: (String?) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Try to get display_name first, fallback to email if not available
                    val name = document.getString("display_name") ?: document.getString("email")
                    onResult(name)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun fetchGroupDetails(groupId: String, onComplete: (Group?) -> Unit) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val createdByUid = doc.getString("createdBy") ?: ""

                    // Fetch user name from users collection
                    getUserNameFromUid(createdByUid) { name ->
                        val group = Group(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            type = doc.getString("type") ?: "",
                            createdBy = name ?: "Unknown User", // Use name instead of email
                            createdByUid = createdByUid
                        )
                        onComplete(group)
                    }
                } else {
                    _message.value = "Group not found"
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to fetch group: ${e.message}"
                onComplete(null)
            }
    }

    fun deleteGroup(groupId: String) {
        val currentUser = auth.currentUser ?: return

        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val createdBy = doc.getString("createdBy")
                    if (createdBy == currentUser.uid) {
                        // First delete all members
                        db.collection("groups").document(groupId)
                            .collection("members")
                            .get()
                            .addOnSuccessListener { members ->
                                val batch = db.batch()
                                for (member in members) {
                                    batch.delete(member.reference)
                                }
                                // Delete group doc after members
                                batch.delete(doc.reference)

                                batch.commit()
                                    .addOnSuccessListener {
                                        _message.value = "Group deleted successfully"
                                        fetchMyGroups()
                                    }
                                    .addOnFailureListener { e ->
                                        _message.value = "Failed to delete group: ${e.message}"
                                    }
                            }
                    } else {
                        _message.value = "Only the creator can delete the group"
                    }
                }
            }
    }

    fun leaveGroup(groupId: String) {
        val currentUser = auth.currentUser ?: return

        val memberRef = db.collection("groups")
            .document(groupId)
            .collection("members")
            .document(currentUser.uid)

        memberRef.delete()
            .addOnSuccessListener {
                _message.value = "You left the group"
                fetchMyGroups()
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to leave group: ${e.message}"
            }
    }


}


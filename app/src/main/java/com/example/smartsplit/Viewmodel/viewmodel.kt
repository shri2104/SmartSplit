package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class LoginScreenViewModel : ViewModel() {
    var lastCreatedGroupId: String? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _user = MutableLiveData<MUser?>()
    val user: LiveData<MUser?> = _user
    private val _errorMessage = MutableLiveData<String?>(null)
    private val _PrivateUser= MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // 🔹 Sign In

    private val _createdGroupId = MutableLiveData<String?>()
    val createdGroupId: LiveData<String?> = _createdGroupId

    private val db = FirebaseFirestore.getInstance()

    private val _invites = MutableLiveData<List<Map<String, Any>>>()
    val invites: LiveData<List<Map<String, Any>>> = _invites

    private val _groups = MutableLiveData<List<Map<String, Any>>>()
    val groups: LiveData<List<Map<String, Any>>> = _groups

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // -------------------------
    // CREATE GROUP
    // -------------------------
    fun createGroup(groupName: String, type: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("CreateGroup", "User not logged in")
            _message.value = "User not logged in"
            return
        }

        val groupRef = db.collection("groups").document()
        val groupData = mapOf(
            "groupId" to groupRef.id,
            "groupName" to groupName,
            "groupType" to type,
            "createdBy" to currentUserId,
            "members" to listOf(currentUserId),
            "createdAt" to System.currentTimeMillis()
        )

        Log.d("CreateGroup", "Attempting to create group with ID: ${groupRef.id}")

        groupRef.set(groupData)
            .addOnSuccessListener {
                Log.d("CreateGroup", "Group created successfully in Firestore: ${groupRef.id}")
                _message.value = "Group created successfully"
                _createdGroupId.value = groupRef.id
            }

            .addOnFailureListener { e ->
                Log.e("CreateGroup", "Failed to create group", e)
                _message.value = "Failed to create group: ${e.message}"
            }
    }


    // -------------------------
    // INVITE USER TO GROUP
    // -------------------------
    fun inviteUserToGroup(email: String, groupId: String?) {
        viewModelScope.launch {
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val targetUser = query.documents[0]
                        val targetUserId = targetUser.id
                        val currentUserId = auth.currentUser?.uid ?: return@addOnSuccessListener

                        val inviteData = hashMapOf(
                            "from" to currentUserId,
                            "groupId" to groupId,
                            "status" to "pending",
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("users").document(targetUserId)
                            .collection("invites")
                            .document(groupId!!)
                            .set(inviteData)
                            .addOnSuccessListener {
                                _message.value = "Invite sent successfully"
                            }
                            .addOnFailureListener { e ->
                                _message.value = "Error sending invite: ${e.message}"
                            }
                    } else {
                        _message.value = "No user found with this email"
                    }
                }
        }
    }

    // -------------------------
    // LISTEN FOR INVITES (REAL-TIME)
    // -------------------------
    fun listenForInvites() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .collection("invites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GroupViewModel", "Error listening invites", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val inviteList = snapshot.documents.map { it.data ?: emptyMap() }
                    _invites.value = inviteList
                } else {
                    _invites.value = emptyList()
                }
            }
    }

    // -------------------------
    // ACCEPT INVITE
    // -------------------------
    fun acceptInvite(groupId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Add user to group members
        val groupRef = db.collection("groups").document(groupId)
        groupRef.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                // Remove invite
                db.collection("users").document(currentUserId)
                    .collection("invites")
                    .document(groupId)
                    .delete()

                _message.value = "Invite accepted"
            }
            .addOnFailureListener { e ->
                _message.value = "Error accepting invite: ${e.message}"
            }
    }

    // -------------------------
    // REJECT INVITE
    // -------------------------
    fun rejectInvite(groupId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .collection("invites")
            .document(groupId)
            .delete()
            .addOnSuccessListener {
                _message.value = "Invite rejected"
            }
            .addOnFailureListener { e ->
                _message.value = "Error rejecting invite: ${e.message}"
            }
    }

    // -------------------------
    // LOAD GROUPS FOR USER
    // -------------------------
    fun loadGroups() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GroupViewModel", "Error loading groups", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val groupList = snapshot.documents.map { it.data ?: emptyMap() }
                    _groups.value = groupList
                }
            }
    }
    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FB", "SignIn Success: ${task.result}")
                        linkEmailPasswordIfMissing(email, password)
                        getUserData() // fetch user data
                        home()
                    } else {
                        Log.d("FB", "SignIn Failed: ${task.exception?.message}")
                        _errorMessage.value = task.exception?.localizedMessage ?: "Login failed. Please try again."
                    }
                }
        } catch (ex: Exception) {
            Log.e("FB", "SignIn Exception: ${ex.message}")
        }
    }

    // 🔹 Update Firestore User Data
    fun updateUserData(updatedUser: MUser) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val authEmail = currentUser.email ?: ""

        val userWithAuthEmail = updatedUser.copy(email = authEmail)

        firestore.collection("users")
            .document(uid)
            .set(userWithAuthEmail.toMap())
            .addOnSuccessListener {
                _user.value = userWithAuthEmail
                Log.d("FB", "User updated successfully with auth email: $authEmail")
            }
            .addOnFailureListener {
                Log.e("FB", "Error updating user: ${it.message}")
            }
    }

    fun resetPassword(email: String, onResult: (Boolean) -> Unit) {
        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }
    fun clearError() {
        _errorMessage.value = null
    }

    // 🔹 Sign Up
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        countryCode: String,
        currency: String,
        onWaitingForVerification: () -> Unit,
        onVerified: () -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        if (_loading.value == false) {
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        val displayName = fullName.ifEmpty { email.split('@')[0] }
                        createUser(displayName, phone, countryCode, currency, email)

                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Log.d("FB", "Verification email sent to ${user.email}")
                                onWaitingForVerification()

                                // Start polling
                                waitForEmailVerification(
                                    onVerified = {
                                        _loading.value = false
                                        onVerified()
                                    },
                                    onWaiting = {
                                        // can trigger UI update if needed
                                    }
                                )
                            }
                            ?.addOnFailureListener { e ->
                                _loading.value = false
                                onFailure("Failed to send verification email: ${e.message}")
                            }
                    } else {
                        _loading.value = false
                        val msg = task.exception?.message ?: "Unknown error"
                        onFailure(msg)
                    }
                }
        }
    }


//    fun checkEmailVerification(
//        onVerified: () -> Unit,
//        onNotVerified: () -> Unit
//    ) {
//        val user = auth.currentUser
//        user?.reload()?.addOnSuccessListener {
//            if (user.isEmailVerified) {
//                onVerified()
//            } else {
//                onNotVerified()
//            }
//        }
//    }

    fun waitForEmailVerification(
        onVerified: () -> Unit,
        onWaiting: () -> Unit
    ) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            while (true) {
                user.reload().await() // use kotlinx-coroutines-play-services
                if (user.isEmailVerified) {
                    Log.d("FB", "Email verified ✅")
                    onVerified()
                    break
                } else {
                    Log.d("FB", "Still waiting for verification ❌")
                    onWaiting()
                }
                delay(3000) // check every 3 seconds
            }
        }
    }



    // 🔹 Create Firestore User
    private fun createUser(
        displayName: String?,
        phone: String,
        countryCode: String,
        currency: String,
        email: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val user = MUser(
            id = null,
            userId = userId,
            displayName = displayName ?: "",
            avatarUrl = "",
            quote = "Life is great",
            profession = "Android Developer",
            phone = phone,
            countryCode = countryCode,
            currency = currency,
            email = email   // ✅ added
        ).toMap()

        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("FB", "User document successfully created!")
            }
            .addOnFailureListener { exception ->
                Log.e("FB", "Error creating user document: ${exception.message}")
            }
    }
    fun updateUserEmail(
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure(Exception("No authenticated user"))
            return
        }

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://smartsplit-99b74.firebaseapp.com/finishSignUp?email=$newEmail")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.smartsplit", true, null)
            .build()

        currentUser.verifyBeforeUpdateEmail(newEmail, actionCodeSettings)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    // 🔹 Fetch User Data
    // 🔹 Fetch User Data & Sync Email
    fun getUserData() {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val authEmail = currentUser.email ?: ""

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firestoreEmail = document.getString("email") ?: ""

                    // ✅ Always prefer Auth email
                    if (authEmail.isNotEmpty() && authEmail != firestoreEmail) {
                        firestore.collection("users")
                            .document(uid)
                            .update("email", authEmail)
                            .addOnSuccessListener {
                                Log.d("FB", "Synced Firestore email → $authEmail")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FB", "Failed syncing email: ${e.message}")
                            }
                    }

                    val user = MUser(
                        id = document.id,
                        userId = document.getString("user_id") ?: "",
                        displayName = document.getString("display_name") ?: "",
                        avatarUrl = document.getString("avatar_url") ?: "",
                        quote = document.getString("quote") ?: "",
                        profession = document.getString("profession") ?: "",
                        phone = document.getString("phone") ?: "",
                        countryCode = document.getString("country_code") ?: "",
                        currency = document.getString("currency") ?: "",
                        email = authEmail // ✅ enforce Auth email
                    )
                    _user.value = user
                } else {
                    _user.value = null
                }
            }
            .addOnFailureListener {
                _user.value = null
            }
    }


    // 🔹 Check if user already registered
    fun isUserRegistered(email: String, callback: (Boolean) -> Unit) {
        FirebaseAuth.getInstance()
            .fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                    Log.d("FB", "Fetched sign-in methods for $email: $signInMethods")

                    val isRegistered = "password" in signInMethods
                    callback(isRegistered)
                } else {
                    Log.e("FB", "Error fetching sign-in methods", task.exception)
                    callback(false)
                }
            }
    }

    // 🔹 Ensure password is linked
    private fun linkEmailPasswordIfMissing(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(normalizedEmail)
            .addOnSuccessListener { result ->
                val signInMethods = result.signInMethods
                if (signInMethods.isNullOrEmpty() || !signInMethods.contains("password")) {
                    val credential = EmailAuthProvider.getCredential(normalizedEmail, password)
                    auth.currentUser?.linkWithCredential(credential)
                        ?.addOnSuccessListener {
                            Log.d("FB", "Successfully linked email/password for $normalizedEmail")
                        }
                        ?.addOnFailureListener { exception ->
                            Log.e("FB", "Failed to link email/password: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FB", "Failed to fetch sign-in methods: ${exception.message}")
            }
    }
}

// 🔹 Updated User Model
data class MUser(
    val id: String? = null,
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val quote: String = "",
    val profession: String = "",
    val phone: String = "",
    val countryCode: String = "",
    val currency: String = "",
    val email: String = ""   // ✅ new field
) {
    fun toMap(): MutableMap<String, Any> {
        return mutableMapOf(
            "user_id" to this.userId,
            "display_name" to this.displayName,
            "quote" to this.quote,
            "profession" to this.profession,
            "avatar_url" to this.avatarUrl,
            "phone" to this.phone,
            "country_code" to this.countryCode,
            "currency" to this.currency,
            "email" to this.email     // ✅ store email in Firestore
        )
    }
}

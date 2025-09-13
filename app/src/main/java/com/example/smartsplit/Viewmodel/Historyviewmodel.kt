package com.example.smartsplit.Viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartsplit.screens.history.ActionType
import com.example.smartsplit.screens.history.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import java.util.logging.Logger

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),  // Unique ID
    val type: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedGroupId: String? = null,
    val userId: String? = null
)
enum class ActionType {
    CREATE,
    GROUP_DELETED,
    GROUP_LEFT,
    EXPENSE_ADDED
}

// Add these constants for activity types
object ActivityTypes {
    const val GROUP_DELETED = "GROUP_DELETED"
    const val GROUP_LEFT = "GROUP_LEFT"
    const val EXPENSE_ADDED = "EXPENSE_ADDED"
    const val GROUP_CREATED = "GROUP_CREATED"
}

fun logActivity(
    type: String,
    description: String,
    relatedGroupId: String? = null,
    userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val log = ActivityLog(
        type = type,
        description = description,
        relatedGroupId = relatedGroupId,
        userId = userId
    )

    FirebaseFirestore.getInstance()
        .collection("historyLogs")
        .document(log.id)
        .set(log)
        .addOnSuccessListener {
            Logger.getGlobal().info("✅ Log added for userId=$userId, type=$type")
        }
        .addOnFailureListener { e ->
            Logger.getGlobal().warning("❌ Failed to add log: ${e.message}")
        }
}
class HistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            fetchHistory(currentUserId)
        }
    }

    private fun fetchHistory(currentUserId: String) {
        Logger.getGlobal().info("📡 Fetching history for userId=$currentUserId")

        db.collection("historyLogs")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Logger.getGlobal().warning("❌ Error fetching history: ${error?.message}")
                    _history.value = emptyList()
                    return@addSnapshotListener
                }

                Logger.getGlobal().info("📡 Got ${snapshot.size()} docs for user=$currentUserId")

                val items = snapshot.documents.mapNotNull { doc ->
                    Logger.getGlobal().info("🔎 Doc data: ${doc.data}")
                    // parse item...
                    try {
                        val typeStr = doc.getString("type") ?: "CREATE"
                        val normalizedType = when (typeStr) {
                            "GROUP_CREATED" -> "CREATE"
                            else -> typeStr
                        }
                        val type = runCatching { ActionType.valueOf(normalizedType) }
                            .getOrDefault(ActionType.CREATE)


                        HistoryItem(
                            id = doc.id,
                            title = typeStr.replace("_", " ").uppercase(),
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            type = type
                        )
                    } catch (e: Exception) {
                        Logger.getGlobal().warning("❌ Parse failed for ${doc.id}: ${e.message}")
                        null
                    }
                }
                _history.value = items
            }
    }
}
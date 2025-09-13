package com.example.smartsplit.Data

import androidx.compose.foundation.background
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.GroupViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.Viewmodel.FriendBalance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.Query
import com.example.smartsplit.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.font.FontWeight

// 🔹 Your API Key
private const val GEMINI_API_KEY = "AIzaSyA7R39IYI91WKtXhculsOWofu-8IvloyZY"

// ---------- Gemini API Models ----------
data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)
data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: ContentResponse)
data class ContentResponse(val parts: List<PartResponse>)
data class PartResponse(val text: String)

// ---------- Gemini API Interface ----------
interface GeminiService {
    @POST("v1/models/gemini-1.5-flash:generateContent")
    suspend fun getGeminiResponse(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// ---------- Retrofit Client ----------
object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }
}


data class ChatMessage(val text: String, val isUser: Boolean)

// --- ChatViewModel ---
// --- ChatViewModel with API Key Rotation ---
class ChatViewModel(
    private val expenseViewModel: ExpenseViewModel,
    private val friendsViewModel: FriendsViewModel,
    private val groupViewModel: GroupViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi, I am SmartSplit AI 👋", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> get() = _messages

    private val apiKeys = listOf(
        "AIzaSyBe03fRSuY_chykK3KfKIGQLrurzq-0Lvk",
        "AIzaSyBg_qI2ikZ-p2_CVcynhRLsSndc6Hv9Brk",
        "AIzaSyA7R39IYI91WKtXhculsOWofu-8IvloyZY"
    )
    private var currentKeyIndex = 0

    private val api = GeminiApiClient.service

    fun sendMessage(userText: String) {
        _messages.value = _messages.value + ChatMessage(userText, true)

        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage("...", false)

            val context = buildContext(userText)

            val success = tryKeysWithRotation(context)

            if (!success) {
                // If all keys failed
                val withoutTyping = _messages.value.dropLast(1)
                _messages.value = withoutTyping + ChatMessage(
                    "⚠️ All API keys hit quota. Try again later.",
                    false
                )
            }
        }
    }

    // 🔹 Rotate through keys until one succeeds
    private suspend fun tryKeysWithRotation(context: String): Boolean {
        for (i in apiKeys.indices) {
            val apiKey = apiKeys[currentKeyIndex]

            try {
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(context))))
                )

                val response = api.getGeminiResponse(apiKey, request)

                val answer = response.candidates.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: "I couldn't find an answer."

                val withoutTyping = _messages.value.dropLast(1)
                _messages.value = withoutTyping + ChatMessage(answer, false)

                return true // ✅ success

            } catch (e: Exception) {
                val errorText = e.message ?: "Unknown error"

                // Log error to chat (for debugging why it's failing)
                val withoutTyping = _messages.value.dropLast(1)
                _messages.value = withoutTyping + ChatMessage(
                    "❌ API Key ${currentKeyIndex + 1} failed: $errorText",
                    false
                )

                // If it's a 429, rotate to next key
                if (errorText.contains("429")) {
                    currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
                    delay(1000) // small wait before next key
                } else {
                    return false // not a quota issue, stop
                }
            }
        }
        return false // all keys failed
    }

    private fun buildContext(userText: String): String {
        val expenses = expenseViewModel.friendExpenses.value ?: emptyList()
        val expenseData = expenses.joinToString("\n") {
            "${it.description}: ₹${it.amount} paid by ${it.paidBy}"
        }

        val friends = friendsViewModel.friends.value ?: emptyList()
        val friendData = "You have ${friends.size} friends: ${
            friends.joinToString(", ") { it.name }
        }"

        val groups = groupViewModel.myGroups.value ?: emptyList()
        val createdGroups = groups.filter { it.createdBy == getCurrentUserId() }
        val groupData = """
            You are in ${groups.size} groups: ${groups.joinToString(", ") { it.name }}.
            You created ${createdGroups.size} groups: ${createdGroups.joinToString(", ") { it.name }}.
        """.trimIndent()

        val balances = expenseViewModel.friendBalances.value ?: emptyList()
        val balanceData = balances.joinToString("\n") {
            val status = if (it.totalBalance < 0) "You owe" else "They owe you"
            "${it.friendName}: $status ₹${kotlin.math.abs(it.totalBalance)}"
        }

        val totalToPay = balances.filter { it.totalBalance < 0 }.sumOf { kotlin.math.abs(it.totalBalance) }
        val totalToReceive = balances.filter { it.totalBalance > 0 }.sumOf { it.totalBalance }

        val totalsData = """
            In total, you owe ₹$totalToPay to your friends.
            In total, your friends owe you ₹$totalToReceive.
        """.trimIndent()

        return """
            You are SmartSplit's AI assistant.
            Here is the user's app data:

            $friendData
            $groupData
            Balances with friends:
            $balanceData
            $totalsData
            Expenses:
            $expenseData

            Question: $userText
        """.trimIndent()
    }

    private fun getCurrentUserId(): String {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}


// ---------- Composable Screen ----------
@Composable
fun ChatScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    val chatViewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(expenseViewModel, friendsViewModel, groupViewModel) as T
            }
        }
    )

    var query by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()

    // LazyColumn scroll state
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when a new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0) // 0 since reverseLayout = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .statusBarsPadding() // ✅ extra top spacing
            .imePadding()         // ✅ pushes content above keyboard
            .padding(horizontal = 16.dp)
    ) {
        // Title Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SmartSplit AI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            )
        }

        // Chat Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() || (messages.size == 1 && !messages.first().isUser)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chatgpt_image_aug_22__2025__10_29_43_pm),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "SmartSplit AI Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Gray
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages.reversed()) { message ->
                        ChatBubble(message)
                    }
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .navigationBarsPadding(), // ✅ safe with gesture nav
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                "Type your message…",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        inner()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        chatViewModel.sendMessage(query)
                        query = ""
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Send", color = Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) Color(0xFFDCF8C6) else Color.White
    val textColor = Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(12.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// --- Data Model ---


// --- ViewModel ---
class ChatViewModel1(
    private val expenseViewModel: ExpenseViewModel,
    private val friendsViewModel: FriendsViewModel,
    private val groupViewModel: GroupViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi, I am SmartSplit AI 👋", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> get() = _messages

    fun sendMessage(userText: String) {
        val updated = _messages.value + ChatMessage(userText, true)
        _messages.value = updated

        viewModelScope.launch {
            // Show typing indicator
            _messages.value = _messages.value + ChatMessage("...", false)

            delay(1000) // simulate thinking

            // Remove typing indicator + add AI response
            val withoutTyping = _messages.value.dropLast(1)
            _messages.value = withoutTyping + ChatMessage(
                generateAIResponse(userText),
                false
            )
        }
    }

    private fun generateAIResponse(query: String): String {
        // Replace this with actual AI logic
        return "You asked: \"$query\". Here’s what I found from your SmartSplit data."
    }
}

package com.example.smartsplit.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartsplit.R
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.Viewmodel.MUser
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen1(
    navController: NavController,
    isSignup: Boolean = false,
    userName: String = "Sanskruti",
    viewModel: LoginScreenViewModel = viewModel()
) {
    val user by viewModel.user.observeAsState()

    // Fetch data when screen loads
    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }
    val firstName = remember(user) {
        user?.displayName?.split(" ")?.firstOrNull()
            ?: userName
    }
    var showCard by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        showText = true
        delay(600)
        showCard = true
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBE0))
            .clickable{navController.navigate("onboardscreen2")}
    ) {
        Image(
            painter = painterResource(id = R.drawable.obimg1),
            contentDescription = "Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Animated Text ---
            AnimatedVisibility(
                visible = showText,
                enter = slideInHorizontally(
                    initialOffsetX = { -it }, // comes from left
                    animationSpec = tween(600)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {Text(
                    text = if (isSignup) "Welcome $firstName 👋"
                    else "Welcome back $firstName 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF222222)
                )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Track your shared bills and see who owes whom.",
                        fontSize = 17.sp,
                        color = Color(0xFF666666),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            AnimatedVisibility(
                visible = showCard,
                enter = fadeIn(animationSpec = tween(600)) +
                        expandVertically(animationSpec = tween(600))
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "💰 Balance Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0D47A1)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏖 Weekend Trip", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Owe ₹750", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍕 Pizza Night", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Lent ₹300", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎬 Movie Tickets", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Owe ₹150", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(350.dp))

            // Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF0D47A1), RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50)) // active
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
            }
        }
    }
}



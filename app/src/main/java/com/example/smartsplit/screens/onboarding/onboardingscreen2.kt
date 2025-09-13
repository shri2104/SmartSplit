package com.example.smartsplit.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.example.smartsplit.R

@Composable
fun OnboardingScreen2(navController: NavHostController) {
    var typedText by remember { mutableStateOf("") }
    val targetText = "94.50"

    var showCard by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showHeader by remember { mutableStateOf(false) }

    // Typing effect
    LaunchedEffect(Unit) {
        typedText = ""
        targetText.forEachIndexed { index, _ ->
            delay(150)
            typedText = targetText.substring(0, index + 1)
        }
    }

    // Sequence of animations
    LaunchedEffect(Unit) {
        delay(300)
        showHeader = true
        delay(400)
        showText = true
        delay(600)
        showCard = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF7F7))
            .clickable{navController.navigate("onboardscreen3")}
    ) {
        Image(
            painter = painterResource(id = R.drawable.obimg2),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Top Animated Text ---
            AnimatedVisibility(
                visible = showHeader,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 700)
                ) + fadeIn(animationSpec = tween(700))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Add expenses",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )
                    Spacer(modifier = Modifier.height(14.dp)) // More spacious
                    Text(
                        text = "You can split expenses with groups or with individuals.",
                        fontSize = 17.sp,
                        color = Color(0xFF666666),
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp)) // Space between header and card

            // --- Card with animation ---
            AnimatedVisibility(
                visible = showText,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 600)
                ) + fadeIn(animationSpec = tween(durationMillis = 600))
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Groceries",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Groceries",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF222222)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF222222)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Typing animation number here
                            TypingNumberAnimation("94.50")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(350.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF0D47A1), RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
            }

        }



        // --- Pager Indicator ---

    }
}


@Composable
fun TypingNumberAnimation(targetText: String) {
    var typedText by remember { mutableStateOf("") }

    LaunchedEffect(targetText) {
        typedText = ""
        targetText.forEachIndexed { index, char ->
            delay(250)
            typedText = targetText.substring(0, index + 1)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = typedText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF222222)
        )
        if (typedText.length < targetText.length) {
            Text(
                text = "|",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}




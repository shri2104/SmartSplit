package com.example.smartsplit.navigation

import CreateGroupScreen
import NewGroupScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartsplit.Component.FriendRequestsScreen
import com.example.smartsplit.Data.ChatScreen
import com.example.smartsplit.LaunchAnimationAppName
import com.example.smartsplit.screens.Friends.AddFriendScreen

import com.example.smartsplit.screens.Friends.FriendsScreen
import com.example.smartsplit.screens.Groups.AddExpenseScreen
import com.example.smartsplit.screens.Groups.paytonanyone
import com.example.smartsplit.screens.Homescreen.GroupSectionScreen
import com.example.smartsplit.screens.Loginscreen.LoginScreen
import com.example.smartsplit.screens.Loginscreen.SignupScreen
import com.example.smartsplit.screens.Loginscreen.Welcomscreen
import com.example.smartsplit.screens.Profile.ChangeNameScreen
import com.example.smartsplit.screens.Profile.ChangePhoneNumberScreen
import com.example.smartsplit.screens.Profile.DarkModeSettingsScreen
import com.example.smartsplit.screens.Profile.DeleteAccount
import com.example.smartsplit.screens.Profile.LanguageScreen
import com.example.smartsplit.screens.Profile.ProfileScreen
import com.example.smartsplit.screens.Profile.UpdateEmailScreen
import com.example.smartsplit.screens.history.HistoryScreen
import com.example.smartsplit.screens.onboarding.OnboardingScreen1
import com.example.smartsplit.screens.onboarding.OnboardingScreen2
import com.example.smartsplit.screens.onboarding.OnboardingScreen3

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("Welcomscreen") { Welcomscreen(navController) }
        composable("splash") { LaunchAnimationAppName(navController) }
        composable(
            "onboardscreen1?isSignup={isSignup}",
            arguments = listOf(navArgument("isSignup") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val isSignup = backStackEntry.arguments?.getBoolean("isSignup") ?: false
            OnboardingScreen1(navController, isSignup)
        }
        composable("onboardscreen2") { OnboardingScreen2(navController) }
        composable("onboardscreen3") { OnboardingScreen3(navController) }
        composable("Group") { GroupSectionScreen(navController) }
        composable("Signup") { SignupScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("creategroup") { CreateGroupScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("friends") { FriendsScreen(navController) }
        composable("addFriend") { AddFriendScreen(navController) }
        composable("notification") { FriendRequestsScreen(navController) }
        composable("GroupOverview/{createdGroupId}") { backStackEntry ->
            val createdGroupId = backStackEntry.arguments?.getString("createdGroupId") ?: ""
            NewGroupScreen(
                navController = navController,
                groupId = createdGroupId
            )
        }
        composable("updateEmail") {
            UpdateEmailScreen(
                navController = navController)
        }
        composable("language") {
            LanguageScreen(navController)
        }
        composable("changeName") {
            ChangeNameScreen(navController)
        }
        composable("changephone") {
            ChangePhoneNumberScreen(navController)
        }
        composable("darkMode") {
            DarkModeSettingsScreen(navController = navController)
        }
        composable("deletaccount") {
            DeleteAccount(navController = navController)
        }
        composable("deletaccount") {
            DeleteAccount(navController = navController)
        }
        composable("addexpense") {
            AddExpenseScreen(navController = navController)
        }
        composable("paytonanyone") {
            paytonanyone()
        }
        composable("smartsplitai") {
            ChatScreen()
        }
    }
}
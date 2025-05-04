package com.github.wh5.mychat.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

import com.github.wh5.mychat.ui.chat.ChatListScreen
import com.github.wh5.mychat.ui.friend.FriendListScreen
import com.github.wh5.mychat.ui.profile.ProfileScreen
import com.github.wh5.mychat.ui.profile.EditProfileScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOf(
                    BottomNavItem("chat_list", "聊天", Icons.Filled.Chat),
                    BottomNavItem("friend_list", "好友", Icons.Filled.Group),
                    BottomNavItem("profile", "我的", Icons.Filled.Person)
                ).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "chat_list",  // 确保从这里开始，不是从 main
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("chat_list") { ChatListScreen() }
            composable("friend_list") { FriendListScreen() }
            composable("profile") { ProfileScreen(navController) }
            composable("edit_profile") {
                EditProfileScreen(navController)
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
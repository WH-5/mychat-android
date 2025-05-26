package com.github.wh5.mychat.ui.main

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext


import android.os.Build
import androidx.annotation.RequiresApi
import com.github.wh5.mychat.ui.friend.FriendRequestScreen

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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.github.wh5.mychat.data.local.LoginPreferences.getTokenOnce
import com.github.wh5.mychat.data.remote.ws.WebSocketManager

import com.github.wh5.mychat.ui.chat.ChatListScreen
import com.github.wh5.mychat.ui.friend.AddFriendScreen
import com.github.wh5.mychat.ui.friend.FriendListScreen
import com.github.wh5.mychat.ui.friend.FriendProfileScreen
import com.github.wh5.mychat.ui.profile.ProfileScreen
import com.github.wh5.mychat.ui.profile.EditProfileScreen
import com.github.wh5.mychat.viewmodel.FriendViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val token = getTokenOnce(context)
        if (token.isNotBlank()) {
            android.util.Log.d("MainScreen", "连接 WebSocket，token 前5位：${token.take(5)}")
            WebSocketManager.connect(token) { incoming ->
                android.util.Log.d("WebSocket", "收到消息：$incoming")
            }
        } else {
            android.util.Log.d("MainScreen", "token 为空，未连接 WebSocket")
        }
    }

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
            composable("chat_list") { ChatListScreen(navController = navController) }
            composable("friend_list") {
                val friendViewModel: FriendViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                FriendListScreen(navController = navController, viewModel = friendViewModel)
            }
            composable("profile") { ProfileScreen(navController) }
            composable("edit_profile") {
                EditProfileScreen(navController)
            }
            composable("friend_requests") {
                val context = androidx.compose.ui.platform.LocalContext.current
                val friendViewModel: FriendViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                FriendRequestScreen(navController = navController, context = context, viewModel = friendViewModel)
            }
            composable("add_friend") {
                AddFriendScreen(navController = navController)
            }
            composable(
                route = "friend_detail/{friendId}",
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId")
                if (friendId != null) {
                    FriendProfileScreen(friendId = friendId, navController = navController)
                }
            }
            composable(
                route = "chat_window/{friendId}",
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId")
                if (friendId != null) {
                    com.github.wh5.mychat.ui.chat.ChatWindowScreen(navController = navController, friendId = friendId)
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
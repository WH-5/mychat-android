package com.github.wh5.mychat.ui.splash

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.wh5.mychat.data.local.LoginPreferences
import kotlinx.coroutines.delay
import com.github.wh5.mychat.viewmodel.LoginViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    userPreferences: LoginPreferences
) {
    val context = LocalContext.current
    val viewModel = remember { SplashViewModel() }
    val loginChecked = remember { mutableStateOf(false) }

    // 自动登录逻辑
    LaunchedEffect(Unit) {
        val token = LoginPreferences.getTokenOnce(context)
        if (token.isNullOrEmpty()) {
            loginChecked.value = true
        } else {
            viewModel.autoLogin(context) {
                loginChecked.value = true
            }
        }
    }

    // 确保一旦 loginChecked 设置为 true，就根据登录状态导航
    LaunchedEffect(loginChecked.value) {
        if (loginChecked.value) {
            delay(500)
            val loggedIn = viewModel.isLoggedIn.value
            if (loggedIn == true) {
                Log.d("SplashScreen", "Navigating to main")
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                Log.d("SplashScreen", "Navigating to login")
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MyChat", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }
    }
}
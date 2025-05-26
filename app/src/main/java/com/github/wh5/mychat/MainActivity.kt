package com.github.wh5.mychat

// MainActivity.kt
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mychat.ui.login.LoginScreen
import com.github.wh5.mychat.ui.login.RegisterScreen
import com.github.wh5.mychat.ui.main.MainScreen

import com.github.wh5.mychat.ui.theme.MychatTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.wh5.mychat.viewmodel.LoginViewModel
import com.github.wh5.mychat.ui.splash.SplashScreen
import com.github.wh5.mychat.data.local.LoginPreferences
import androidx.compose.ui.platform.LocalContext
import com.github.wh5.mychat.data.remote.ws.WebSocketManager

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MychatTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                NavHost(navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(navController = navController, userPreferences = LoginPreferences)
                    }
                    composable("login") {
                        val loginViewModel: LoginViewModel = viewModel()
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                LoginPreferences.getTokenAsync(context) { token ->
                                    if (token.isNotBlank()) {
                                        WebSocketManager.connect(token) { incoming ->
                                            android.util.Log.d("WebSocket", "收到消息：$incoming")
                                        }
                                    }
                                    navController.navigate("main")
                                }
                            },
                            onGoRegister = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            context = this@MainActivity,
                            onRegisterSuccess = {
                                Toast.makeText(this@MainActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                                navController.navigate("login")
                            },
                            onBackToLogin = { navController.popBackStack("login", inclusive = false) }
                        )
                    }
                    composable("main") {
                        MainScreen()
                    }
                }
            }
        }
    }
}
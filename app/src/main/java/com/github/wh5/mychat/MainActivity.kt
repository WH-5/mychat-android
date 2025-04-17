package com.github.wh5.mychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.wh5.mychat.ui.ChatScreen
import com.github.wh5.mychat.ui.theme.MychatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MychatTheme {
                ChatScreen()
            }
        }
    }
}
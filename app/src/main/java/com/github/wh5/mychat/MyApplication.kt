package com.github.wh5.mychat

import android.app.Application
import android.util.Log
import com.github.wh5.mychat.data.local.LoginPreferences.getTokenOnce
import com.github.wh5.mychat.data.remote.ws.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        Log.d("MyApplication", "onCreate() 被调用")
    }
}
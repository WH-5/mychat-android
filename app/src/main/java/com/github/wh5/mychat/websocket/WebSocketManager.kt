package com.github.wh5.mychat.websocket

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.Timer
import java.util.TimerTask

object WebSocketManager {
    private const val TAG = "WebSocketManager"
    private const val SERVER_URL = "ws://192.168.31.192:8002/ws" // Android 模拟器访问本地服务

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val reconnectInterval: Long = 10000 // 10 seconds
    private var reconnectTimer: Timer? = null
    private val pingInterval: Long = 30000 // 30 seconds for ping interval

    fun connect(onMessageReceived: (String) -> Unit) {
        val request = Request.Builder()
            .url(SERVER_URL)
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDUxMzY1MTksImlhdCI6MTc0NDg3NzMxOSwic2Vzc2lvbiI6ImVjZTVkYWUxLTEwMjctNDBjNC05NTAzLWI4NmRkNDBmNjhkMyIsInVzZXJfaWQiOjJ9.lmgnuOAaaEP1KQS6Dt_U9D_5DydmznxPmdhaO26A4fo")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                reconnectTimer?.cancel() // Stop reconnecting when connected
                startPing() // Start sending ping after connection is open
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text == "pong") {
                    Log.d(TAG, "收到服务器心跳 pong")
                } else {
                    Log.d(TAG, "Received message: $text")
                    onMessageReceived(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code / $reason")
                isConnected = false
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code / $reason")
                isConnected = false
                scheduleReconnect(onMessageReceived) // Start reconnecting if closed
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                isConnected = false
                scheduleReconnect(onMessageReceived) // Start reconnecting if failed
            }


        })
    }

    fun send(message: String) {
        Log.d(TAG, "Sending message: $message")
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "User disconnected")
    }

    private fun scheduleReconnect(onMessageReceived: (String) -> Unit) {
        if (reconnectTimer != null) return // already scheduled
        reconnectTimer = Timer()
        reconnectTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (!isConnected) {
                    Log.d(TAG, "Trying to reconnect WebSocket...")
                    connect(onMessageReceived)
                } else {
                    reconnectTimer?.cancel()
                    reconnectTimer = null
                }
            }
        }, reconnectInterval, reconnectInterval)
    }

    private fun startPing() {
        // Create a timer that sends a ping every 30 seconds
        Timer().schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "Sending ping")
                webSocket?.send(ByteString.EMPTY) // Send real WebSocket ping frame
            }
        }, 0, pingInterval)
    }
}
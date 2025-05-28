package com.github.wh5.mychat.data.remote.ws

import android.util.Log
import com.github.wh5.mychat.common.AppConfig
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import org.json.JSONObject

object WebSocketManager : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // WebSocket 要求不超时
            .build()
    }

    private var onMessageCallback: ((String) -> Unit)? = null
    private var pingJob: Job? = null

    private var lastToken: String? = null
    private var reconnectJob: Job? = null

    fun connect(token: String, onMessage: (String) -> Unit) {
        lastToken = token
        onMessageCallback = onMessage
        Log.d("WebSocket", "调用 connect，目标地址：${AppConfig.BASE_URL.replace("http", "ws")}push/ws，token 前5位：${token.take(5)}")

        val request = Request.Builder()
            .url(AppConfig.BASE_URL.replace("http", "ws") + "push/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "连接成功")
        startPingLoop()
    }

    private fun startPingLoop() {
        pingJob?.cancel()
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(50_000)//50s
                val pingMsg = """{"type": 0}"""
                send(pingMsg)
                Log.d("WebSocket", "发送心跳 ping: $pingMsg")
            }
        }
    }

    fun sendMessage(to: String, content: String, type: String = "text") {
        val payloadJson = JSONObject().apply {
            put("content", content)
            put("message_type", type)
        }

        val messageJson = JSONObject().apply {
            put("type", 1)
            put("target_unique", to)
            put("payload", payloadJson)
        }

        Log.d("sendmessagelook", "发送了一个文本消息: $messageJson")

        send(messageJson.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "收到文本消息: $text")

        try {
            val json = JSONObject(text)
            if (json.getInt("type") == 1) {
                val from = json.getString("unique_id")
                val timestamp = json.getString("timestamp")
                val payloadBase64 = json.getString("payload")
                val payloadStr = String(android.util.Base64.decode(payloadBase64, android.util.Base64.DEFAULT))
                Log.d("WebSocket", "解码后的 payload 内容: $payloadStr")
                val payloadJson = JSONObject(payloadStr)
                val innerPayloadBase64 = payloadJson.optString("payload")
                val innerPayloadStr = String(android.util.Base64.decode(innerPayloadBase64, android.util.Base64.DEFAULT))
                Log.d("WebSocket", "二次解码后的消息内容: $innerPayloadStr")
                val innerPayloadJson = JSONObject(innerPayloadStr)
                val content = innerPayloadJson.optString("content")
                val messageType = innerPayloadJson.optString("message_type")

                Log.d("WebSocket", "来自 $from 的消息: $content [$messageType] @ $timestamp")

                CoroutineScope(Dispatchers.IO).launch {
                    val context = com.github.wh5.mychat.MyApplication.instance.applicationContext
                    val userId = com.github.wh5.mychat.data.local.LoginPreferences.getUniqueIdOnce(context)
                    val message = com.github.wh5.mychat.data.local.MessageEntity(
                        userId = userId,
                        friendId = from,
                        content = content,
                        timestamp = timestamp,
                        isMe = false
                    )
                    com.github.wh5.mychat.data.local.AppDatabase
                        .getDatabase(context)
                        .messageDao()
                        .insertMessage(message)
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "解析消息失败: ${e.message}")
        }

        onMessageCallback?.invoke(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("WebSocket", "收到二进制消息: $bytes")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "正在关闭: $code / $reason")
        pingJob?.cancel()
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "连接已关闭: $code / $reason")
        attemptReconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "连接失败: ${t.message}", t)
        pingJob?.cancel()
        attemptReconnect()
    }

    private fun attemptReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            lastToken?.let {
                Log.d("WebSocket", "尝试重新连接 WebSocket")
                connect(it, onMessageCallback ?: {})
            }
        }
    }

    fun send(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }

    fun close() {
        webSocket?.close(1000, "手动关闭")
        webSocket = null
    }

    fun setOnMessageCallback(callback: (String) -> Unit) {
        onMessageCallback = callback
    }
}



# MyChat 安卓客户端

这是即时聊天应用的 Android 客户端，使用 Kotlin 开发，支持用户登录、好友添加、消息收发等功能。

## 📱 技术栈

- Kotlin
- Jetpack Compose（用于构建 UI）
- Retrofit + OkHttp（用于网络请求）
- WebSocket（用于即时消息通信）
- Room（本地存储聊天记录）
- ViewModel + StateFlow（状态管理）
- Hilt（依赖注入，可选）

## 🧱 模块结构

- `auth`：登录与注册功能模块
- `chat`：聊天界面，消息收发
- `contacts`：好友列表、添加与审批
- `network`：与服务端通信（API + WebSocket）
- `data`：本地数据库管理
- `common`：公共组件、样式、工具函数等

## 🚀 启动流程

1. 启动 App，检查是否已登录（本地 token）
2. 已登录则连接 WebSocket，加载好友和聊天记录
3. 未登录则跳转登录页面
4. 聊天过程中，消息发送到服务器并本地保存
5. 接收到新消息实时展示并写入数据库

## ✅ 最小可运行版本（MVP）

- 登录界面（账号密码登录）
- 聊天主界面（静态好友列表）
- 聊天窗口（发送/接收文本消息）

后续会逐步完善好友管理、通知、聊天记录分页等功能。

📦 MVP 建议开发顺序
1.	登录页面（Compose 都行）
2.	创建 network 模块，用 Retrofit 封装登录 API
3.	登录成功后进入主界面（用 Navigation 管理页面跳转）
4.	聊天页面：连接 WebSocket，发送和接收消息

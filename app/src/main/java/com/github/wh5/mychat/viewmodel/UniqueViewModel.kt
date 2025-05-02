package com.github.wh5.mychat.viewmodel

data class UniqueIdRequest(
    val uniqueId: String,
    val newUniqueId: String
)

data class UniqueIdReply(
    val msg: String,
    val newUniqueId: String
)
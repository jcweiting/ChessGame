package com.joyce.chessgame.server.bean

data class ActionData(
    val actionType: Long,
    val host: String,
    val time: Long,
    val roomId: String,
    val documentId: String,
    val roomName: String,
    val player2 :String,
    val x :Long,
    val y :Long,
    val whoPress:Long
)

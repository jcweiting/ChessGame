package com.joyce.chessgame.server.bean

data class GameRoomData (
    val roomId:String,
    val host :String ,
    val player2:String ,
    val timeStamp:Long ,
    val status:Long,
    val documentId:String,
    val roomName:String
)
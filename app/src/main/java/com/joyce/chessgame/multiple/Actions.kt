package com.joyce.chessgame.multiple

class Actions{
    var actionType: Long? = null    //1:創建房間, 2:發出對弈請求, 4:加入房間
    var host: String? = null
    var time: Long? = null
    var roomName: String? = null
    var roomId: String? = null
    var player2: String? = null

    /**創建房間*/
    constructor(actionType: Long? = null, host: String? = null, time: Long? = null, roomName: String? = null){
        this.actionType = actionType
        this.host = host
        this.time = time
        this.roomName = roomName
    }

    /**發出對弈請求*/
    constructor(actionType: Long? = null, roomId: String? = null){
        this.actionType = actionType
        this.roomId = roomId
    }

    /**加入房間*/
    constructor(actionType: Long? = null, roomId: String? = null, player2: String? = null){
        this.actionType = actionType
        this.roomId = roomId
        this.player2 = player2
    }
}
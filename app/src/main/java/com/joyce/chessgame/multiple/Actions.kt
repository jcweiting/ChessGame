package com.joyce.chessgame.multiple

class Actions{
    var actionType: Long? = null    //1:創建房間, 2:發出對弈請求, 3:加入房間, 5:下棋, 6:離開房間, 7:發送對弈結果
    var host: String? = null
    var time: Long? = null
    var roomName: String? = null
    var roomId: String? = null
    var player2: String? = null
    var x: Long? = null
    var y: Long? = null
    var whoPress: Long? = null
    var whoLeave: Long? = null
    var who_win: String? = null      //0:房主, 1:另一個用戶

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
    constructor(actionType: Long? = null, roomId: String? = null, player2: String = "", who_win: String = ""){
        this.actionType = actionType
        this.roomId = roomId
        this.player2 = player2
        this.who_win = who_win
    }

    /**傳送下棋位置*/
    constructor(actionType: Long? = null, x: Long? = null, y: Long? = null, whoPress: Long? = null, roomId: String? = null){
        this.actionType = actionType
        this.x = x
        this.y = y
        this.whoPress = whoPress
        this.roomId = roomId
    }

    /**離開房間*/
    constructor(actionType: Long? = null, whoLeave: Long? = null, roomId: String? = null){
        this.actionType = actionType
        this.whoLeave = whoLeave
        this.roomId = roomId
    }
}
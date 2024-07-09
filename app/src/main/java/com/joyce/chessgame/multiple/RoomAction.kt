package com.joyce.chessgame.multiple

class RoomAction(
    var host: String? = null,
    var msg: String? = null,
    var player2: String? = null,
    var roomId: String? = null,
    var roomName: String? = null,
    var second: Int = 0,       //倒數計時顯示
    var status: Int = 0,       //0:等待中, 2:開始倒數, 4:開始對弈
    var whoFirst: Int = 0,     //0:房主, 1:另一個用戶
    var whoTurn: Int = 0       //0:房主, 1:另一個用戶
)
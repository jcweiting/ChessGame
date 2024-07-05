package com.joyce.chessgame.base

data class UserData (
    var active: Boolean = false,
    var email: String? = null,
    var timeStamp: Long = 0,
    var loginType: String? = null
)
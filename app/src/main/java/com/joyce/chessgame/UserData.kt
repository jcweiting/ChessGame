package com.joyce.chessgame

data class UserData (
    var active: Boolean = false,
    var email: String? = null,
    var timeStamp: Long = 0,
    var loginType: String? = null
)
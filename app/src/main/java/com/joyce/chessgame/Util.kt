package com.joyce.chessgame

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Util {

    fun getString(id: Int): String{
        return MyApplication.instance.getContext().getString(id)
    }

    fun getCurrentTime():String{
        val sdf = SimpleDateFormat("MM/dd hh:mm", Locale.ROOT)
        return sdf.format(Date(System.currentTimeMillis()))
    }

    fun hideEmail(host: String): String {
        val atIndex = host.indexOf('@')
        if (atIndex == -1) {
            return "Invalid email format"
        }

        val namePart = host.substring(0, atIndex)

        val visiblePart = if (namePart.length <= 4) {
            namePart
        } else {
            namePart.take(4)
        }

        return "$visiblePart*****"
    }

}
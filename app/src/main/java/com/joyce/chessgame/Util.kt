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

}
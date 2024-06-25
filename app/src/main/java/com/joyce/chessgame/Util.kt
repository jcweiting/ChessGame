package com.joyce.chessgame

object Util {

    fun getString(id: Int): String{
        return MyApplication.instance.getContext().getString(id)
    }

}
package com.joyce.gomoku

class Point (var centerX: Float, var centerY: Float, var index: Int){

    //默認狀態
    var status = PointStatus.NORMAL

    fun setStatusNormal(){
        status = PointStatus.NORMAL
    }

}

/**點的狀態*/
enum class PointStatus {
    NORMAL, PRESSED
}
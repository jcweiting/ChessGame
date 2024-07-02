package com.joyce.chessgame.game_board

class Point(var centerX: Float, var centerY: Float, var index: Int){

    //默認狀態
    var status = PointStatus.NORMAL

    fun setStatusNormal(){
        status = PointStatus.NORMAL
    }
}

class ChessPoint(var isBlackChess: Boolean, var centerX: Float, var centerY: Float){
    fun copy(): ChessPoint {
        return ChessPoint(isBlackChess, centerX, centerY)
    }
}

/**點的狀態*/
enum class PointStatus {
    NORMAL, PRESSED
}
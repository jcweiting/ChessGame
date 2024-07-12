package com.joyce.chessgame.base

class Point(var centerX: Float, var centerY: Float, var index: Int)

class ChessPoint(var isBlackChess: Boolean, var centerX: Float, var centerY: Float){
    fun copy(): ChessPoint {
        return ChessPoint(isBlackChess, centerX, centerY)
    }
}
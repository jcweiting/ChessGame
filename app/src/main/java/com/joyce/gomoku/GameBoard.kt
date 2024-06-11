package com.joyce.gomoku

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlin.math.hypot

class GameBoard: View {

    private var mPoints: ArrayList<ArrayList<Point?>> = ArrayList()
    private var mStonesArr: ArrayList<ChessPoint> = ArrayList()
    private var listener: OnGameBoardListener? = null
    private var gameBoardCells = 10
    private var screenWidth = 0f        //螢幕寬度
    private var screenHeight = 0f       //螢幕高度
    private var dotRadius = 0           //外圓半徑
    private var isBlackChess = true     //true:黑子, false:白子

    fun setGameBoardListener(listener: OnGameBoardListener){
        this.listener = listener
    }

    //畫筆
    private lateinit var mNormalPaint: Paint
    private lateinit var mStonePaintBlack: Paint
    private lateinit var mStonePaintWhite: Paint

    //畫筆顏色
    private val mNormalColor = ContextCompat.getColor(context, R.color.black)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defaultStyle: Int): super(context, attrs, defaultStyle)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        initPaint()
        initGameBoard()
        drawShow(canvas)
        drawStone(canvas)
    }

    /**畫棋子*/
    private fun drawStone(canvas: Canvas?) {
        for (stone in mStonesArr){
            if (stone.isBlackChess){
                canvas?.drawCircle(stone.centerX, stone.centerY, dotRadius.toFloat(), mStonePaintBlack)
            } else {
                canvas?.drawCircle(stone.centerX, stone.centerY, dotRadius.toFloat(), mStonePaintWhite)
            }
        }
    }

    /**畫筆預設值*/
    @SuppressLint("ResourceType")
    private fun initPaint() {
        //棋盤格線
        mNormalPaint = Paint().apply {
            color = mNormalColor
            isAntiAlias = true             //是否抗鋸齒
            style = Paint.Style.STROKE     //樣式：Stroke 描邊; Fill 填充; Fill and Stroke 描邊加填充
            strokeWidth = 5f               //寬度
        }

        //黑子
        mStonePaintBlack = Paint().apply {
            color = mNormalColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        //白子
        mStonePaintWhite = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    private fun initGameBoard() {
        val squareWidth = width / gameBoardCells    //每個方格的大小
        dotRadius = squareWidth / 4     //中心點座標

        var count = 0

        mPoints.clear()
        for (i in 0..10){
            val row = ArrayList<Point?>()   //創建一個新的行
            for (j in 0..10){
                count++
                val point = Point(screenWidth/gameBoardCells * j, screenHeight/gameBoardCells * i, count)
                row.add(point)
            }
            mPoints.add(row)
        }
    }

    private fun drawShow(canvas: Canvas?) {
        //棋盤格線(直線)
        for (i in 0 .. gameBoardCells){
            canvas?.drawLine(screenWidth/gameBoardCells*i, 0f, screenWidth/gameBoardCells*i, screenHeight, mNormalPaint)
        }

        //棋盤格線(橫線)
        for (j in 0 .. gameBoardCells){
            canvas?.drawLine(0f, screenHeight/gameBoardCells*j, screenWidth, screenHeight/gameBoardCells*j, mNormalPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //決定棋盤的尺寸應該是寬度和高度中的最小值
        val size = Math.min(widthSize, heightSize)

        //更新螢幕寬度和高度變量
        screenWidth = size.toFloat()
        screenHeight = size.toFloat()

        //設置測量的寬度和高度都為計算出的尺寸，保證視圖是正方形
        setMeasuredDimension(size, size)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN){
            val x = event.x     //取得觸摸的x座標
            val y = event.y     //取得觸摸的y座標

            if (isWithinBounds(x, y)){
                handleTouch(x, y)
            }
        }
        return true
    }

    /**是否在邊界內*/
    private fun isWithinBounds(x: Float, y: Float): Boolean {
        val eachHalfWidth = width / gameBoardCells / 2     //半格的寬度
        val checkX = !(x <= eachHalfWidth || x >= width - eachHalfWidth)

        val eachHalfHeight = height / gameBoardCells / 2   //半格的高度
        val checkY = !(y <= eachHalfHeight || y >= (height - eachHalfHeight))

        GameLog.i("checkX = $checkX , checkY = $checkY")
        return !(!checkX || !checkY)
    }

    private fun handleTouch(x: Float, y: Float) {
        val nearestPoint = findNearestPoint(x, y)

        nearestPoint?.let {
            if (!checkIfSamePosition(it)){
                placeStone(it)
            }
        }
    }

    /**確認是否有相同座標*/
    private fun checkIfSamePosition(nearestPoint: ChessPoint): Boolean{
        var isSamePosition = false
        for (mStone in mStonesArr){
            if (mStone.centerX == nearestPoint.centerX && mStone.centerY == nearestPoint.centerY){
                isSamePosition = true
                break
            }
        }
        return isSamePosition
    }

    /**放置旗棋子*/
    private fun placeStone(point: ChessPoint) {
        GameLog.i("棋子座標 = ${Gson().toJson(point)}")
        mStonesArr.add(point)           //將棋子座標加到列表中

        if (checkConnectInLine()){
            listener?.onConnectInLine(isBlackChess)
        } else {
            updateGameBoard()
        }
    }

    private fun updateGameBoard(){
        isBlackChess = !isBlackChess    //儲存落子之後換玩家
        listener?.onChangePlayer(isBlackChess)
        invalidate()                    //重繪視圖
    }

    /**確認連線*/
    private fun checkConnectInLine(): Boolean {
        var blackInLine = false
        var whiteInLine = false
        val whiteChessArr = ArrayList<ChessPoint>()
        val blackChessArr = ArrayList<ChessPoint>()

        //把黑白棋arr分開
        for (stones in mStonesArr){
            if (stones.isBlackChess){
                blackChessArr.add(stones)
            } else {
                whiteChessArr.add(stones)
            }
        }

        when(isBlackChess){
            //比對黑棋
            true -> blackInLine = if (blackChessArr.size < 5) false else isChessArrConnectInLine(blackChessArr)
            //比對白棋
            false -> whiteInLine = if (whiteChessArr.size < 5) false else isChessArrConnectInLine(whiteChessArr)
        }

        return blackInLine || whiteInLine
    }

    private fun isChessArrConnectInLine(chessArray: ArrayList<ChessPoint>): Boolean{
        GameLog.i("chessArr = ${Gson().toJson(chessArray)}")
        var isInLine = false

        val blackChessArr = ArrayList<ChessPoint>()
        val whiteChessArr = ArrayList<ChessPoint>()

        for (chess in chessArray){
            when(chess.isBlackChess){
                true -> blackChessArr.add(chess)
                false -> whiteChessArr.add(chess)
            }
        }

        for (i in 0 until blackChessArr.size){
            if (i != blackChessArr[i].centerX == blackChessArr[i].centerX)
        }

//        for (chess in chessArray){
//
//            //1.都是直線
//
//
//            //2.都是橫線
//
//
//            //3.斜線
//
//
//        }

        return isInLine
    }

    /**找到最近的交界點*/
    private fun findNearestPoint(x: Float, y: Float): ChessPoint? {
        val minDistance = width / gameBoardCells / 2    //每一格的半徑
        var nearestPoint: ChessPoint? = null

        for (row in mPoints){
            for (point in row){
                point?.let {
                    val distance = hypot((it.centerX - x).toDouble(), (it.centerY - y).toDouble()).toFloat()    //計算點到觸摸位置的距離
                    if (distance < minDistance){    //如果這個距離小於之前的最小距離
                        nearestPoint = ChessPoint(isBlackChess, it.centerX, it.centerY)   //更新最近的點
                    }
                }
            }
        }
        return nearestPoint
    }

    fun onClearGameBoard(){
        mStonesArr.clear()
        invalidate()
    }

    interface OnGameBoardListener{
        fun onChangePlayer(isBlackChess: Boolean)
        fun onConnectInLine(isBlackChess: Boolean)
    }
}
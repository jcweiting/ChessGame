package com.joyce.gomoku

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
    private var mStonesBlack: ArrayList<Point> = ArrayList()
    private var mStonesWhite: ArrayList<Point> = ArrayList()
    private var screenWidth = 0f        //螢幕寬度
    private var screenHeight = 0f       //螢幕高度
    private var dotRadius = 0           //外圓半徑
    private var listener: OnGameBoardListener? = null
    private var gameBoardCells = 10

    fun setGameBoardListener(listener: OnGameBoardListener){
        this.listener = listener
    }

    //畫筆
    private lateinit var mNormalPaint: Paint
    private lateinit var mStonePaint: Paint

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
        for (stone in mStonesBlack){
            canvas?.drawCircle(stone.centerX, stone.centerY, dotRadius.toFloat(), mStonePaint)
        }
    }

    /**畫筆預設值*/
    private fun initPaint() {
        //棋盤格線
        mNormalPaint = Paint().apply {
            color = mNormalColor
            isAntiAlias = true             //是否抗鋸齒
            style = Paint.Style.STROKE     //樣式：Stroke 描邊; Fill 填充; Fill and Stroke 描邊加填充
            strokeWidth = 5f               //寬度
        }

        //黑子
        mStonePaint = Paint().apply {
            color = mNormalColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        //白子
        //TODO: 白子樣式
    }

    private fun initGameBoard() {
        GameLog.i("初始化棋盤")
        val squareWidth = width / 10    //每個方格的大小
        dotRadius = squareWidth / 4     //中心點座標

        var count = 0

        mPoints.clear()
        for (i in 0..10){
            val row = ArrayList<Point?>()   //創建一個新的行
            for (j in 0..10){
                count++
                val point = Point(screenWidth/10 * j, screenHeight/10 * i, count)
                row.add(point)
            }
            mPoints.add(row)
        }
    }

    private fun drawShow(canvas: Canvas?) {
        //棋盤格線(直線)
        for (i in 0 .. 10){
            canvas?.drawLine(screenWidth/10*i, 0f, screenWidth/10*i, screenHeight, mNormalPaint)
        }

        //棋盤格線(橫線)
        for (j in 0 .. 10){
            canvas?.drawLine(0f, screenHeight/10*j, screenWidth, screenHeight/10*j, mNormalPaint)
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
        GameLog.i("x = $x , y = $y , width = $width , height = $height")

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
    private fun checkIfSamePosition(nearestPoint: Point): Boolean{
        GameLog.i("nearestPoint = ${Gson().toJson(nearestPoint)}")
        GameLog.i("已下過棋的座標 = ${Gson().toJson(mStonesBlack)}")

        var isSamePosition = false

        for (mStone in mStonesBlack){
            if (mStone.centerX == nearestPoint.centerX && mStone.centerY == nearestPoint.centerY){
                isSamePosition = true
                break
            }
        }

        for (mStone in mStonesWhite){
            if (mStone.centerX == nearestPoint.centerX && mStone.centerY == nearestPoint.centerY){
                isSamePosition = true
                break
            }
        }

        GameLog.i("isSamePosition = $isSamePosition")
        return isSamePosition
    }

    /**放置旗棋子*/
    private fun placeStone(point: Point) {
        GameLog.i("棋子座標 = ${Gson().toJson(point)}")
        mStonesBlack.add(point)  //將棋子座標加到列表中
        invalidate()        //重繪視圖
    }

    /**找到最近的交界點*/
    private fun findNearestPoint(x: Float, y: Float): Point? {
        var minDistance = Float.MAX_VALUE
        var nearestPoint: Point? = null
        for (row in mPoints){
            for (point in row){
                point?.let {
                    //計算點到觸摸位置的距離
                    val distance = hypot((it.centerX - x).toDouble(), (it.centerY - y).toDouble()).toFloat()

                    if (distance < minDistance){    //如果這個距離小於之前的最小距離
                        minDistance = distance      //更新最小距離
                        nearestPoint = it           //更新最近的點
                    }
                }
            }
        }
        return nearestPoint
    }

    fun onClearGameBoard(){
        GameLog.i("啟動 重新開始")
        mStonesBlack.clear()
        mStonesWhite.clear()
        invalidate()
    }

    interface OnGameBoardListener{
    }
}
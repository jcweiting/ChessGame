package com.joyce.gomoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class GameBoard: View {

    private var mPoints: Array<Array<Point?>> = Array(11) { Array(11) { null } }
    private var screenWidth = 0f        //螢幕寬度
    private var screenHeight = 0f       //螢幕高度
    private var dotRadius = 0           //外圓半徑

    //畫筆
    private lateinit var mNormalPaint: Paint

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
    }

    /**畫筆預設值*/
    private fun initPaint() {
        mNormalPaint = Paint()
        mNormalPaint.color = mNormalColor
        mNormalPaint.isAntiAlias = true             //是否抗鋸齒
        mNormalPaint.style = Paint.Style.STROKE     //樣式：Stroke 描邊; Fill 填充; Fill and Stroke 描邊加填充
        mNormalPaint.strokeWidth = 5f               //寬度
    }

    private fun initGameBoard() {
        val squareWidth = width / 10    //每個方格的大小
        dotRadius = squareWidth / 4     //中心點座標

        var count = 0

        for (i in 0.. 10){
            for (j in 0 .. 10){
                count++
                mPoints[i][j] = Point(screenWidth/10 * x, screenHeight/10 * y, count)
                GameLog.i("i = $i ; j = $j ; count = $count")
            }
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

}
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
import kotlin.math.abs
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
    private var eachHalfWidth = 0
    private var eachHalfHeight = 0
    private var avgSpaceDifferent = 0.0f //每一格的間距

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

        avgSpaceDifferent = screenWidth / gameBoardCells    //每隔的間距

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

            eachHalfWidth = width / gameBoardCells / 2     //半格的寬度
            eachHalfHeight = height / gameBoardCells / 2   //半格的高度

            if (isWithinBounds(x, y)){
                handleTouch(x, y)
            }
        }
        return true
    }

    /**是否在邊界內*/
    private fun isWithinBounds(x: Float, y: Float): Boolean {
        val checkX = !(x <= eachHalfWidth || x >= width - eachHalfWidth)
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
        var isWin = false

        val horizontalStoneWinArray = getHorizontalArray(chessArray)
        val straightStoneWinArray = getStraightStoneArray(chessArray)
        val slantingStoneWinArray = getSlantingStoneArray(chessArray)
        GameLog.i("isChessArrConnectInLine() slantingStoneWinArray = ${Gson().toJson(slantingStoneWinArray)}")

        var straightCount = 0
        var horizontalCount = 0

        //1.判斷橫的------------------------------
        for ((index, stoneList) in horizontalStoneWinArray.withIndex()){
            val sortedStoneList = stoneList.sortedBy { it.centerX }         //按照x座標排序
            horizontalStoneWinArray[index] = ArrayList(sortedStoneList)     //把排序後的值重新丟回horizontalStoneWinArray
        }

        for (stoneList in horizontalStoneWinArray){
            for ((index,stone) in stoneList.withIndex()){

                //若超出list長度,則跳出
                if (index + 1 >= stoneList.size){
                    break
                }

                //判斷當前棋子與下一個棋子的X坐標差距是否在允許範圍內(表示連在一起)
                //abs: 絕對值
                //+3: 誤差值
                if (abs(stone.centerX - stoneList[index+1].centerX) < avgSpaceDifferent + 3) {
                    horizontalCount ++
                }
            }
            if (horizontalCount >= 4){
                isWin = true
                break
            }
            horizontalCount = 0
        }

        //2.判斷直的------------------------------
        for ((index, stoneList) in straightStoneWinArray.withIndex()){
            val sortedStoneList = stoneList.sortedBy { it.centerY }
            straightStoneWinArray[index] = ArrayList(sortedStoneList)
        }

        for (stoneList in straightStoneWinArray){
            for ((index,stone) in stoneList.withIndex()){
                if (index + 1 >= stoneList.size){
                    break
                }
                if (abs(stone.centerY - stoneList[index+1].centerY) < avgSpaceDifferent+3) {
                    straightCount ++
                }
            }
            if (straightCount >= 4){
                isWin = true
                break
            }
            straightCount = 0
        }

        //3.判斷斜的------------------------------
        for ((index, stoneList) in slantingStoneWinArray.withIndex()){
            val sortedStoneList = stoneList.sortedBy { it.centerX }
            slantingStoneWinArray[index] = ArrayList(sortedStoneList)
        }
        GameLog.i("由左至右 排序 : ${Gson().toJson(slantingStoneWinArray)}")

        for (stoneList in slantingStoneWinArray){
            for (stone in stoneList){
                //檢查從左上到右下的斜線（/）
                if (checkDiagonal(stone, chessArray, 1, 1)) {
                    isWin = true
                }
                //檢查從左下到右上的斜線（\）
                if (checkDiagonal(stone, chessArray, 1, -1)) {
                    isWin = true
                }
            }
        }

        return isWin
    }

    /**
     * 判斷斜線
     * @param stone 當前檢查的棋子座標
     * @param mStonesArr 要檢查的棋子的arr
     * @param dx 檢查方向的x增量
     * @param dy 檢查方向的y增量
     */
    private fun checkDiagonal(stone: ChessPoint, mStonesArr: List<ChessPoint>, dx: Int, dy: Int): Boolean {
        var count = 1

        //向一個方向檢查
        var x = stone.centerX
        var y = stone.centerY

        for (i in 1..4) {
            x += dx * avgSpaceDifferent
            y += dy * avgSpaceDifferent
            if (mStonesArr.any { it.isBlackChess == stone.isBlackChess && abs(it.centerX - x) < 5 && abs(it.centerY - y) < 5 }) {
                count++
            } else {
                break
            }
        }

        //向另一个方向檢查
        x = stone.centerX
        y = stone.centerY
        for (i in 1..4) {
            x -= dx * avgSpaceDifferent
            y -= dy * avgSpaceDifferent
            if (mStonesArr.any { it.isBlackChess == stone.isBlackChess && abs(it.centerX - x) < 5 && abs(it.centerY - y) < 5 }) {
                count++
            } else {
                break
            }
        }

        return count >= 5
    }

    /**取得斜排的棋子列表*/
    private fun getSlantingStoneArray(chessArray: ArrayList<ChessPoint>): ArrayList<ArrayList<ChessPoint>> {
        val slantingStoneWinArray = ArrayList<ArrayList<ChessPoint>>()
        val dataList = ArrayList<ChessPoint>()

        for (stone in chessArray) {
            dataList.add(stone.copy())
        }

        slantingStoneWinArray.add(dataList)

        return slantingStoneWinArray
    }

    /**取得縱向的棋子列表(相同X軸放一起)*/
    private fun getStraightStoneArray(chessArray: ArrayList<ChessPoint>):  ArrayList<ArrayList<ChessPoint>> {
        val straightStoneWinArray = ArrayList<ArrayList<ChessPoint>>()

        for (stone in chessArray){
            var isFoundSameStone = false

            for (stoneList in straightStoneWinArray){
                for (data in stoneList){
                    if (data.centerY == stone.centerY && data.centerX == stone.centerX){
                        continue
                    }
                    if (data.centerX == stone.centerX){
                        stoneList.add(stone)
                        isFoundSameStone = true
                    }
                    break
                }
            }

            if (!isFoundSameStone){
                val stoneList = ArrayList<ChessPoint>()
                stoneList.add(stone)
                straightStoneWinArray.add(stoneList)
            }
        }
        return straightStoneWinArray
    }

    /**橫向的棋子列表(相同Y軸放一起)*/
    private fun getHorizontalArray(chessArray: ArrayList<ChessPoint>): ArrayList<ArrayList<ChessPoint>> {
        val horizontalStoneWinArray = ArrayList<ArrayList<ChessPoint>>()

        for (stone in chessArray){
            var isFoundSameStone = false

            for (stoneList in horizontalStoneWinArray){
                for (data in stoneList){
                    //跳過相同的棋子
                    if (data.centerY == stone.centerY && data.centerX == stone.centerX){
                        continue
                    }

                    //y軸相同, 則加入
                    if (data.centerY == stone.centerY){
                        stoneList.add(stone)
                        isFoundSameStone = true
                    }
                    break
                }
            }

            if (!isFoundSameStone){
                val stoneList = ArrayList<ChessPoint>()
                stoneList.add(stone)
                horizontalStoneWinArray.add(stoneList)
            }
        }
        return horizontalStoneWinArray
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
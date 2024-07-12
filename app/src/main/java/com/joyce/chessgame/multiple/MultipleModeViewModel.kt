package com.joyce.chessgame.multiple

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.BLACK
import com.joyce.chessgame.GlobalConfig.Companion.HOST
import com.joyce.chessgame.GlobalConfig.Companion.PLAYER2
import com.joyce.chessgame.GlobalConfig.Companion.WHITE
import com.joyce.chessgame.R
import com.joyce.chessgame.Util
import com.joyce.chessgame.base.BaseViewModel

class MultipleModeViewModel: BaseViewModel() {

    var backToPreviousPageLiveData = MutableLiveData<Boolean>()
    var setRoomInfoLiveData = MutableLiveData<Pair<String?, String?>>()
    var showStartGameBtnLiveDta = MutableLiveData<Boolean>()
    var gameStartCountDownLiveData = MutableLiveData<String>()
    var isFinishedCountDownLiveData = MutableLiveData<Boolean>()
    var giveUpGameAlertLiveData = MutableLiveData<Boolean>()
    var showAlertDialogLiveData = MutableLiveData<String>()
    var myRemainTimeLiveData = MutableLiveData<String>()
    var opponentTimeRemainLiveData = MutableLiveData<String>()
    var isMyTurnLiveData = MutableLiveData<Boolean>()
    var setTextLiveData = MutableLiveData<Pair<String, String>>()
    var whoFirstLiveData = MutableLiveData<String>()
    var autoPlaceChessLiveData = MutableLiveData<Boolean>()
    var showWinnerLiveData = MutableLiveData<String>()
    var isShowWaitingDialog = MutableLiveData<Boolean>()
    var updateBackBtnStyleLiveData = MutableLiveData<Pair<Int, String>>()
    var updateChessBoard = MutableLiveData<Pair<ChessCollection, Boolean>>()
    private val db = Firebase.firestore
    private var isEnableTvBack = true
    private var roomAction = RoomAction()
    private var countDownTimer: CountDownTimer? = null
    private var isInitial = true
    private var character = ""
    private var myChessColor = ""
    private var isStartCheckChessLocation = false

    fun initView(roomId: String?, character: String?) {
        isShowWaitingDialog.value = true
        checkRoomAction(roomId)
        checkChessBoard(roomId)
        character?.let {
            this.character = it
        }
    }

    fun sentGameStartToServer(roomId: String?) {
        roomId?.let {
            val actions = Actions(2, it)
            sentActionNotification(actions){}
        }
    }

    /**倒數10秒*/
    private fun gameStartCountDown() {
        if (roomAction.second >= 1){
            gameStartCountDownLiveData.value = roomAction.second.toString()
        } else {
            isEnableTvBack = true
            isFinishedCountDownLiveData.value = true
        }
    }

    /**確認按鈕目前狀態*/
    fun checkButtonType(buttonContent: String) {
        if (isEnableTvBack && buttonContent == Util.getString(R.string.back)){
            backToPreviousPageLiveData.value = true

        } else if (buttonContent == Util.getString(R.string.give_up)){
            giveUpGameAlertLiveData.value = true
        }
    }

    fun isQuitGame(isQuitGame: Boolean) {
        if(isQuitGame){
            //TODO: 通知server要放棄遊戲
            backToPreviousPageLiveData.value = true
        }
    }

    private fun checkRoomAction(roomId: String?) {
        GameLog.i("roomId = $roomId, RoomAction(original) = ${Gson().toJson(roomAction)}")

        if (roomId.isNullOrBlank()){
            showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
            return
        }

        val docRef = db.collection("Room_Action").document(roomId)
        docRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                error.printStackTrace()
                showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
                return@addSnapshotListener
            }

            snapshots?.let { docSnapshots ->
                if (docSnapshots.exists()){
                    val data = docSnapshots.data

                    if (data != null){
                        roomAction = mapToRoomAction(data)
                        GameLog.i("checkRoomAction() --> roomAction = ${Gson().toJson(roomAction)}")

                        //遊戲準備開始
                        if (roomAction.status == 0 && !roomAction.player2.isNullOrEmpty()){
                            val opponent = if (character == HOST) roomAction.player2 else roomAction.host
                            val roomName = if (character == HOST) roomAction.roomName + Util.getString(R.string.desc_host) else roomAction.roomName
                            setRoomInfoLiveData.value = Pair(roomName, opponent)

                            //顯示「開始對弈」
                            if (character == HOST) {
                                isShowWaitingDialog.value = false
                                showStartGameBtnLiveDta.value = true
                            }

                            //倒數10秒
                        } else if (roomAction.status == 2){
                            if (character == PLAYER2) {
                                isShowWaitingDialog.value = false
                            }
                            isEnableTvBack = false
                            updateBackBtnStyleLiveData.value = Pair(R.drawable.bg_grey_radius10, Util.getString(R.string.back))
                            gameStartCountDown()

                            //開始遊戲
                        } else if (roomAction.status == 4){
                            checkWhoTurn()
                            if (isInitial) checkFirstMove() else startCountDownTimer()
                            isStartCheckChessLocation = true
                        }

                    } else {
                        GameLog.i("找不到相對應的roomId資料")
                        showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
                    }

                } else {
                    GameLog.i("找不到相對應的roomId資料")
                    showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
                }
            }
        }

        GameLog.i("RoomAction = ${Gson().toJson(roomAction)}")
    }

    private fun mapToRoomAction(data: Map<String, Any>): RoomAction {
        return RoomAction(
            host = data["host"] as String?,
            msg = data["msg"] as String?,
            player2 = data["player2"] as String?,
            roomId = data["roomId"] as String?,
            roomName = data["roomName"] as String?,
            second = ((data["second"] as? Long)?.toInt()?:0),
            status = (data["status"] as? Long)?.toInt()?:0,
            whoFirst = (data["who_first"] as? Long)?.toInt()?:0,
            whoTurn = (data["who_turn"] as? Long)?.toInt()?:0
        )
    }

    private fun checkChessBoard(roomId: String?) {
        if (roomId.isNullOrBlank()){
            showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
            return
        }

        var chessCollection: ChessCollection? = null
        val docRef = db.collection("Room_Action").document(roomId).collection("chessBoard")
        docRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                error.printStackTrace()
                showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
                return@addSnapshotListener
            }

            for (docChange in snapshots!!.documentChanges){
                when(docChange.type){
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val data = docChange.document.data
                        if (!isStartCheckChessLocation){
                            return@addSnapshotListener
                        }

                        chessCollection = mapToChessCollection(data)
                        GameLog.i("監聽 chessCollection XY = ${chessCollection!!.x}-${chessCollection!!.y} | ${Gson().toJson(chessCollection)}")

                    }

                    DocumentChange.Type.REMOVED -> {
                        val data = docChange.document.data
                        GameLog.i("移除 chessCollection  = ${Gson().toJson(data)}")
                    }
                }
            }
            chessCollection?.let { updateChessBoard(it) }
        }
    }

    private fun updateChessBoard(chessCollection: ChessCollection) {
        if (chessCollection.whoPress == 0 && character == HOST || chessCollection.whoPress == 1 && character == PLAYER2){
            GameLog.i("不更新")

        } else if (chessCollection.whoPress == 0 && character == PLAYER2 || chessCollection.whoPress == 1 && character == HOST){
            GameLog.i("----------------------------------------------")
            GameLog.i("開始更新")
            updateChessBoard.value = Pair(chessCollection, myChessColor != BLACK)
        }
    }

    private fun mapToChessCollection(data: Map<String, Any>): ChessCollection{
        return ChessCollection(
            whoPress = (data["whoPress"] as? Long)?.toInt()?:0,
            x = (data["x"] as? Long)?.toInt()?:0,
            y = (data["y"] as? Long)?.toInt()?:0,
        )
    }

    private fun checkFirstMove() {
        if (roomAction.whoFirst == 0 && character == HOST || roomAction.whoFirst == 1 && character == PLAYER2){
            myChessColor = BLACK
            whoFirstLiveData.value = Util.getString(R.string.first_move)
            setTextLiveData.value = Pair(Util.getString(R.string.my_side_black),Util.getString(R.string.opposite_side_white))

        } else if (roomAction.whoFirst == 0 && character == PLAYER2 || roomAction.whoFirst == 1 && character == HOST){
            myChessColor = WHITE
            whoFirstLiveData.value = Util.getString(R.string.second_move)
            setTextLiveData.value = Pair(Util.getString(R.string.my_side_white),Util.getString(R.string.opposite_side_black))
        }

        updateBackBtnStyleLiveData.value = Pair(R.drawable.bg_grey_radius10, Util.getString(R.string.give_up))
        isInitial = false
    }

    private fun checkWhoTurn() {
        if (roomAction.whoTurn == 0 && character == HOST || roomAction.whoTurn == 1 && character == PLAYER2){
            isMyTurnLiveData.value = true
        } else if (roomAction.whoTurn == 0 && character == PLAYER2 || roomAction.whoTurn == 1 && character == HOST){
            isMyTurnLiveData.value = false
        }
    }

    fun startCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(31000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val secondRemaining = millisUntilFinished/1000

                if (roomAction.whoTurn == 0 && character == HOST || roomAction.whoTurn == 1 && character == PLAYER2){
                    myRemainTimeLiveData.value = secondRemaining.toString()
                } else if (roomAction.whoTurn == 0 && character == PLAYER2 || roomAction.whoTurn == 1 && character == HOST){
                    opponentTimeRemainLiveData.value = secondRemaining.toString()
                }
            }

            //倒數結束, 自動下棋
            override fun onFinish() {
                if (roomAction.whoTurn == 0 && character == HOST || roomAction.whoTurn == 1 && character == PLAYER2){
                    autoPlaceChessLiveData.value = true
                } else {
                    GameLog.i("不是輪到我方, 不需自動下棋")
                }
            }
        }
        countDownTimer?.start()
    }

    fun gameEnd(blackChess: Boolean, mySide: String, player2Side: String) {
        countDownTimer?.cancel()
        if (blackChess){
            if (roomAction.whoFirst == 0){
                showWinnerLiveData.value = mySide
            } else {
                showWinnerLiveData.value = player2Side
            }
        } else {
            if (roomAction.whoFirst == 0){
                showWinnerLiveData.value = mySide
            } else {
                showWinnerLiveData.value = player2Side
            }
        }
    }

    fun sentChessLocation(blackChess: Boolean, x: Long, y: Long) {
        GameLog.i("blackChess = $blackChess, roomAction.whoTurn = ${roomAction.whoTurn}")

        val whoPress = if (character == HOST) 0.toLong() else 1.toLong()
        val actions = Actions(5, x, y, whoPress, roomAction.roomId)
        sentActionNotification(actions){
            GameLog.i("座標更新完成")
        }
    }
}
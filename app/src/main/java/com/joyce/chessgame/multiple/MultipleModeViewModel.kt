package com.joyce.chessgame.multiple

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.core.InFilter
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.HOST
import com.joyce.chessgame.GlobalConfig.Companion.PLAYER2
import com.joyce.chessgame.R
import com.joyce.chessgame.Util
import com.joyce.chessgame.base.BaseViewModel

class MultipleModeViewModel: BaseViewModel() {

    var backToPreviousPageLiveData = MutableLiveData<Boolean>()
    var waitingOpponentLiveData = MutableLiveData<Boolean>()
    var startGameLiveData = MutableLiveData<Pair<String?, String?>>()
    var gameStartCountDownLiveData = MutableLiveData<String>()
    var isFinishedCountDownLiveData = MutableLiveData<Boolean>()
    var giveUpGameAlertLiveData = MutableLiveData<Boolean>()
    var showAlertDialogLiveData = MutableLiveData<String>()
    var hostTimeRemainLiveData = MutableLiveData<String>()
    var player2TimeRemainLiveData = MutableLiveData<String>()
    var hostTurnLiveData = MutableLiveData<Boolean>()
    var player2TurnLiveData = MutableLiveData<Boolean>()
    var whoFirstLiveData = MutableLiveData<String>()
    var whoFirstTextLiveData = MutableLiveData<Pair<String, String>>()
    var autoPlaceChessLiveData = MutableLiveData<Boolean>()
    var showWinnerLiveData = MutableLiveData<String>()
    private val db = Firebase.firestore
    private var isEnableTvBack = true
    private var roomAction = RoomAction()
    private var countDownTimer: CountDownTimer? = null
    private var isInitial = true
    private var character = ""

    fun initView(roomId: String?, character: String?) {
        waitingOpponentLiveData.value = true
        checkRoomAction(roomId)
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
        GameLog.i("gameStartCountDown() roomAction.second = ${roomAction.second}")
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
                GameLog.i("監聽失敗 = $error")
                showAlertDialogLiveData.value = Util.getString(R.string.get_some_mistake)
                return@addSnapshotListener
            }

            snapshots?.let { docSnapshots ->
                if (docSnapshots.exists()){
                    val data = docSnapshots.data

                    if (data != null){
                        roomAction = mapToRoomAction(data)
                        GameLog.i("checkRoomAction() --> roomAction = ${Gson().toJson(roomAction)}")

                        //顯示「開始對弈」按鈕
                        if (character == HOST && roomAction.status == 0 && !roomAction.player2.isNullOrEmpty()){
                            isEnableTvBack = false
                            startGameLiveData.value = Pair(roomAction.roomName, roomAction.player2)

                            //倒數10秒
                        } else if (roomAction.status == 2){
                            gameStartCountDown()
                            if (character == PLAYER2) {
                                waitingOpponentLiveData.value = false
                            }

                            //開始遊戲
                        } else if (roomAction.status == 4){
                            checkPlayerTurn()
                            if (isInitial) checkWhoTurn() else startCountDownTimer()
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

    private fun checkPlayerTurn() {
        if (roomAction.whoTurn == 0){
            hostTurnLiveData.value = true
        } else {
            player2TurnLiveData.value = true
        }
    }

    private fun checkWhoTurn() {
        if (roomAction.whoFirst == 0){
            if (character == HOST){
                isFirstMove(true)
            } else {
                isFirstMove(false)
            }

        } else {
            if (character == PLAYER2){
                isFirstMove(true)
            } else {
                isFirstMove(false)
            }
        }
    }

    private fun isFirstMove(isFirstMove: Boolean){
        if (isFirstMove){
            whoFirstLiveData.value = Util.getString(R.string.first_move)
            whoFirstTextLiveData.value = Pair(Util.getString(R.string.my_side_black), Util.getString(R.string.opposite_side_white))
        } else {
            whoFirstLiveData.value = Util.getString(R.string.second_move)
            whoFirstTextLiveData.value = Pair(Util.getString(R.string.my_side_white), Util.getString(R.string.opposite_side_black))
        }
    }

    fun startCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(21000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val secondRemaining = millisUntilFinished/1000

                if (roomAction.whoTurn == 0) {
                    hostTimeRemainLiveData.value = secondRemaining.toString()
                } else {
                    player2TimeRemainLiveData.value = secondRemaining.toString()
                }
            }

            //倒數結束, 自動下棋
            override fun onFinish() {
                autoPlaceChessLiveData.value = true
            }
        }
        countDownTimer?.start()
        isInitial = false
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
        val actions = Actions(5, x, y, roomAction.whoTurn.toLong(), roomAction.roomId)
        sentActionNotification(actions){
            GameLog.i("座標更新完成")
        }
    }
}
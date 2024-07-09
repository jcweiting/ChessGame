package com.joyce.chessgame.multiple

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalFunction.getStringValue
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseViewModel

class MultipleModeViewModel: BaseViewModel() {

    var backToPreviousPageLiveData = MutableLiveData<Boolean>()
    var waitingOpponentLiveData = MutableLiveData<Boolean>()
    var startGameLiveData = MutableLiveData<Boolean>()
    var gameStartCountDownLiveData = MutableLiveData<String>()
    var isFinishedCountDownLiveData = MutableLiveData<Boolean>()
    var showWhoFirstLiveData = MutableLiveData<Pair<String, String>>()
    var giveUpGameAlertLiveData = MutableLiveData<Boolean>()
    var showAlertDialogLiveData = MutableLiveData<String>()
    private val db = Firebase.firestore
    private var isEnableTvBack = true
    private var roomAction = RoomAction()
    private var isClickedStartGame = false
    private var countDownTimer: CountDownTimer? = null

    fun initView(roomId: String?) {
        waitingOpponentLiveData.value = true
        checkRoomAction(roomId)
    }

    fun sentGameStartToServer(roomId: String?) {
        roomId?.let {
            val actions = Actions(2, it)
            sentStartGameNotification(actions){
                isClickedStartGame = true
            }
        }
    }

    /**倒數10秒*/
    private fun gameStartCountDown() {
        GameLog.i("gameStartCountDown() roomAction.second = ${roomAction.second}")
        if (roomAction.second > 1){
            gameStartCountDownLiveData.value = roomAction.second.toString()
        } else {
            isEnableTvBack = true
            isFinishedCountDownLiveData.value = true

            //區分我方黑子or白子
            if (roomAction.whoFirst == 0){
                showWhoFirstLiveData.value = Pair(R.string.my_side_black.getStringValue(), R.string.opposite_side_white.getStringValue())
            } else {
                showWhoFirstLiveData.value = Pair(R.string.my_side_white.getStringValue(), R.string.opposite_side_black.getStringValue())
            }
        }
    }

    /**確認按鈕目前狀態*/
    fun checkButtonType(buttonContent: String) {
        if (isEnableTvBack && buttonContent == R.string.back.getStringValue()){
            backToPreviousPageLiveData.value = true

        } else if (buttonContent == R.string.give_up.getStringValue()){
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
            showAlertDialogLiveData.value = R.string.get_some_mistake.getStringValue()
            return
        }

        val docRef = db.collection("Room_Action").document(roomId)
        docRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                GameLog.i("監聽失敗 = $error")
                showAlertDialogLiveData.value = R.string.get_some_mistake.getStringValue()
                return@addSnapshotListener
            }

            snapshots?.let { docSnapshots ->
                if (docSnapshots.exists()){
                    val data = docSnapshots.data

                    if (data != null){
                        roomAction = mapToRoomAction(data)

                        //顯示「開始對弈」按鈕
                        if (roomAction.status == 0 && !roomAction.player2.isNullOrEmpty()){
                            isEnableTvBack = false
                            startGameLiveData.value = true
                        }

                        //開始倒數10秒
                        if (roomAction.status == 2){
                            gameStartCountDown()
                        }

                    } else {
                        GameLog.i("找不到相對應的roomId資料")
                        showAlertDialogLiveData.value = R.string.get_some_mistake.getStringValue()
                    }

                } else {
                    GameLog.i("找不到相對應的roomId資料")
                    showAlertDialogLiveData.value = R.string.get_some_mistake.getStringValue()
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

    fun startCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(20000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                if (roomAction.whoTurn == 0) {


                } else {


                }
            }

            override fun onFinish() {
                if (roomAction.whoTurn == 0) {


                } else {


                }
            }
        }
        countDownTimer?.start()
    }
}
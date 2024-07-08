package com.joyce.chessgame.server

import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joyce.chessgame.Util
import com.joyce.chessgame.server.bean.ActionData
import com.joyce.chessgame.server.bean.GameRoomData
import com.joyce.chessgame.server.bean.MemberData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Objects
import java.util.UUID

class ServerRepository {

    companion object{
        const val CREATE_ROOM = 1L
        const val WAITING_STATUS = 0L
        const val READY_TO_COUNT_DOWN = 2L
        const val GO_CHESS_BOARD = 4L
        const val JOIN_GAME = 3L
        const val PRESS_CHESS = 5L
        const val HOST = 0L
        const val USER2 = 1L
    }
    private val roomsList = ArrayList<GameRoomData>()
    private lateinit var onCatchDataFromDataBaseListener: OnCatchDataFromDataBaseListener
    private val db = Firebase.firestore
    private var memberList = ArrayList<MemberData>()

    fun setOnCatchDataFromDataBaseListener(onCatchDataFromDataBaseListener: OnCatchDataFromDataBaseListener) {
        this.onCatchDataFromDataBaseListener = onCatchDataFromDataBaseListener
    }

    fun onCatchTempUsersData() {
        db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Michael", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("Michael", "Current data: null")
                    return@addSnapshotListener
                }
                memberList.clear()
                for (document in snapshot) {
                    var email = ""
                    var isActive = false
                    var timeStamp = 0L
                    document.getString("email")?.let { serverEmail ->
                        email = serverEmail
                    }
                    document.getBoolean("active")?.let { serverIsActive ->
                        isActive = serverIsActive
                    }
                    document.getLong("timeStamp")?.let { serverTimeStamp ->
                        timeStamp = serverTimeStamp
                    }
                    val memberData = MemberData(email, isActive, document.id, timeStamp)
                    memberList.add(memberData)

                }

                memberList = ArrayList(memberList.groupBy {
                    it.email
                }.map {
                        (_,memerbs) -> memerbs.maxByOrNull { it.timeStamp }!!
                })

                onCatchDataFromDataBaseListener.onCatchTempUsersDataList(memberList)

            }
    }

    fun onCatchGameAction() {
        db.collection("Actions")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.i("Michael", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    Log.i("Michael", "Current data: null")
                    return@addSnapshotListener
                }
                val actionsList = ArrayList<ActionData>()
                for (document in snapshot) {
                    var actionType = 0L
                    var host = ""
                    var time = 0L
                    var roomId = ""
                    var roomName = ""
                    var player2 = ""
                    var x = 0L
                    var y = 0L
                    var whoPress = 0L
                    document.getLong("whoPress")?.let { pressStatus->
                        whoPress = pressStatus
                    }
                    document.getLong("x")?.let { chessX->
                        x = chessX
                    }
                    document.getLong("y")?.let { chessY->
                        y = chessY
                    }
                    document.getString("player2")?.let { name ->
                        player2 = name
                    }
                    document.getString("roomName")?.let { name ->
                        roomName = name
                    }
                    document.getString("roomId")?.let { id->
                        roomId = id
                    }
                    document.getLong("actionType")?.let { action ->
                        actionType = action
                    }
                    document.getString("host")?.let { email ->
                        host = email
                    }
                    document.getLong("timeStamp")?.let { serverTimeStamp ->
                        time = serverTimeStamp
                    }
                    val actionData = ActionData(actionType, host, time,roomId,document.id,roomName,player2,x,y,whoPress)
                    actionsList.add(actionData)
                }
                for (action in actionsList){
                    if (action.actionType == CREATE_ROOM){
                        createRoom(action)
                    }
                    if (action.actionType == JOIN_GAME){
                        joinTheRoom(action)
                    }
                    if (action.actionType == READY_TO_COUNT_DOWN){
                        handleCountingDown(action)
                    }
                    if (action.actionType == PRESS_CHESS){
                        handlePressChess(action)
                    }
                }
            }
    }

    private fun handlePressChess(action: ActionData) {
        val map = hashMapOf(
            "x" to action.x,
            "y" to action.y,
            "whoPress" to action.whoPress
        )
        onCatchDataFromDataBaseListener.onShowLog("房間 : ${action.roomId} \n 下棋 : ${action.whoPress} 位置 : ${action.x}之${action.y}")
        db.collection("Room_Action")
            .document(action.roomId)
            .collection("chessBoard")
            .document()
            .set(map,SetOptions.merge())
        changePressSite(action)
    }

    private fun changePressSite(action: ActionData) {
        db.collection("Room_Action")
            .document(action.roomId)
            .update("who_turn",if (action.whoPress == HOST) USER2 else HOST)
        clearActionData(action.documentId)
    }

    private fun joinTheRoom(action: ActionData) {
        onCatchDataFromDataBaseListener.onShowLog("${action.player2} 加入房間 ${action.roomId}")
        var documentId = ""
        for (room in roomsList){
            if (room.roomId == action.roomId){
                documentId = room.documentId
            }
        }
        if (documentId.isEmpty()){
            onCatchDataFromDataBaseListener.onShowLog("加入房間錯誤")
            return
        }
        db.collection("Rooms")
            .document(documentId)
            .update("user2",Util.hideEmail(action.player2))

        db.collection("Room_Action")
            .document(action.roomId)
            .update("player2",Util.hideEmail(action.player2))

        onCatchDataFromDataBaseListener.onShowLog("更新Room也更新RoomActions")


        clearActionData(action.documentId)
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private fun handleCountingDown(action: ActionData) {
        onCatchDataFromDataBaseListener.onShowLog("收到開始倒數房間 : ${action.roomId}")
        scope.launch(Dispatchers.IO) {
            for (time in 10 downTo  0){
                updateSecondToRoom(action.roomId,time)
                withContext(Dispatchers.Main){
                    onCatchDataFromDataBaseListener.onShowLog("Room : ${action.roomId} 開始倒數計時 : $time")
                }
                delay(1000L)
            }
            countingDownFinish(action.roomId)
            updateRoomStatus(action.roomId)
            clearActionData(action.documentId)
        }
    }

    private fun countingDownFinish(roomId: String) {
        val whoStatus = (0..1).random()
        val roomData = hashMapOf(
            "second" to 0,
            "status" to GO_CHESS_BOARD,
            "who_turn" to whoStatus,
            "who_first" to whoStatus
        )
        db.collection("Room_Action")
            .document(roomId)
            .set(roomData, SetOptions.merge())

    }

    private fun updateRoomStatus(roomId: String) {
        var roomData : GameRoomData? = null
        roomData = roomsList.find {
            it.roomId == roomId
        }
        roomData?.let {
            val roomRef = db.collection("Rooms").document(it.documentId)
            roomRef.update("status", GO_CHESS_BOARD)
                .addOnSuccessListener {
                    onCatchDataFromDataBaseListener.onShowLog("已更新房間狀態 : $roomId")
                }
        }
    }

    private fun updateSecondToRoom(roomId: String, time: Int) {
        val roomData = hashMapOf(
            "second" to time,
            "status" to READY_TO_COUNT_DOWN
        )
        db.collection("Room_Action")
            .document(roomId)
            .set(roomData, SetOptions.merge())
    }

    private fun createRoom(action: ActionData) {
        val roomData = hashMapOf(
            "roomId" to UUID.randomUUID().toString(),
            "host" to Util.hideEmail(action.host),
            "player2" to "",
            "timeStamp" to action.time,
            "roomName" to action.roomName,
            "status" to 0
        )
        onCatchDataFromDataBaseListener.onShowLog("收到動作 : ${checkAction(action.actionType)}")
        db.collection("Rooms")
            .document()
            .set(roomData, SetOptions.merge())
            .addOnSuccessListener {
                onCatchDataFromDataBaseListener.onShowLog("創建房間 : ${roomData["roomId"]} 成功")
            }
        clearActionData(action.documentId)
        createRoomAction(action, roomData["roomId"].toString())
    }

    private fun createRoomAction(action: ActionData, roomId: String) {
        val map = hashMapOf(
            "roomId" to roomId,
            "host" to Util.hideEmail(action.host),
            "player2" to "",
            "roomName" to action.roomName,
            "status" to WAITING_STATUS,
            "second" to 0,
            "who_turn" to 0,
            "who_first" to 0
        )
        db.collection("Room_Action")
            .document(roomId)
            .set(map, SetOptions.merge())

    }

    private fun checkAction(actionType: Long): String {
        when(actionType){
            CREATE_ROOM -> return "創建房間"
        }
        return ""
    }

    private fun clearActionData(documentId: String) {
        db.collection("Actions")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                onCatchDataFromDataBaseListener.onShowLog("移除Action : $documentId")
            }

    }

    fun onCatchRoomList() {
        db.collection("Rooms")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Michael", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("Michael", "Current data: null")
                    return@addSnapshotListener
                }
                roomsList.clear()
                for (document in snapshot) {
                    var host = ""
                    var roomId = ""
                    var status = 0L
                    var timeStamp = 0L
                    var user2 = ""
                    document.getString("host")?.let { hostEmail ->
                        host = hostEmail
                    }
                    document.getString("roomId")?.let { roomId2 ->
                        roomId = roomId2
                    }
                    document.getString("user2")?.let { user2Email ->
                        user2 = user2Email
                    }
                    document.getLong("status")?.let { roomStatus ->
                        status = roomStatus
                    }
                    document.getLong("timeStamp")?.let { time->
                        timeStamp = time
                    }
                    val roomData = GameRoomData(roomId,host,user2,timeStamp,status,document.id)
                    roomsList.add(roomData)
                }
                var waitingCount = 0
                var matchingCount = 0
                for (room in roomsList){
                    if (room.status == WAITING_STATUS){
                        waitingCount ++
                    }
                    if (room.status == GO_CHESS_BOARD){
                        matchingCount ++
                    }
                }
                onCatchDataFromDataBaseListener.onShowLog("目前房間數 : ${roomsList.size} , 等待中 : $waitingCount , 對弈中 : $matchingCount")
            }
    }


}
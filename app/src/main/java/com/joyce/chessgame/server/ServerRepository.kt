package com.joyce.chessgame.server

import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joyce.chessgame.server.bean.ActionData
import com.joyce.chessgame.server.bean.GameRoomData
import com.joyce.chessgame.server.bean.MemberData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ServerRepository {

    companion object{
        const val CREATE_ROOM = 1L
        const val WAITING_STATUS = 0L
        const val READY_TO_COUNT_DOWN = 2L
        const val GO_CHESS_BOARD = 2L
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
                    val actionData = ActionData(actionType, host, time,roomId,document.id)
                    actionsList.add(actionData)
                }
                for (action in actionsList){
                    if (action.actionType == CREATE_ROOM){
                        createRoom(action)
                    }
                    if (action.actionType == READY_TO_COUNT_DOWN){
                        handleCountingDown(action)
                    }
                }
            }
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
        val roomData = hashMapOf(
            "second" to 0,
            "msg" to "對弈開始",
            "status" to GO_CHESS_BOARD
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
            "msg" to "對弈即將開始,倒數${time}秒",
            "status" to READY_TO_COUNT_DOWN
        )
        db.collection("Room_Action")
            .document(roomId)
            .set(roomData, SetOptions.merge())
    }

    private fun createRoom(action: ActionData) {
        val roomData = hashMapOf(
            "roomId" to UUID.randomUUID().toString(),
            "host" to action.host,
            "user2" to "",
            "timeStamp" to action.time,
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
package com.joyce.chessgame.multiple

import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.CREATE_ROOM
import com.joyce.chessgame.GlobalConfig.Companion.SEARCH_ROOM
import com.joyce.chessgame.R
import com.joyce.chessgame.ShareTool
import com.joyce.chessgame.Util
import com.joyce.chessgame.base.BaseViewModel

class MultipleLobbyViewModel: BaseViewModel() {

    var showCreateRoomViewLiveData = MutableLiveData<Boolean>()
    var showSearchRoomContentLiveData = MutableLiveData<Boolean>()
    var hideCreateRoomViewLiveData = MutableLiveData<Boolean>()
    var hideSearchRoomContentLiveData = MutableLiveData<Boolean>()
    var showAlertLiveData = MutableLiveData<String>()
    var isShowProgressBarLiveData = MutableLiveData<Boolean>()
    var roomsArrayLiveData = MutableLiveData<ArrayList<GameRoomData>>()
    var emptyRoomLiveData = MutableLiveData<Boolean>()
    var isCreateRoomsSuccessLiveData = MutableLiveData<String>()
    var isSuccessAddRoomLiveData = MutableLiveData<String>()
    private val roomsArray = ArrayList<GameRoomData>()
    private val db = Firebase.firestore
    private var isCreatedRoomsName = ""

    fun checkOptionUiUpdate(isAddRoom: Boolean, isCreateRoom: Boolean, isSearchRoom: Boolean) {
        if (isAddRoom){
            hideCreateRoomViewLiveData.value = true
            hideSearchRoomContentLiveData.value = true
            pickRandomRoom()
        } else {

        }

        if (isCreateRoom){
            showCreateRoomViewLiveData.value = true
            hideSearchRoomContentLiveData.value = true
        } else {
            hideCreateRoomViewLiveData.value = true
        }

        if (isSearchRoom){
            showSearchRoomContentLiveData.value = true
            hideCreateRoomViewLiveData.value = true
        } else {
            hideSearchRoomContentLiveData.value = true
        }
    }

    private fun pickRandomRoom() {
        val availableRoomsArr = ArrayList<GameRoomData>()
        for (i in 0 until roomsArray.size){
            if (roomsArray[i].player2.isNullOrBlank()){
                availableRoomsArr.add(roomsArray[i])
            }
        }

        if (availableRoomsArr.isEmpty()){
            showAlertLiveData.value = Util.getString(R.string.cannot_find_empty_room)
            return
        }

        val randomInt = (0 until availableRoomsArr.size).random()
        val randomRoomId = availableRoomsArr[randomInt].roomId

        randomRoomId?.let {
            addAvailableRoom(it)
        }
    }

    /**加入房間*/
    fun addAvailableRoom(roomId: String){
        val actions = Actions(3, roomId, ShareTool.getUserData().email)
        sentActionNotification(actions){
            GameLog.i("成功加入房間 roomId = $roomId")
            isShowProgressBarLiveData.value = false
            isSuccessAddRoomLiveData.value = roomId
        }
    }

    fun checkRoomName(edRoomName: String?, type: String) {
        if (edRoomName.isNullOrBlank()){
            showAlertLiveData.value = Util.getString(R.string.enter_room_name)

        } else {
            isShowProgressBarLiveData.value = true
            when(type){
                CREATE_ROOM -> createGameRoom(edRoomName)
                SEARCH_ROOM -> searchRoomName(edRoomName)
            }
        }
    }

    /**搜尋房間*/
    private fun searchRoomName(edRoomName: String) {
        val availableRoomsArr = ArrayList<GameRoomData>()
        for (i in 0 until roomsArray.size){
            if (roomsArray[i].player2.isNullOrBlank()){
                availableRoomsArr.add(roomsArray[i])
            }
        }

        for (i in 0 until availableRoomsArr.size){
            if (availableRoomsArr[i].roomName == edRoomName){
                availableRoomsArr[i].roomId?.let { addAvailableRoom(it) }
                break

            } else {
                if (i == availableRoomsArr.size-1){
                    isShowProgressBarLiveData.value = false
                    showAlertLiveData.value = Util.getString(R.string.cannot_find_the_room)
                }
            }
        }
    }

    /**取得rooms列表*/
    fun checkRoomsList() {
        val docRef = db.collection("Rooms")
        docRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                GameLog.i("監聽失敗 = $error")
                return@addSnapshotListener
            }

            for (docChange in snapshots!!.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        GameLog.i("add doc: ${docChange.document.data}")
                        val data = docChange.document.data
                        val gameRoomData = mapToGameRoomData(data)
                        roomsArray.add(gameRoomData)

                        if (isCreatedRoomsName.isNotEmpty()){
                            checkRoomId(isCreatedRoomsName)
                        }
                    }

                    DocumentChange.Type.MODIFIED -> {
                        GameLog.i("Modified doc: ${docChange.document.data}")
                        val data = docChange.document.data
                        val gameRoomData = mapToGameRoomData(data)

                        //更新roomsArr
                        val index = roomsArray.indexOfFirst { it.roomId == gameRoomData.roomId }
                        if (index != -1){   //有匹配的元素
                            roomsArray[index] = gameRoomData
                        } else {    //無匹配的元素
                            roomsArray.add(gameRoomData)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        GameLog.i("Removed doc: ${docChange.document.data}")
                        val data = docChange.document.data
                        val gameRoomData = mapToGameRoomData(data)
                        roomsArray.removeAll {it.roomId == gameRoomData.roomId}
                    }
                }
            }

            GameLog.i("roomsArray = ${Gson().toJson(roomsArray)}")
            if (roomsArray.isEmpty()){
                emptyRoomLiveData.value = true
            } else {
                roomsArrayLiveData.value = roomsArray
            }
        }
    }

    private fun mapToGameRoomData(data: Map<String, Any>): GameRoomData{
        return GameRoomData(
            host = data["host"] as String?,
            roomId = data["roomId"] as String?,
            roomName = data["roomName"] as String?,
            status = ((data["status"] as? Long) ?:0).toInt(),
            timeStamp = ((data["timeStamp"] as? Long)?:0).toInt(),
            player2 = data["player2"] as String?
        )
    }

    private fun createGameRoom(roomName: String) {
        val actions = Actions(1, ShareTool.getUserData().email, System.currentTimeMillis(), roomName)
        createRooms(actions){
            isCreatedRoomsName = roomName
        }
    }

    private fun checkRoomId(roomName: String) {
        var roomId = ""
        val currentUserEmail = ShareTool.getUserData().email
        currentUserEmail?.let {
            for (roomData in roomsArray){
                if (roomData.host == Util.hideEmail(currentUserEmail) && roomData.roomName == roomName){
                    roomData.roomId?.let { roomId = it }
                    break
                }
            }
        }

        isCreatedRoomsName = ""
        hideCreateRoomViewLiveData.value = true
        isShowProgressBarLiveData.value = false
        isCreateRoomsSuccessLiveData.value = roomId
    }
}
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
    var isSearchRoomLiveData = MutableLiveData<String>()
    var isShowProgressBarLiveData = MutableLiveData<Boolean>()
    var roomsArrayLiveData = MutableLiveData<ArrayList<GameRoomData>>()
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
            if (roomsArray[i].user2.isNullOrBlank()){
                availableRoomsArr.add(roomsArray[i])
            }
        }

        val randomInt = (0 until availableRoomsArr.size).random()
        val randomRoomId = availableRoomsArr[randomInt].roomId
        GameLog.i("randomRoom = $randomInt, randomRoom = ${Gson().toJson(availableRoomsArr[randomInt])}")

        val actions = Actions(3, randomRoomId, ShareTool.getUserData().email)
        sentActionNotification(actions){
            GameLog.i("成功隨機加入房間")
            isSuccessAddRoomLiveData.value = randomRoomId
        }
    }

    fun checkRoomName(edRoomName: String?, type: String) {
        if (edRoomName.isNullOrBlank()){
            showAlertLiveData.value = Util.getString(R.string.enter_room_name)

        } else {
            isShowProgressBarLiveData.value = true
            when(type){
                CREATE_ROOM -> createGameRoom(edRoomName)
                SEARCH_ROOM -> isSearchRoomLiveData.value = edRoomName
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
                        GameLog.i("index = $index")

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

            //TODO: 如果回的DATA是空的? --- 無房間
            GameLog.i("roomsArray = ${Gson().toJson(roomsArray)}")
            roomsArrayLiveData.value = roomsArray
        }
    }

    private fun mapToGameRoomData(data: Map<String, Any>): GameRoomData{
        return GameRoomData(
            host = data["host"] as String?,
            roomId = data["roomId"] as String?,
            roomName = data["roomName"] as String?,
            status = ((data["status"] as? Long) ?:0).toInt(),
            timeStamp = ((data["timeStamp"] as? Long)?:0).toInt(),
            user2 = data["user2"] as String?
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
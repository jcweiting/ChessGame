package com.joyce.chessgame.multiple

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.CREATE_ROOM
import com.joyce.chessgame.GlobalConfig.Companion.SEARCH_ROOM
import com.joyce.chessgame.GlobalFunction.getStringValue
import com.joyce.chessgame.R

class MultipleLobbyViewModel: ViewModel() {

    var showCreateRoomViewLiveData = MutableLiveData<Boolean>()
    var showSearchRoomContentLiveData = MutableLiveData<Boolean>()
    var hideCreateRoomViewLiveData = MutableLiveData<Boolean>()
    var hideSearchRoomContentLiveData = MutableLiveData<Boolean>()
    var showAlertLiveData = MutableLiveData<String>()
    var isCreateRoomLiveData = MutableLiveData<String>()
    var isSearchRoomLiveData = MutableLiveData<String>()
    var isShowProgressBarLiveData = MutableLiveData<Boolean>()
    var roomsArrayLiveData = MutableLiveData<ArrayList<GameRoomData>>()

    fun checkOptionUiUpdate(isAddRoom: Boolean, isCreateRoom: Boolean, isSearchRoom: Boolean) {
        if (isAddRoom){
            hideCreateRoomViewLiveData.value = true
            hideSearchRoomContentLiveData.value = true

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

    fun checkRoomName(edRoomName: String?, type: String) {
        if (edRoomName.isNullOrBlank()){
            showAlertLiveData.value = R.string.enter_room_name.getStringValue()

        } else {
            isShowProgressBarLiveData.value = true
            when(type){
                CREATE_ROOM -> isCreateRoomLiveData.value = edRoomName
                SEARCH_ROOM -> isSearchRoomLiveData.value = edRoomName
            }
        }
    }

    /**取得rooms列表*/
    fun checkRoomsList() {
        val roomsArray = ArrayList<GameRoomData>()
        val db = Firebase.firestore

        val docRef = db.collection("Rooms")
        docRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                println("監聽失敗 = $error")
                return@addSnapshotListener
            }

            for (docChange in snapshots!!.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val data = docChange.document.data
                        val gameRoomData = mapToGameRoomData(data)
                        roomsArray.add(gameRoomData)
                    }
                    DocumentChange.Type.MODIFIED -> GameLog.i("Modified doc: ${docChange.document.data}")
                    DocumentChange.Type.REMOVED -> GameLog.i("Removed doc: ${docChange.document.data}")
                }
            }

            //TODO: 如果回的DATA是空的? --- 無房間

            GameLog.i("roomsArray = ${Gson().toJson(roomsArray)}")
            roomsArrayLiveData.value = roomsArray
        }
    }

    private fun mapToGameRoomData(data: Map<String, Any>): GameRoomData{
        return GameRoomData(
            roomId = data["roomId"] as String?,
            host = data["host"] as String?,
            user2 = data["user2"] as String?,
            timeStamp = (data["timeStamp"] as? Long)?:0,
            status = (data["status"] as? Long) ?:0   //TODO: status的型別是int還是long?
        )
    }
}
package com.joyce.chessgame.multiple

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
}
package com.joyce.chessgame.server

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joyce.chessgame.Util
import com.joyce.chessgame.server.bean.MemberData
import com.joyce.chessgame.server.bean.ServerLogData

class ServerViewModel : ViewModel() {

    private val _viewState = MutableLiveData<ServerViewState>()
    val viewState:MutableLiveData<ServerViewState> get() = _viewState
    private val repository = ServerRepository()
    private val logList =  ArrayList<ServerLogData>()
    private var tempMemberList = ArrayList<MemberData>()


    fun processIntent(intent: ServerIntent) {
        when (intent) {
            is ServerIntent.OnCreate -> onCreate()
        }
    }

    private fun onCreate() {
        showLog("伺服器啟動!")
        repository.onCatchTempUsersData()
        repository.onCatchGameAction()
        repository.onCatchRoomList()

        repository.setOnCatchDataFromDataBaseListener(object : OnCatchDataFromDataBaseListener{
            override fun onCatchTempUsersDataList(memberList: ArrayList<MemberData>) {

                //移除掉相同的Email且該Email保留最新的時間
                tempMemberList = memberList
                showLog("目前線上人數為:${getMemberCount()}位")
            }

            override fun onShowLog(msg: String) {
                showLog(msg)
            }
        })



    }


    private fun getMemberCount(): Int {


        var memberCount = 0
        for (memData in tempMemberList){
            if (memData.active){
                memberCount ++
            }
        }
        return memberCount
    }

    private fun showLog(log: String) {


        logList.add(ServerLogData(log,Util.getCurrentTime()))
        _viewState.value = ServerViewState(logList)

    }
}
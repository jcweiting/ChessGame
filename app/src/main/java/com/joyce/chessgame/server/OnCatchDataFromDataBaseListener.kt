package com.joyce.chessgame.server

import com.joyce.chessgame.server.bean.MemberData

interface OnCatchDataFromDataBaseListener {

    fun onCatchTempUsersDataList(memberList :ArrayList<MemberData>)

    fun onShowLog(msg:String)
}
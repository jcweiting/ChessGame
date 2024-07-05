package com.joyce.chessgame.offline

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joyce.chessgame.R
import com.joyce.chessgame.Util

class OfflineModeViewModel: ViewModel() {

    var showWinnerLiveData = MutableLiveData<String>()
    var blackChessActiveLiveData = MutableLiveData<Boolean>()
    var whiteChessActiveLiveData = MutableLiveData<Boolean>()

    fun onChangePlayer(blackChess: Boolean) {
        if (blackChess){
            blackChessActiveLiveData.value = true
        } else {
            whiteChessActiveLiveData.value = true
        }
    }

    fun showWinnerView(blackChess: Boolean) {
        showWinnerLiveData.value = if (blackChess) Util.getString(R.string.player1) else Util.getString(
            R.string.player2
        )
    }
}
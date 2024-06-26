package com.joyce.chessgame

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    var showPlayerLiveData = MutableLiveData<String>()
    var showWinnerLiveData = MutableLiveData<String>()

    fun onChangePlayer(blackChess: Boolean) {
        showPlayerLiveData.value = if (blackChess) Util.getString(R.string.player1) else Util.getString(R.string.player2)
    }

    fun showWinnerView(blackChess: Boolean) {
        showWinnerLiveData.value = if (blackChess) Util.getString(R.string.player1) else Util.getString(R.string.player2)
    }


}
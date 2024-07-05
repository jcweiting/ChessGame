package com.joyce.chessgame.multiple

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MultipleModeViewModel: ViewModel() {

    private var gamePerson = 2
    private var goFirst = 1       //1: 先手, 2:後手
    var waitingOpponentLiveData = MutableLiveData<Boolean>()
    var startGameLiveData = MutableLiveData<Boolean>()

    fun checkGamePerson() {
        if (gamePerson <= 1){
            waitingOpponentLiveData.value = true
        } else {
            startGameLiveData.value = true
        }
    }

}
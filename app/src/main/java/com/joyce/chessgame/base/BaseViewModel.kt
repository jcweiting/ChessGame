package com.joyce.chessgame.base

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.multiple.Actions

open class BaseViewModel: ViewModel() {

    private var db = FirebaseFirestore.getInstance()

    fun createRooms(actions: Actions, onComplete: () -> Unit){
        val actionCollection = db.collection("Actions")
        actions.roomName?.let { roomName ->
            db.runTransaction{ transaction ->
                val documentRef = actionCollection.document(roomName)
                val snapshot = transaction.get(documentRef)

                if (!snapshot.exists()){
                    transaction.set(documentRef, actions)
                }

            }.addOnSuccessListener {
                GameLog.i("*****房間新增-成功***** actions = ${Gson().toJson(actions)}")
                onComplete()
            }.addOnFailureListener {
                GameLog.i("*****房間新增-失敗 = $it")
                //TODO: 若有相同房間, server錯誤訊息會回在這裡嗎?
                onComplete()
            }
        }
    }

    fun sentStartGameNotification(actions: Actions, onComplete: () -> Unit){
        val actionCollection = db.collection("Actions")
        actions.roomId?.let { roomName ->
            db.runTransaction{ transaction ->
                val documentRef = actionCollection.document(roomName)
                val snapshot = transaction.get(documentRef)

                if (!snapshot.exists()){
                    transaction.set(documentRef, actions)
                }

            }.addOnSuccessListener {
                GameLog.i("*****動作新增-成功***** actions = ${Gson().toJson(actions)}")
                onComplete()
            }.addOnFailureListener {
                GameLog.i("*****動作新增-失敗***** ")
                onComplete()
            }
        }
    }
}
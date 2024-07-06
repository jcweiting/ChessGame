package com.joyce.chessgame.base

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.joyce.chessgame.multiple.Actions
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.ShareTool
import com.joyce.chessgame.login.LoginActivity
import kotlin.system.exitProcess

open class BaseActivity: AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private var userData = UserData()

    override fun onResume() {
        super.onResume()
        userData = ShareTool.getUserData()

        if (this !is LoginActivity){
            GameLog.i("非LoginActivity的base的onResume")
            if (!userData.email.isNullOrBlank()){
                userData.active = true
                addUserData(userData, "from onResume"){}
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!userData.email.isNullOrBlank()){
            userData.active = false
            addUserData(userData, "from onPause"){}
        }
    }

    fun newUserData(active: Boolean, email: String?, loginType: String){
        userData.active = active
        userData.email = email
        userData.timeStamp = System.currentTimeMillis()
        userData.loginType = loginType
        ShareTool.saveUserData(userData)
        addUserData(userData, "from newUserData"){
            if(this is LoginActivity){
                convertToMainActivity()
            }
        }
    }

    fun updateUserData(active: Boolean){
        userData.active = active
        ShareTool.saveUserData(userData)
        addUserData(userData, "from updateUserData"){
            exitProcess(0)
        }
    }

    private fun addUserData(user: UserData, from: String, onComplete:() -> Unit) {
        GameLog.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        GameLog.i("[$from] addUserData = ${Gson().toJson(userData)}")

        val usersCollection = db.collection("users")
        val uniqueId = "${user.email}_${user.loginType}"  // 使用 email 和 loginType 作為唯一標識

        db.runTransaction { transaction ->
            val documentRef = usersCollection.document(uniqueId)
            val snapshot = transaction.get(documentRef)

            if (!snapshot.exists()) {  // 如果文件不存在，新增一筆
                transaction.set(documentRef, user)
                GameLog.i("用戶不存在，新增: ${user.email}, [$from] userData = ${Gson().toJson(userData)}")

            } else {  // 如果文件存在，更新現有資料
                transaction.set(documentRef, user)
                GameLog.i("用戶存在，更新: ${user.email}, [$from] userData = ${Gson().toJson(userData)}")
            }

        }.addOnSuccessListener {
            GameLog.i("*****用戶資料更新-成功***** [$from] userData = ${Gson().toJson(userData)}")
            onComplete()

        }.addOnFailureListener {
            GameLog.i("*****用戶資料更新-失敗 = $it  [$from] userData = ${Gson().toJson(userData)}")
            onComplete()
        }
    }

    fun createRooms(actions: Actions, onComplete: () -> Unit){
        GameLog.i("actions = ${Gson().toJson(actions)}")

        val actionCollection = db.collection("Actions")
        val uniqueId = "${actions.host}_${actions.roomName}"

        db.runTransaction{ transaction ->
            val documentRef = actionCollection.document(uniqueId)
            val snapshot = transaction.get(documentRef)

            if (!snapshot.exists()){
                transaction.set(documentRef, actions)
                GameLog.i("房間不存在，新增: ${actions.host}, ${Gson().toJson(actions)}")
            }

        }.addOnSuccessListener {
            GameLog.i("*****房間新增-成功***** = ${Gson().toJson(userData)}")
            onComplete()
        }.addOnFailureListener {
            GameLog.i("*****房間新增-失敗 = $it, ${Gson().toJson(userData)}")
            onComplete()
        }
    }
}
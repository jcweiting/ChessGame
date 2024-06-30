package com.joyce.chessgame.base

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.ShareTool
import com.joyce.chessgame.UserData
import com.joyce.chessgame.login.LoginActivity

open class BaseActivity: AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private var userData = UserData()

    override fun onResume() {
        super.onResume()
        userData = ShareTool.getUserData()

        if (!userData.email.isNullOrBlank()){
            userData.active = true
            addUserData(userData, 1){}
        }
    }

    override fun onPause() {
        super.onPause()
        if (!userData.email.isNullOrBlank()){
            userData.active = false
            addUserData(userData, 2){}
        }
    }

    fun newUserData(active: Boolean, email: String?, loginType: String){
        userData.active = active
        userData.email = email
        userData.timeStamp = System.currentTimeMillis()
        userData.loginType = loginType
        ShareTool.saveUserData(userData)
        addUserData(userData, 3){
            if(this is LoginActivity){
                convertToMainActivity()
            }
        }
    }

    private fun addUserData(user: UserData, from: Int, onComplete:() -> Unit) {
        GameLog.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        GameLog.i("[$from] addUserData = ${Gson().toJson(userData)}")

        val usersCollection = db.collection("users")
        val uniqueId = "${user.email}_${user.loginType}"  // 使用 email 和 loginType 作為唯一標識

        db.runTransaction { transaction ->
            val documentRef = usersCollection.document(uniqueId)
            val snapshot = transaction.get(documentRef)

            if (!snapshot.exists()) {  // 如果文件不存在，新增一筆
                transaction.set(documentRef, user)
                GameLog.i("用戶不存在，新增: ${user.email}")

            } else {  // 如果文件存在，更新現有資料
                transaction.set(documentRef, user)
                GameLog.i("用戶存在，更新: ${user.email}")
            }

        }.addOnSuccessListener {
            GameLog.i("*****用戶資料更新-成功*****")
            onComplete()

        }.addOnFailureListener {
            GameLog.i("*****用戶資料更新-失敗 = $it")
            onComplete()
        }
    }
}
package com.joyce.chessgame.login

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.FACEBOOK
import com.joyce.chessgame.GlobalConfig.Companion.GOOGLE
import com.joyce.chessgame.MyApplication

class LoginViewModel: ViewModel() {

    var authResult = MutableLiveData<Boolean>()
    var firebaseAuthLiveData = MutableLiveData<GoogleSignInAccount>()
    var showProgressBarLiveData = MutableLiveData<Boolean>()
    var isAutoLoginLiveData = MutableLiveData<Pair<String, String>>()
    private var isLoginByBtn = false

    fun checkResponseCode(requestCode: Int, data: Intent?) {
        showProgressBarLiveData.value = true

        if (requestCode == LoginActivity.RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthLiveData.value = account

            } catch (e: ApiException) {
                GameLog.i("google auth error = $e")
                authResult.value = false
                showProgressBarLiveData.value = false
            }
        }
    }

    fun hideProgressBar(){
        showProgressBarLiveData.value = false
    }

    fun setIsLoginByBtn(pressBtnLogin: Boolean){
        isLoginByBtn = pressBtnLogin
    }

    /**是否有自動登入*/
    fun checkAutoLogin() {
        if (!isLoginByBtn){
            checkedLoginRecord()
        }
    }

    /**確認是否已登入*/
    private fun checkedLoginRecord() {
        var isFbLoggedIn = false
        var isGoogleLoggedIn = false

        //FB
        val fbToken = AccessToken.getCurrentAccessToken()
        if (fbToken != null && !fbToken.isExpired){
            isFbLoggedIn = true
            GraphRequest.newMeRequest(fbToken){ jsonObject, _ ->
                val email = jsonObject?.optString("email")
                email?.let {
                    isAutoLoginLiveData.value = Pair(it, FACEBOOK)
                }
                GameLog.i("FB自動登入，確認Email是否相同 = $email")
            }
        }

        if (!isFbLoggedIn){
            //google
            val googleAcc = GoogleSignIn.getLastSignedInAccount(MyApplication.instance)
            if (googleAcc != null){
                GameLog.i("Google自動登入，確認Email是否相同 = ${googleAcc.email}")
                isGoogleLoggedIn = true
                googleAcc.email?.let {
                    isAutoLoginLiveData.value = Pair(it, GOOGLE)
                }
            }
        }

        GameLog.i("用GOOGLE帳號 = $isGoogleLoggedIn | 用FB帳號 = $isFbLoggedIn")
    }
}
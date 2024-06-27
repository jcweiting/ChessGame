package com.joyce.chessgame.login

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.joyce.chessgame.GameLog

class LoginViewModel: ViewModel() {

    var authResult = MutableLiveData<Boolean>()
    var firebaseAuthLiveData = MutableLiveData<GoogleSignInAccount>()
    var showProgressBarLiveData = MutableLiveData<Boolean>()

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
}
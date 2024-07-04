package com.joyce.chessgame.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.FACEBOOK
import com.joyce.chessgame.GlobalConfig.Companion.GOOGLE
import com.joyce.chessgame.MyApplication
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityLoginBinding
import com.joyce.chessgame.menu.MenuActivity
import org.json.JSONException

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private var loginType = ""

    companion object{
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameLog.i("LoginActivity onCreate ================================")

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initLogin()
        buttonCollection()
        liveDataCollection()

        viewModel.checkAutoLogin()
    }

    private fun initLogin() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        //init callback Manager
        callbackManager = CallbackManager.Factory.create()
    }

    private fun liveDataCollection() {
        viewModel.isAutoLoginLiveData.observe(this){
            loginType = it.second
            newUserData(true, it.first, it.second)
        }

        viewModel.authResult.observe(this){
            showLoginFailedDialog()
        }

        viewModel.firebaseAuthLiveData.observe(this){ data ->
            firebaseAuthWithGoogle(data)
        }

        viewModel.showProgressBarLiveData.observe(this){
            showProgressBar(it)
        }
    }

    private fun buttonCollection() {
        binding.cnsFbLogin.setOnClickListener {
            viewModel.setIsLoginByBtn(true)
            fbLogin()
        }

        binding.cnsGoogleLogin.setOnClickListener {
            viewModel.setIsLoginByBtn(true)
            googleSignIn()
        }
    }

    private fun fbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onCancel() {
                Toast.makeText(this@LoginActivity, getString(R.string.cancel_login), Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                GameLog.i("FB Login Failed = ${error.message}")
                showLoginFailedDialog()
            }

            override fun onSuccess(result: LoginResult) {
                GameLog.i("fb login success")

                val accessToken = result.accessToken
                GraphRequest.newMeRequest(accessToken){`object`, _ ->
                    try {
                        val email = `object`?.getString("email")
                        val name = `object`?.getString("name")

                        loginType = FACEBOOK
                        newUserData(true, email, loginType)

                    } catch (e: JSONException){
                        e.printStackTrace()
                        showLoginFailedDialog()
                    }
                }
            }
        })
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.checkResponseCode(requestCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        viewModel.hideProgressBar()

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    val user = auth.currentUser
                    user?.let {
                        loginType = GOOGLE
                        newUserData(true, user.email, loginType)
                    }

                } else {
                    showLoginFailedDialog()
                }
            }
    }

    fun convertToMainActivity() {
        showProgressBar(false)
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showProgressBar(isShowed:Boolean){
        when(isShowed){
            true -> {
                binding.pbLogin.visibility = View.VISIBLE
                binding.maskLogin.visibility = View.VISIBLE
            }
            false -> {
                binding.pbLogin.visibility = View.GONE
                binding.maskLogin.visibility = View.GONE
            }
        }
    }

    private fun showLoginFailedDialog(){
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.login_failed))
            .setMessage(getString(R.string.try_later))
            .setPositiveButton(getString(R.string.confirm), null)
            .show()
    }
}
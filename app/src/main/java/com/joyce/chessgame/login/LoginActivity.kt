package com.joyce.chessgame.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
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
import com.joyce.chessgame.MainActivity
import com.joyce.chessgame.R
import com.joyce.chessgame.databinding.ActivityLoginBinding
import org.json.JSONException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    companion object{
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initLogin()
        buttonCollection()
        liveDataCollection()
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
        binding.cnsGoogleLogin.setOnClickListener {
            googleSignIn()
        }

        binding.cnsFbLogin.setOnClickListener {
            fbLogin()
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
                val request = GraphRequest.newMeRequest(accessToken){`object`, _ ->
                    try {
                        val email = `object`?.getString("email")
                        val name = `object`?.getString("name")
                        GameLog.i("登入成功, 用戶訊息 = $name & $email")
                    } catch (e: JSONException){
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id, name, email")
                request.parameters = parameters
                request.executeAsync()

                convertToMainActivity()
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
                    convertToMainActivity()

                } else {
                    showLoginFailedDialog()
                }
            }
    }

    private fun convertToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
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
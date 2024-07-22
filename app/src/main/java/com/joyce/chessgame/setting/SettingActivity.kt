package com.joyce.chessgame.setting

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.joyce.chessgame.MyApplication
import com.joyce.chessgame.R
import com.joyce.chessgame.ShareTool
import com.joyce.chessgame.Util
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivitySettingBinding
import com.joyce.chessgame.login.LoginActivity

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        buttonCollection()
    }

    private fun init(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        binding.lifecycleOwner = this

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(Util.getContext(), gso)
    }

    override fun onResume() {
        super.onResume()
        binding.switchMusic.isChecked = ShareTool.getMusicSetting()
        binding.switchSound.isChecked = ShareTool.getSoundEffect()
    }

    private fun buttonCollection() {
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            ShareTool.saveMusicSetting(isChecked)
            MyApplication.instance.turnOnBgMusic(isChecked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            ShareTool.saveSoundEffect(isChecked)
        }

        binding.tvLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Util.showAlertDialog(this, getString(R.string.error), getString(R.string.failed_logout))
                }
            }
    }
}
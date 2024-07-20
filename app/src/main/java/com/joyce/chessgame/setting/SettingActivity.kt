package com.joyce.chessgame.setting

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.joyce.chessgame.MyApplication
import com.joyce.chessgame.R
import com.joyce.chessgame.ShareTool
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        binding.lifecycleOwner = this

        buttonCollection()
    }

    override fun onResume() {
        super.onResume()

        binding.switchMusic.isChecked = ShareTool.getMusicSetting()
    }

    private fun buttonCollection() {
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            ShareTool.saveMusicSetting(isChecked)
            MyApplication.instance.turnOnBgMusic(isChecked)
        }
    }
}
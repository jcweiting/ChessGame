package com.joyce.chessgame.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GlobalConfig.Companion.MODE_TYPE
import com.joyce.chessgame.GlobalConfig.Companion.OFFLINE
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMenuBinding
import com.joyce.chessgame.offline.OfflineModeActivity
import com.joyce.chessgame.multiple.MultipleLobbyActivity

class MenuActivity : BaseActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MenuViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        buttonCollection()
    }

    private fun buttonCollection() {
        //離線模式
        binding.cnsOfflinePlay.setOnClickListener {
            convertToGameBoard(OFFLINE)
        }

        //連線模式
        binding.cnsMultipleMode.setOnClickListener {
            startActivity(Intent(this, MultipleLobbyActivity::class.java))
        }

        //設定
        binding.cnsSetting.setOnClickListener {
            //TODO: 設定
        }

        //離開
        binding.cnsExit.setOnClickListener {
            showProgressBar(true)
            updateUserData(active = false)
        }
    }

    private fun convertToGameBoard(modeType: String) {
        val intent = Intent(this, OfflineModeActivity::class.java)
        intent.putExtra(MODE_TYPE, modeType)
        startActivity(intent)
    }

    private fun showProgressBar(isShowed:Boolean){
        when(isShowed){
            true -> {
                binding.pbMenu.visibility = View.VISIBLE
                binding.maskMenu.visibility = View.VISIBLE
            }
            false -> {
                binding.pbMenu.visibility = View.GONE
                binding.maskMenu.visibility = View.GONE
            }
        }
    }
}
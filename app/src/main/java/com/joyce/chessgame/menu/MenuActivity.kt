package com.joyce.chessgame.menu

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GlobalConfig.Companion.MODE_TYPE
import com.joyce.chessgame.GlobalConfig.Companion.MULTIPLE
import com.joyce.chessgame.GlobalConfig.Companion.OFFLINE
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMenuBinding
import com.joyce.chessgame.game_board.GameBoardActivity

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
            convertToGameBoard(MULTIPLE)
        }
    }

    private fun convertToGameBoard(modeType: String) {
        val intent = Intent(this, GameBoardActivity::class.java)
        intent.putExtra(MODE_TYPE, modeType)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

}
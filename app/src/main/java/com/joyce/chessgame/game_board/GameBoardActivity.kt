package com.joyce.chessgame.game_board

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityGameBoardBinding

class GameBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityGameBoardBinding
    private lateinit var viewModel: GameBoardViewModel

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[GameBoardViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_board)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        buttonCollection()
        gameBoardListener()
        liveDataCollection()
    }

    @SuppressLint("SetTextI18n")
    private fun liveDataCollection() {
        viewModel.showPlayerLiveData.observe(this){
            binding.tvPlayer.text = it
        }

        viewModel.showWinnerLiveData.observe(this){
            binding.tvWin.visibility = View.VISIBLE
            binding.tvWin.text = it + "Win!"
        }
    }

    private fun gameBoardListener() {
        binding.gameBoard.setGameBoardListener(object : GameBoard.OnGameBoardListener {
            override fun onChangePlayer(isBlackChess: Boolean) {
                viewModel.onChangePlayer(isBlackChess)
            }

            override fun onConnectInLine(isBlackChess: Boolean) {
                viewModel.showWinnerView(isBlackChess)
            }
        })
    }

    private fun buttonCollection() {
        binding.tvClear.setOnClickListener {
            binding.gameBoard.onClearGameBoard()
        }

        binding.tvWin.setOnClickListener {
            binding.tvWin.visibility = View.GONE
            binding.gameBoard.onClearGameBoard()
        }
    }
}
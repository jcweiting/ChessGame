package com.joyce.gomoku

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.gomoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
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
        binding.gameBoard.setGameBoardListener(object : GameBoard.OnGameBoardListener{
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
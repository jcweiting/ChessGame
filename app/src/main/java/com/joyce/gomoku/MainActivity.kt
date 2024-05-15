package com.joyce.gomoku

import android.os.Bundle
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
    }

    private fun gameBoardListener() {
        binding.gameBoard.setGameBoardListener(object : GameBoard.OnGameBoardListener{
            override fun onChangePlayer(isBlackChess: Boolean) {
                binding.tvPlayer.text = if (isBlackChess) getString(R.string.player1) else getString(R.string.player2)
            }
        })
    }

    private fun buttonCollection() {
        binding.tvClear.setOnClickListener {
            binding.gameBoard.onClearGameBoard()
        }
    }
}
package com.joyce.gomoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
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

        })
    }

    private fun buttonCollection() {
        binding.tvClear.setOnClickListener {
            binding.gameBoard.onClearGameBoard()
        }
    }
}
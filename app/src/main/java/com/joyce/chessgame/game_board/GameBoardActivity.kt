package com.joyce.chessgame.game_board

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.MODE_TYPE
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityGameBoardBinding

class GameBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityGameBoardBinding
    private lateinit var viewModel: GameBoardViewModel
    private var modeType: String? = null
    private var countDownTimer: CountDownTimer? = null

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

        modeType = intent.getStringExtra(MODE_TYPE)
        modeType?.let {
            binding.gameBoard.setModeType(it)
        }
        GameLog.i("play mode = $modeType")

        buttonCollection()
        gameBoardListener()
        liveDataCollection()
    }

    @SuppressLint("SetTextI18n")
    private fun liveDataCollection() {
        viewModel.showWinnerLiveData.observe(this){
            binding.tvWin.visibility = View.VISIBLE
            binding.tvWin.text = it + "Win!"
        }

        viewModel.blackChessActiveLiveData.observe(this){
            isActiveBlackChess()
        }

        viewModel.whiteChessActiveLiveData.observe(this){
            isActiveWhiteChess()
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

            override fun onStartCountDown() {
                startCountDownTimer()
            }

            override fun onStartCountTime() {

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

    private fun isActiveBlackChess(){
        binding.imFlag1.visibility = View.VISIBLE
        binding.imFlag2.visibility = View.GONE
        binding.tvPlayer1.setBackgroundResource(R.drawable.bg_player_active)
        binding.tvPlayer2.setBackgroundResource(R.drawable.bg_player_inactive)
    }

    private fun isActiveWhiteChess(){
        binding.imFlag1.visibility = View.GONE
        binding.imFlag2.visibility = View.VISIBLE
        binding.tvPlayer1.setBackgroundResource(R.drawable.bg_player_inactive)
        binding.tvPlayer2.setBackgroundResource(R.drawable.bg_player_active)
    }

    private fun startCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = object: CountDownTimer(21000, 1000){
            @SuppressLint("SetTextI18n")
            //倒數中
            override fun onTick(millisUntilFinished: Long) {
                val secondRemaining = millisUntilFinished/1000
                binding.tvTimingCountDown.text = "$secondRemaining 秒"
            }

            //倒數計時結束
            override fun onFinish() {
                //TODO: 自動下子
            }
        }
        countDownTimer?.start()
    }
}
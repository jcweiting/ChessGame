package com.joyce.chessgame.multiple

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GlobalConfig.Companion.CHARACTER
import com.joyce.chessgame.GlobalConfig.Companion.MULTIPLE
import com.joyce.chessgame.GlobalConfig.Companion.ROOM_ID
import com.joyce.chessgame.R
import com.joyce.chessgame.Util.showAlertDialog
import com.joyce.chessgame.Util.showAlertDialogWithNegative
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.base.GameBoard
import com.joyce.chessgame.databinding.ActivityMultipleModeBinding

class MultipleModeActivity : BaseActivity() {

    private lateinit var binding: ActivityMultipleModeBinding
    private lateinit var viewModel: MultipleModeViewModel
    private var roomId: String? = null
    private var character: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MultipleModeViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_mode)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        roomId = intent.getStringExtra(ROOM_ID)
        character = intent.getStringExtra(CHARACTER)

        init(roomId, character)
        buttonCollection()
        liveDataCollection()
        gameBoardListener()
    }

    private fun init(roomId: String?, character: String?) {
        viewModel.initView(roomId, character)
        binding.multipleGameBoard.setModeType(MULTIPLE)
    }

    @SuppressLint("SetTextI18n")
    private fun liveDataCollection() {
        //更新棋盤
        viewModel.updateChessBoard.observe(this){
            binding.multipleGameBoard.updateChessCollection(it.first, it.second)
        }

        viewModel.autoPlaceChessLiveData.observe(this){
            binding.multipleGameBoard.placeRandomChess()
        }

        viewModel.isMyTurnLiveData.observe(this){
            binding.multipleGameBoard.setIsMyTurn(it)
            updateMyTextStyle(it)
        }

        //顯示先手or後手
        viewModel.whoFirstLiveData.observe(this){
            binding.tvFirstMove.text = it
            showFirstMove()
        }

        viewModel.setTextLiveData.observe(this){
            binding.tvMultipleMe.text = it.first
            binding.tvMultipleOther.text = it.second
        }

        //Btn Style
        viewModel.updateBackBtnStyleLiveData.observe(this){
            isShowMask(true, R.color.mask_multiple)
            binding.tvBack.setBackgroundResource(it.first)
            binding.tvBack.text = it.second
        }

        //遊戲開始倒數
        viewModel.gameStartCountDownLiveData.observe(this){
            binding.progressBarMultiple.visibility = View.GONE
            binding.tvGameStartCountDown.visibility = View.VISIBLE
            binding.tvGameStartCountDown.text = it
        }

        //倒數結束
        viewModel.isFinishedCountDownLiveData.observe(this){
            binding.tvGameStartCountDown.visibility = View.GONE
            binding.progressBarMultiple.visibility = View.VISIBLE
        }

        //等待中dialog
        viewModel.isShowWaitingDialog.observe(this){
            isShowWaitingDialog(it)
        }

        //顯示開始對弈按鈕
        viewModel.showStartGameBtnLiveDta.observe(this){
            isShowGameStart(it)
        }

        //房間資訊
        viewModel.setRoomInfoLiveData.observe(this){
            binding.tvMultipleRoomName.text = getString(R.string.room_name) + it.first
            binding.tvOpponent.text = getString(R.string.oppsite_player) + it.second
        }

        //放棄遊戲
        viewModel.giveUpGameAlertLiveData.observe(this){
            showAlertDialogWithNegative(this, getString(R.string.error), getString(R.string.sure_to_quit), true, getString(R.string.confirm), getString(R.string.cancel)){
                viewModel.leftRoom()
            }
        }

        //我方倒數計時
        viewModel.myRemainTimeLiveData.observe(this){
            binding.tvCountDownMe.text = it + getString(R.string.second)
        }

        //對方倒數計時
        viewModel.opponentTimeRemainLiveData.observe(this){
            binding.tvCountDownOpposite.text = it + getString(R.string.second)
        }

        viewModel.showAlertDialogLiveData.observe(this){
            showAlertDialog(this, getString(R.string.error), it)
        }

        viewModel.showLeftRoomDialogLiveData.observe(this){
            showAlertDialogWithNegative(this, null, it, false, getString(R.string.confirm), ""){
                finish()
            }
        }

        viewModel.backToPreviousPageLiveData.observe(this){
            finish()
        }

        viewModel.showWinnerLiveData.observe(this){
            showWinnerView(it)
        }
    }

    private fun buttonCollection() {
        binding.maskMultipleWaiting.setOnClickListener {}
        binding.cnsWaiting.setOnClickListener {}

        binding.tvBack.setOnClickListener {
            viewModel.checkButtonType(binding.tvBack.text.toString())
        }

        //開始對弈
        binding.cnsGameStart.setOnClickListener {
            isShowGameStart(false)
            binding.progressBarMultiple.visibility = View.VISIBLE
            viewModel.sentGameStartToServer(roomId)
        }
    }

    private fun gameBoardListener() {
        binding.multipleGameBoard.setGameBoardListener(object : GameBoard.OnGameBoardListener{
            override fun onChangePlayer(isBlackChess: Boolean) {
                initPlayerStyle()
                isShowMask(true, android.R.color.transparent)
            }

            override fun onConnectInLine(isBlackChess: Boolean) {
                viewModel.gameEnd(isBlackChess, binding.tvMultipleMe.text.toString(), binding.tvMultipleOther.text.toString())
            }

            override fun onStartCountDown() {
                viewModel.startCountDownTimer()
            }
        })

        binding.multipleGameBoard.setMultipleModeListener(object : GameBoard.OnMultipleModeListener{
            override fun onChessLocation(x: Long, y: Long) {
                viewModel.sentChessLocation(x, y)
            }
        })
    }

    private fun updateMyTextStyle(isMyTurn: Boolean){
        when(isMyTurn){
            true -> {
                isShowMask(false, android.R.color.transparent)

                binding.tvMultipleMe.setBackgroundResource(R.drawable.bg_player_active)
                binding.imMultipleFlagMe.visibility = View.VISIBLE
                binding.tvCountDownMe.visibility = View.VISIBLE

                binding.tvMultipleOther.setBackgroundResource(R.drawable.bg_player_inactive)
                binding.imMultipleFlagOther.visibility = View.GONE
                binding.tvCountDownOpposite.visibility = View.GONE
            }
            false -> {
                isShowMask(true, android.R.color.transparent)

                binding.tvMultipleMe.setBackgroundResource(R.drawable.bg_player_inactive)
                binding.imMultipleFlagMe.visibility = View.GONE
                binding.tvCountDownMe.visibility = View.GONE

                binding.tvMultipleOther.setBackgroundResource(R.drawable.bg_player_active)
                binding.imMultipleFlagOther.visibility = View.VISIBLE
                binding.tvCountDownOpposite.visibility = View.VISIBLE
            }
        }
    }

    private fun initPlayerStyle(){
        binding.tvMultipleMe.setBackgroundResource(R.drawable.bg_player_inactive)
        binding.tvMultipleOther.setBackgroundResource(R.drawable.bg_player_inactive)
        binding.imMultipleFlagMe.visibility = View.GONE
        binding.imMultipleFlagOther.visibility = View.GONE
        binding.tvCountDownMe.visibility = View.GONE
        binding.tvCountDownOpposite.visibility = View.GONE
    }

    private fun isShowMask(isShow: Boolean, color: Int){
        binding.maskMultipleWaiting.setBackgroundResource(color)
        when(isShow){
            true -> binding.maskMultipleWaiting.visibility = View.VISIBLE
            false -> binding.maskMultipleWaiting.visibility = View.GONE
        }
    }

    private fun isShowWaitingDialog(isShow: Boolean){
        when(isShow){
            true -> {
                isShowMask(true, R.color.mask_multiple)
                binding.cnsWaiting.visibility = View.VISIBLE
            }
            false -> {
                isShowMask(false, R.color.mask_multiple)
                binding.cnsWaiting.visibility = View.GONE
            }
        }
    }

    private fun isShowGameStart(isShow: Boolean){
        when(isShow){
            true -> {
                isShowMask(true, R.color.mask_multiple)
                binding.cnsGameStart.visibility = View.VISIBLE
            }
            false -> {
                isShowMask(false, R.color.mask_multiple)
                binding.cnsGameStart.visibility = View.GONE
            }
        }
    }

    private fun showFirstMove() {
        binding.progressBarMultiple.visibility = View.GONE
        binding.tvFirstMove.visibility = View.VISIBLE
        //加載進入動畫
        val slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left)
        //加載淡出動畫
        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        //啟動進入動畫
        binding.tvFirstMove.startAnimation(slideInAnimation)
        //10秒後淡出
        Handler().postDelayed({
            binding.tvFirstMove.startAnimation(fadeOutAnimation)
            binding.tvFirstMove.visibility = TextView.GONE

            isShowMask(false, R.color.mask_multiple)
            viewModel.startCountDownTimer()
        },1000)
    }

    private fun showWinnerView(winner: String){
        binding.tvCountDownMe.visibility = View.GONE
        binding.tvCountDownOpposite.visibility = View.GONE

        binding.tvMultipleWin.visibility = View.VISIBLE
        binding.imMultipleWin.visibility = View.VISIBLE
        binding.tvMultipleWin.text = winner
    }

    private fun hideWinnerView(){
        binding.tvMultipleWin.visibility = View.GONE
        binding.imMultipleWin.visibility = View.GONE
    }
}
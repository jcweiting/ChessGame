package com.joyce.chessgame.multiple

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.GlobalConfig.Companion.ROOM_ID
import com.joyce.chessgame.GlobalFunction.showAlertDialog
import com.joyce.chessgame.GlobalFunction.showAlertDialogWithNegative
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMultipleModeBinding

class MultipleModeActivity : BaseActivity() {

    private lateinit var binding: ActivityMultipleModeBinding
    private lateinit var viewModel: MultipleModeViewModel
    private var roomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MultipleModeViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_mode)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        roomId = intent.getStringExtra(ROOM_ID)

        init(roomId)
        buttonCollection()
        liveDataCollection()
    }

    private fun init(roomId: String?) {
        viewModel.initView(roomId)
    }

    private fun liveDataCollection() {
        viewModel.showWhoFirstLiveData.observe(this){
            binding.tvMultipleMe.text = it.first
            binding.tvMultipleOther.text = it.second
        }

        //遊戲開始倒數
        viewModel.gameStartCountDownLiveData.observe(this){
            binding.tvGameStartCountDown.visibility = View.VISIBLE
            binding.tvGameStartCountDown.text = it
        }

        //倒數結束
        viewModel.isFinishedCountDownLiveData.observe(this){
            binding.tvGameStartCountDown.visibility = View.GONE
            binding.maskMultipleWaiting.visibility = View.GONE
            binding.tvBack.setBackgroundResource(R.drawable.bg_brown1_radius10)
            showFirstMove()
        }

        //等待對手
        viewModel.waitingOpponentLiveData.observe(this){
            showWaitingDialog()
        }

        //開始遊戲
        viewModel.startGameLiveData.observe(this){
            hideWaitingDialog()
            showGameStart()
        }

        //放棄遊戲
        viewModel.giveUpGameAlertLiveData.observe(this){
            showAlertDialogWithNegative(this, getString(R.string.error), getString(R.string.sure_to_quit), true, getString(R.string.confirm), getString(R.string.canel)){
                viewModel.isQuitGame(it)
            }
        }

        viewModel.showAlertDialogLiveData.observe(this){
            showAlertDialog(this, getString(R.string.error), it)
        }

        viewModel.backToPreviousPageLiveData.observe(this){
            finish()
        }
    }

    private fun buttonCollection() {
        binding.maskMultipleWaiting.setOnClickListener {  }
        binding.cnsWaiting.setOnClickListener {  }

        binding.tvBack.setOnClickListener {
            viewModel.checkButtonType(binding.tvBack.text.toString())
        }

        //開始對弈
        binding.cnsGameStart.setOnClickListener {
            hideGameStart()
            viewModel.sentGameStartToServer(roomId)
        }
    }

    private fun showWaitingDialog(){
        binding.maskMultipleWaiting.visibility = View.VISIBLE
        binding.cnsWaiting.visibility = View.VISIBLE
    }

    private fun hideWaitingDialog(){
        binding.maskMultipleWaiting.visibility = View.GONE
        binding.cnsWaiting.visibility = View.GONE
    }

    private fun showGameStart() {
        binding.maskMultipleWaiting.visibility = View.VISIBLE
        binding.cnsGameStart.visibility = View.VISIBLE
    }

    private fun hideGameStart() {
        binding.cnsGameStart.visibility = View.GONE
        binding.tvBack.setBackgroundResource(R.drawable.bg_grey_radius10)
    }

    private fun showFirstMove() {
        binding.tvBack.text = getString(R.string.give_up)
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
        },1000)

        viewModel.startCountDownTimer()
    }
}
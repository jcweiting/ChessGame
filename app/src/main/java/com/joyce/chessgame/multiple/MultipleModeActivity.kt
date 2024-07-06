package com.joyce.chessgame.multiple

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMultipleModeBinding

class MultipleModeActivity : BaseActivity() {

    private lateinit var binding: ActivityMultipleModeBinding
    private lateinit var viewModel: MultipleModeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MultipleModeViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_mode)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        init()
        buttonCollection()
        liveDataCollection()
    }

    private fun init(){
        viewModel.checkGamePerson()
    }

    private fun liveDataCollection() {
        //等待對手
        viewModel.waitingOpponentLiveData.observe(this){
            showWaitingDialog()
        }

        //開始遊戲
        viewModel.startGameLiveData.observe(this){
            hideWaitingDialog()
            showFirstMove()
        }
    }

    private fun showFirstMove() {
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
        },3000)
    }

    private fun buttonCollection() {
        binding.maskMultipleWaiting.setOnClickListener {  }
        binding.cnsWaiting.setOnClickListener {  }
        binding.tvBack.setOnClickListener { finish() }

    }

    private fun showWaitingDialog(){
        binding.maskMultipleWaiting.visibility = View.VISIBLE
        binding.cnsWaiting.visibility = View.VISIBLE
    }

    private fun hideWaitingDialog(){
        binding.maskMultipleWaiting.visibility = View.GONE
        binding.cnsWaiting.visibility = View.GONE
    }
}
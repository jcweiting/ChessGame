package com.joyce.chessgame.server

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.R
import com.joyce.chessgame.databinding.ActivityServerBinding

class ServerActivity : AppCompatActivity() {

    private lateinit var viewModel: ServerViewModel
    private lateinit var dataBinding: ActivityServerBinding
    private var adapter : ServerLogAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        dataBinding = DataBindingUtil.setContentView(this,R.layout.activity_server)
        viewModel = ViewModelProvider(this)[ServerViewModel::class.java]

        viewModel.processIntent(ServerIntent.OnCreate)

        viewModel.viewState.observe(this){
            if (it.logList != null){
                if (adapter == null) {
                    adapter = ServerLogAdapter(it.logList)
                    dataBinding.logList.adapter = adapter
                    dataBinding.logList.scrollToPosition(it.logList.size - 1)
                }else{
                    adapter?.updateData(it.logList)
                    dataBinding.logList.scrollToPosition(it.logList.size - 1)
                }
            }

        }




    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

}
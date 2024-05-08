package com.joyce.gomoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonCollection()
    }

    private fun buttonCollection() {
        //TODO: 重新開始的點擊事件
    }
}
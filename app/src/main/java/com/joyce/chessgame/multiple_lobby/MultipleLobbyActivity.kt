package com.joyce.chessgame.multiple_lobby

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.R
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMultipleLobbyBinding

class MultipleLobbyActivity : BaseActivity() {

    private lateinit var binding: ActivityMultipleLobbyBinding
    private lateinit var viewModel: MultipleLobbyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MultipleLobbyViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_lobby)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }
}
package com.joyce.chessgame.multiple

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GlobalConfig.Companion.CREATE_ROOM
import com.joyce.chessgame.GlobalConfig.Companion.SEARCH_ROOM
import com.joyce.chessgame.GlobalFunction.editTextMaxLength
import com.joyce.chessgame.GlobalFunction.hideKeyBoard
import com.joyce.chessgame.GlobalFunction.showAlertDialog
import com.joyce.chessgame.R
import com.joyce.chessgame.ShareTool
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

        init()
        buttonCollection()
        liveDataCollection()
    }

    private fun init(){
        binding.edRoomName.filters = editTextMaxLength(15)
        binding.edSearchRoomName.filters = editTextMaxLength(15)
    }

    private fun liveDataCollection() {

        viewModel.showCreateRoomViewLiveData.observe(this){
            binding.cnsRoomName.visibility = View.VISIBLE
            binding.cnsCreateRoom.setBackgroundResource(R.drawable.bg_brown_radius15)
        }

        viewModel.hideCreateRoomViewLiveData.observe(this){
            hideCreateRoom()
        }

        viewModel.showSearchRoomContentLiveData.observe(this){
            binding.cnsSearchRoomName.visibility = View.VISIBLE
            binding.cnsSearchRoom.setBackgroundResource(R.drawable.bg_brown_radius15)
        }

        viewModel.hideSearchRoomContentLiveData.observe(this){
            hideKeyBoard(this)
            binding.edSearchRoomName.text = null
            binding.cnsSearchRoomName.visibility = View.GONE
            binding.cnsSearchRoom.setBackgroundResource(0)
        }

        viewModel.showAlertLiveData.observe(this){
            showAlertDialog(this, getString(R.string.error), it)
        }

        viewModel.isCreateRoomLiveData.observe(this){
            createRoom(it)
        }

        viewModel.isSearchRoomLiveData.observe(this){
            //TODO: 開始搜尋房間
        }

        viewModel.isShowProgressBarLiveData.observe(this){
            showProgressBar(it)
        }
    }

    private fun buttonCollection() {
        //加入房間
        binding.tvAddRoom.setOnClickListener {
            viewModel.checkOptionUiUpdate(isAddRoom = true, isCreateRoom = false, isSearchRoom = false)
        }

        //創建房間
        binding.tvCreateRoom.setOnClickListener {
            viewModel.checkOptionUiUpdate(isAddRoom = false, isCreateRoom = true, isSearchRoom = false)
        }

        //搜尋房間
        binding.tvSearchRoom.setOnClickListener {
            viewModel.checkOptionUiUpdate(isAddRoom = false, isCreateRoom = false, isSearchRoom = true)
        }

        //確定創建
        binding.tvRoomNameConfirm.setOnClickListener {
            hideKeyBoard(this)
            viewModel.checkRoomName(binding.edRoomName.text.toString().trim(), CREATE_ROOM)
        }

        //確定搜尋
        binding.tvSearchRoomNameConfirm.setOnClickListener {
            viewModel.checkRoomName(binding.edSearchRoomName.text.toString().trim(), SEARCH_ROOM)
        }
    }

    private fun hideCreateRoom() {
        hideKeyBoard(this)
        binding.edRoomName.text = null
        binding.cnsRoomName.visibility = View.GONE
        binding.cnsCreateRoom.setBackgroundResource(0)
    }

    private fun createRoom(roomName: String) {
        val actions = Actions(1, ShareTool.getUserData().email, System.currentTimeMillis(), roomName)
        createRooms(actions){
            hideCreateRoom()
            showProgressBar(false)
            startActivity(Intent(this, MultipleModeActivity::class.java))
        }
    }

    private fun showProgressBar(isShowed:Boolean){
        when(isShowed){
            true -> {
                binding.pbMultipleLobby.visibility = View.VISIBLE
                binding.maskMultipleLobby.visibility = View.VISIBLE
            }
            false -> {
                binding.pbMultipleLobby.visibility = View.GONE
                binding.maskMultipleLobby.visibility = View.GONE
            }
        }
    }

}
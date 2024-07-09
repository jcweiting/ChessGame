package com.joyce.chessgame.multiple

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.joyce.chessgame.GlobalConfig.Companion.CREATE_ROOM
import com.joyce.chessgame.GlobalConfig.Companion.ROOM_ID
import com.joyce.chessgame.GlobalConfig.Companion.SEARCH_ROOM
import com.joyce.chessgame.R
import com.joyce.chessgame.Util.editTextMaxLength
import com.joyce.chessgame.Util.hideKeyBoard
import com.joyce.chessgame.Util.showAlertDialog
import com.joyce.chessgame.base.BaseActivity
import com.joyce.chessgame.databinding.ActivityMultipleLobbyBinding

class MultipleLobbyActivity : BaseActivity() {

    private lateinit var binding: ActivityMultipleLobbyBinding
    private lateinit var viewModel: MultipleLobbyViewModel
    private var roomsAdapter = RoomsListAdapter()

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

    override fun onResume() {
        super.onResume()
        viewModel.checkRoomsList()
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

        viewModel.isSearchRoomLiveData.observe(this){
            //TODO: 開始搜尋房間
        }

        viewModel.isShowProgressBarLiveData.observe(this){
            showProgressBar(it)
        }

        //房間列表
        viewModel.roomsArrayLiveData.observe(this){
            roomsAdapter.setRoomsArr(it)
            binding.rvRooms.adapter = roomsAdapter
        }

        viewModel.isCreateRoomsSuccessLiveData.observe(this){ roomId ->
            val intent = Intent(this, MultipleModeActivity::class.java)
            intent.putExtra(ROOM_ID, roomId)
            startActivity(intent)
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
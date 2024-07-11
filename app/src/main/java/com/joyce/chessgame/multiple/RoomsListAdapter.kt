package com.joyce.chessgame.multiple

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.R
import com.joyce.chessgame.Util

class RoomsListAdapter: RecyclerView.Adapter<RoomsListAdapter.ViewHolder>() {

    private var roomsArray = ArrayList<GameRoomData>()

    fun setRoomsArr(roomsArr: ArrayList<GameRoomData>){
        this.roomsArray = roomsArr
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_rooms, parent, false))
    }

    override fun getItemCount(): Int {
        return roomsArray.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gameRoomData = roomsArray[position]
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tv_room_title)
        val tvPeople = holder.itemView.findViewById<TextView>(R.id.tv_current_people)
        val tvStatus = holder.itemView.findViewById<TextView>(R.id.tv_room_status)

        tvTitle.text = Util.getString(R.string.room_title) + gameRoomData.roomName

        if (gameRoomData.user2.isNullOrBlank()){
            tvPeople.text = Util.getString(R.string.current_people) + "1"
        } else {
            tvPeople.text = Util.getString(R.string.current_people) + "2"
        }

        if (gameRoomData.status == 0){
            tvStatus.text = Util.getString(R.string.waiting)
        } else if (gameRoomData.status == 2){
            tvStatus.text = Util.getString(R.string.heading_to_game_board)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
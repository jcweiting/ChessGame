package com.joyce.chessgame.multiple

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joyce.chessgame.GlobalFunction.getStringValue
import com.joyce.chessgame.R

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

        tvTitle.text = R.string.room_title.getStringValue() + gameRoomData.roomName

        if (gameRoomData.user2.isNullOrBlank()){
            tvPeople.text = R.string.current_people.getStringValue() + "1"
        } else {
            tvPeople.text = R.string.current_people.getStringValue() + "2"
        }

        if (gameRoomData.status.toInt() == 0){
            tvStatus.text = R.string.waiting.getStringValue()
        } else if (gameRoomData.status.toInt() == 2){
            tvStatus.text = R.string.heading_to_game_board.getStringValue()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
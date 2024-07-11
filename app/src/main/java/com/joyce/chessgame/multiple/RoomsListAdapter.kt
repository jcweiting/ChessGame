package com.joyce.chessgame.multiple

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.recyclerview.widget.RecyclerView
import com.joyce.chessgame.GameLog
import com.joyce.chessgame.R
import com.joyce.chessgame.Util

class RoomsListAdapter: RecyclerView.Adapter<RoomsListAdapter.ViewHolder>() {

    private var roomsArray = ArrayList<GameRoomData>()
    private var listener: OnRoomsListListener? = null

    fun setRoomsArr(roomsArr: ArrayList<GameRoomData>){
        this.roomsArray = roomsArr
    }

    fun setListener(listener: OnRoomsListListener){
        this.listener = listener
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
        val root = holder.itemView.findViewById<ConstraintLayout>(R.id.root)
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tv_room_title)
        val tvPeople = holder.itemView.findViewById<TextView>(R.id.tv_current_people)
        val tvStatus = holder.itemView.findViewById<TextView>(R.id.tv_room_status)

        tvTitle.text = Util.getString(R.string.room_title) + gameRoomData.roomName

        if (gameRoomData.player2.isNullOrBlank()){
            tvPeople.text = Util.getString(R.string.current_people) + "1"
        } else {
            tvPeople.text = Util.getString(R.string.current_people) + "2"
        }

        when (gameRoomData.status) {
            0 -> {
                tvStatus.text = Util.getString(R.string.waiting)
                root.setBackgroundResource(R.drawable.bg_rooms_active)
            }
            2 -> tvStatus.text = Util.getString(R.string.heading_to_game_board)
            4 -> tvStatus.text = Util.getString(R.string.gaming)
        }

        root.setOnClickListener {
            gameRoomData.roomId?.let {
                listener?.onClickToAddRooms(it)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    interface OnRoomsListListener{
        fun onClickToAddRooms(roomId: String)
    }

}


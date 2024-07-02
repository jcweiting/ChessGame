package com.joyce.chessgame.server

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joyce.chessgame.R
import com.joyce.chessgame.server.bean.ServerLogData

class ServerLogAdapter(private var dataList: ArrayList<ServerLogData>) :
    RecyclerView.Adapter<ServerLogAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_log_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvLog.text = dataList[position].log
        holder.tvTime.text = dataList[position].time
    }

    fun updateData(logList: java.util.ArrayList<ServerLogData>) {
        this.dataList = logList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.time)
        val tvLog: TextView = itemView.findViewById(R.id.log)
    }

}
package com.example.covidtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StateRVAdapter (private val stateList:List<StateModal>) : RecyclerView.Adapter<StateRVAdapter.StateRVViewHolder>() {

    class StateRVViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val stateNameTV:TextView = itemView.findViewById(R.id.idTVState)
        val casesTV:TextView = itemView.findViewById(R.id.idTVCases)
        val deathsTV:TextView = itemView.findViewById(R.id.idTVDeaths)
        val recoveredTV:TextView = itemView.findViewById(R.id.idTVRecovered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StateRVViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.state_rv_item, parent,false)
        return StateRVViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StateRVViewHolder, position: Int) {
        val stateData = stateList[position]
        holder.casesTV.text = stateData.cases.toString()
        holder.stateNameTV.text = stateData.state
        holder.deathsTV.text = stateData.deaths.toString()
        holder.recoveredTV.text = stateData.recovered.toString()

    }

    override fun getItemCount(): Int {
        return stateList.size
    }
}
package com.kosmo.uncrowded.model.event

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kosmo.uncrowded.databinding.EventLayoutBinding

class EventRecyclerViewAdapter(
    val context : Context,
    val items : List<EventDTO>
    ) : RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder>(){

    private var binding : EventLayoutBinding? = null

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var itemNo : TextView
        lateinit var itemName : TextView
        lateinit var itemTitle : TextView
        lateinit var itemPostDate : TextView

        var cardView: CardView

        init {
            cardView = itemView as CardView
            binding?.let {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

}
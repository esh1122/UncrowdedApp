package com.kosmo.uncrowded.model.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.EventLayoutBinding
import com.kosmo.uncrowded.view.DetailEventFragmentDirections
import com.kosmo.uncrowded.view.EventFragmentDirections
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class EventRecyclerViewAdapter(
    val fragment : Fragment,
    val items : List<EventDTO>
    ) : RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder>(){

    inner class EventViewHolder(binding: EventLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemTitle: TextView = binding.eventTitle
        val itemName: TextView = binding.eventVenuName
        val itemPoster: ImageView = binding.eventPoster
        var cardView: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val data = items[position]
        Log.i("com.kosmo.uncrowded.event","$data")
        holder.itemTitle.text = data.event_title
        holder.itemName.text = data.event_venue_name
        if(data.event_image_url.isNotEmpty()) {
            Picasso.get()
                .load(data.event_image_url)
                .error(R.drawable.ic_exclamation_50)
                .into(holder.itemPoster)
        }else{
            holder.itemPoster.setImageResource(R.drawable.ic_exclamation_50)
        }
        holder.cardView.setOnClickListener {
            val jsonString = Json.encodeToString(data)
            val action = EventFragmentDirections.actionEventFragmentToDetailEventFragment(jsonString)
            // NavController를 통해 ReceiverFragment로 이동하면서 데이터를 전송합니다.
            NavHostFragment.findNavController(fragment).navigate(action)
        }

    }

}
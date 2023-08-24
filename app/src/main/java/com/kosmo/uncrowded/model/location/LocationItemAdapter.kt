package com.kosmo.uncrowded.model.location

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.kosmo.uncrowded.databinding.LocationItemBinding
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.view.LocationFragmentDirections
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocationItemAdapter(
    val fragment : Fragment,
    val locations : List<LocationDTO>
) : RecyclerView.Adapter<LocationItemAdapter.LocationItemViewHolder>() {

    inner class LocationItemViewHolder(binding: LocationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemName: TextView = binding.locationItemName
        val itemGu: TextView = binding.locationItemGu
        val itemCrowdLvl: TextView = binding.locationItemTextCrowdLvl
        val itemImage: ImageView = binding.locationItemImage
        val itemBadge: CircleImageView = binding.locationItemCrowdLvl

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationItemViewHolder {
        val binding = LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun onBindViewHolder(holder: LocationItemViewHolder, position: Int) {
        val data = locations[position]
        holder.itemName.text = data.location_name
        holder.itemGu.text = data.gu_name
        when(data.congest_level){
            "여유" -> holder.itemBadge.setBackgroundColor(Color.GREEN)
            "보통" -> holder.itemBadge.setBackgroundColor(Color.YELLOW)
            "약간 붐빔" -> holder.itemBadge.setBackgroundColor(Color.parseColor("#ffd700"))
            "붐빔" -> holder.itemBadge.setBackgroundColor(Color.RED)
        }
        holder.itemCrowdLvl.text = data.congest_level
        Picasso.get().load(data.location_image_string).into(holder.itemImage)
        (holder.itemImage.parent as View).setOnClickListener {
            val jsonString = Json.encodeToString(data)
            val action = LocationFragmentDirections.actionLocationFragmentToDetailLocationFragment(jsonString)
            NavHostFragment.findNavController(fragment).navigate(action)
        }
    }
}
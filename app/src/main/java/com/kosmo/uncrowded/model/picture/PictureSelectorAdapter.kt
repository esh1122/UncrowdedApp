package com.kosmo.uncrowded.model.picture

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.kosmo.uncrowded.R

class PictureSelectorAdapter(
    context: Context,
    private val isGrid: Boolean,
    private val count: Int
) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val sortMenu = PictureMenuCode.values()
    override fun getCount(): Int {
        return count
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view: View? = convertView
        val data = sortMenu[position]
        if (view == null) {
            view = layoutInflater.inflate(R.layout.simple_grid_item, parent, false)

            viewHolder = ViewHolder(
                view.findViewById(R.id.text_view),
                view.findViewById(R.id.image_view)
            )
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.textView.text = data.krName
        viewHolder.imageView.setImageResource(data.drawableId)

        return view!!
    }

    data class ViewHolder(val textView: TextView, val imageView: ImageView)
}
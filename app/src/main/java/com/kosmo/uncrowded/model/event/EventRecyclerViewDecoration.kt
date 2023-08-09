package com.kosmo.uncrowded.model.event

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EventRecyclerViewDecoration(
    bottom : Int
) : RecyclerView.ItemDecoration() {

    private val bottom = bottom

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = bottom
    }
}
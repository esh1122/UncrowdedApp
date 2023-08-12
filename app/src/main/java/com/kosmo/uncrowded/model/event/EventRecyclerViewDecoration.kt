package com.kosmo.uncrowded.model.event

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EventRecyclerViewDecoration(
    private val right : Int,
    private val bottom : Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right = right
        outRect.bottom = bottom
    }
}
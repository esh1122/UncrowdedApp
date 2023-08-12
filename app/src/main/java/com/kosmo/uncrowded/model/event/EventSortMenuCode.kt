package com.kosmo.uncrowded.model.event

import com.kosmo.uncrowded.R

enum class EventSortMenuCode(val target: String, val drawableId: Int) {
    RECOMMEND("추천순",R.drawable.ic_recommend),
    RECENT("최신순",R.drawable.ic_recent),
    FREE("공짜",R.drawable.ic_free),
    CHILDREN("자녀동반",R.drawable.ic_children),
    SEAT("좌석제공",R.drawable.ic_seat);

    companion object {
        fun fromTarget(target: String): EventSortMenuCode? {
            return values().find { it.target == target }
        }
    }
}
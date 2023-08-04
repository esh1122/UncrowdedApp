package com.kosmo.uncrowded.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.adapter.EventSelectorAdapter
import com.kosmo.uncrowded.databinding.FragmentEventBinding
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.GridHolder
import com.orhanobut.dialogplus.ListHolder


class EventFragment : Fragment() {

    private lateinit var context : Context
    private var binding: FragmentEventBinding? = null
    private lateinit var dialog : DialogPlus

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","EventFragment 생성")
        binding = FragmentEventBinding.inflate(inflater,container,false)
        val adapter = EventSelectorAdapter(context, false, 2)
        dialog = DialogPlus.newDialog(context).apply {
            setContentHolder(GridHolder(1))
            isCancelable = true
            setGravity(Gravity.BOTTOM)
            setAdapter(adapter)
            setOnItemClickListener { dialog, item, view, position ->
                val textView = view.findViewById<TextView>(R.id.text_view)
            }
            setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            setContentWidth(800)
        }.create()
        return binding?.root
    }
}
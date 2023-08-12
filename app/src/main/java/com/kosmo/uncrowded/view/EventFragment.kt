package com.kosmo.uncrowded.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentEventBinding
import com.kosmo.uncrowded.model.event.EventDTO
import com.kosmo.uncrowded.model.event.EventRecyclerViewAdapter
import com.kosmo.uncrowded.model.event.EventRecyclerViewDecoration
import com.kosmo.uncrowded.model.event.EventSelectorAdapter
import com.kosmo.uncrowded.model.event.EventSortMenuCode
import com.kosmo.uncrowded.retrofit.event.EventService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.GridHolder
import kotlinx.serialization.json.Json
import nl.joery.animatedbottombar.AnimatedBottomBar
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class EventFragment : Fragment() {

    private lateinit var context : Context
    private var binding: FragmentEventBinding? = null
    private lateinit var dialog : DialogPlus
    private val spinnerItems = EventSortMenuCode.values()
            
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                // 백 버튼이 눌렸을 때의 동작 처리
//                if (handleBackPress()) return
//                isEnabled = false
//                requireActivity().onBackPressed()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","EventFragment 생성")
        binding = FragmentEventBinding.inflate(inflater,container,false)
        val adapter = EventSelectorAdapter(context, false, spinnerItems.size)
        getEvent(spinnerItems[0].name.lowercase())
        binding?.let { binding ->
            dialog = DialogPlus.newDialog(context).apply {
                setContentHolder(GridHolder(3))
                isCancelable = true
                setGravity(Gravity.BOTTOM)
                setHeader(R.layout.header_layout)
                setAdapter(adapter)
                setOnItemClickListener { dialog, item, view, position ->
                    binding.eventSpinner.text = spinnerItems[position].target
                    getEvent(spinnerItems[position].name.lowercase())
                    dialog.dismiss()
                }
                setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            }.create()

            binding.eventSpinner.text = spinnerItems[0].target
            binding.eventSpinner.setOnClickListener { v->
                dialog.show()
            }

            binding.searchEvent.setOnEditorActionListener{ v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getSearchedEvent()
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.searchEvent.windowToken, 0)
                    v.clearFocus()
                    true
                } else {
                    false
                }
            }
        }

        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val bottomBar = activity.findViewById<AnimatedBottomBar>(R.id.bottom_bar)
            bottomBar?.selectTabAt(2)
        }
    }

    private fun getEvent(requirement : String){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(EventService::class.java)
        val call = service.getEvents(requirement)
        call.enqueue(object : Callback<MutableList<EventDTO>>{
            override fun onResponse(
                call: Call<MutableList<EventDTO>>,
                response: Response<MutableList<EventDTO>>
            ) {
                val events = response.body()!!
                val adapter = EventRecyclerViewAdapter(this@EventFragment,events)
                binding?.let { binding->
                    binding.eventList.adapter = adapter
                    binding.eventList.addItemDecoration(EventRecyclerViewDecoration(0,60))
                    binding.eventList.layoutManager = LinearLayoutManager(this@EventFragment.activity, RecyclerView.VERTICAL,false)
                }
            }

            override fun onFailure(call: Call<MutableList<EventDTO>>, t: Throwable) {
                Log.i("com.kosmo.uncrowded.event","eventDto 전송 실패 ${t.message}")
            }

        })
    }

    private fun getSearchedEvent(){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        binding?.let { binding ->
            val service = retrofit.create(EventService::class.java)
            val call = service.getSearchedEvents(binding.searchEvent.text.toString())
            call.enqueue(object : Callback<MutableList<EventDTO>?> {
                override fun onResponse(
                    call: Call<MutableList<EventDTO>?>,
                    response: Response<MutableList<EventDTO>?>
                ) {
                    Log.i("event", "event:${response.body()}")
                    val events = response.body() ?: return AlertDialog.Builder(context)
                        .setTitle("이벤트를 찾을 수 없습니다").setCancelable(true).create().show()
                    val adapter = EventRecyclerViewAdapter(this@EventFragment, events)
                    binding.eventList.adapter = adapter
                    binding.eventList.addItemDecoration(EventRecyclerViewDecoration(0,60))
                    binding.eventList.layoutManager = LinearLayoutManager(
                        this@EventFragment.activity,
                        RecyclerView.VERTICAL,
                        false
                    )
                }

                override fun onFailure(call: Call<MutableList<EventDTO>?>, t: Throwable) {
                    Log.i("com.kosmo.uncrowded.event", "eventDto 전송 실패 ${t.message}")
                }
            })
        }
    }
}
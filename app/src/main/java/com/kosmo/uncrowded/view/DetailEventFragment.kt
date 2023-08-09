package com.kosmo.uncrowded.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.Event
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentDetailEventBinding
import com.kosmo.uncrowded.model.event.EventDTO
import com.kosmo.uncrowded.payment.UncrowdedEventPaymentRequestBuilder
import com.squareup.picasso.Picasso
import kotlinx.serialization.json.Json

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailEventFragment : Fragment() {

    private var binding: FragmentDetailEventBinding? = null
    val args: DetailEventFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailEventBinding.inflate(layoutInflater,container,false)
        val event = Json.decodeFromString<EventDTO>(args.event)
        Iamport.init(this)
        binding?.let {binding->
            if(event.event_image_url.isNotEmpty()) {
                Picasso.get()
                    .load(event.event_image_url)
                    .error(R.drawable.ic_exclamation_50)
                    .into(binding.eventDetailPoster)
            }else{
                binding.eventDetailPoster.setImageResource(R.drawable.ic_exclamation_50)
            }
            binding.eventDetailTitle.text=event.event_title
            binding.eventDetailDate.text="${event.event_startdate.split("T")[0]} \n~ ${event.event_enddate.split("T")[0]}"
            binding.eventTheme.text=event.event_theme
            binding.eventDetailVenueName.text=event.event_venue_name
            binding.eventUseTarget.text=event.event_use_target
            binding.eventUseFee.text=event.event_use_fee
            binding.btnKakaoPay.setOnClickListener{v->
                UncrowdedEventPaymentRequestBuilder(event,(context as MainActivity).fragmentMember).kakao().payment{
                    callBackListener.result(it)
                }
            }
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("결제가 완료되었습니다.")
                .setMessage(resJson)
                .create().show()
        }
    }

}
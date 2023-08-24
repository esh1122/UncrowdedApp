package com.kosmo.uncrowded.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentDetailEventBinding
import com.kosmo.uncrowded.model.event.EventDTO
import com.kosmo.uncrowded.payment.UncrowdedEventPaymentRequestBuilder
import com.squareup.picasso.Picasso
import kotlinx.serialization.json.Json

class DetailEventFragment : Fragment() {

    private var binding: FragmentDetailEventBinding? = null
    val args: DetailEventFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailEventBinding.inflate(layoutInflater,container,false)
        val event = Json{ignoreUnknownKeys = true}.decodeFromString<EventDTO>(args.event)
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
            (requireActivity() as MainActivity).member.observe(viewLifecycleOwner) { memberDTO ->
                binding.btnKakaoPay.setOnClickListener { v ->
                    UncrowdedEventPaymentRequestBuilder(event,memberDTO).kakao().payment {
                        callBackListener.result(it)
                    }
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
//            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("결제 완료")
                .setIcon(R.drawable.ic_logo)
                .setMessage("결제가 완료 되었습니다")
                .create().show()
        }
    }

}
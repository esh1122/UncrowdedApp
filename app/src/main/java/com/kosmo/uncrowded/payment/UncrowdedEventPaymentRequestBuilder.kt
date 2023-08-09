package com.kosmo.uncrowded.payment

import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.Iamport
import com.kosmo.uncrowded.model.event.EventDTO
import com.kosmo.uncrowded.model.MemberDTO

class UncrowdedEventPaymentRequestBuilder(eventDto: EventDTO, memberDto : MemberDTO) {

    private val eventDto = eventDto
    private val memberDto = memberDto
    private val storeCode = "imp64087601"
    private lateinit var iamPortRequest : IamPortRequest


    fun kakao() : UncrowdedEventPaymentRequestBuilder {
        iamPortRequest = IamPortRequest(
            pg = PG.kakaopay.makePgRawName(pgId = ""),           // PG 사
            pay_method = PayMethod.kakaopay.name,                // 결제수단
            name = eventDto.event_org_name,                         // 주문명
            merchant_uid = "muid_aos_${eventDto.event_code}",                 // 주문번호
            amount = "1000",                            // 결제금액
            buyer_name = memberDto.name,
            card = null, // 카드사 다이렉트
        )
        return this
    }

    fun payment(paymentResultCallback: (IamPortResponse?) -> Unit) {
        Iamport.payment(storeCode, iamPortRequest = iamPortRequest, paymentResultCallback = paymentResultCallback)
    }

}

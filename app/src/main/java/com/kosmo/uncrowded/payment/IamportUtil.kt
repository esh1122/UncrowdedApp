package com.kosmo.uncrowded.payment

import android.util.Log
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.domain.core.ICallbackPaymentResult

class IamportUtil {
    val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            Log.i("SAMPLE", "결제 결과 콜백\n$resJson")
        }
    }
}
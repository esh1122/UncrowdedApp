package com.kosmo.uncrowded

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import io.multimoon.colorful.CAppCompatActivity


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("com.kosmo.uncrowded","MainActivity생성")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tablayout = binding.tabLayout

        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.home).setText("HOME"))
        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.contact).setText("연락처"))
        tablayout.addTab(tablayout.newTab().setIcon(R.drawable.settings).setText("설정"))

        //firebase
        analytics = Firebase.analytics
        askNotificationPermission()

        Iamport.init(this)

        binding.testKakao.setOnClickListener {
            val request = IamPortRequest(
                pg = PG.kakaopay.makePgRawName(pgId = ""),           // PG 사
                pay_method = PayMethod.kakaopay.name,                // 결제수단
                name = "결제 테스트",                         // 주문명
                merchant_uid = "muid_aos_1690263354456",                 // 주문번호
                amount = "1000",                            // 결제금액
                buyer_name = "남궁안녕",
                card = null, // 카드사 다이렉트
            )
            Iamport.payment("imp64087601", iamPortRequest = request
            ) {
                callBackListener.result(it)
                Iamport.close()
            }
        }

        FirebaseApp.initializeApp(this)




        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.i("com.kosmo.uncrowded","Fetching FCM registration token failed")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
            Log.i("com.kosmo.uncrowded",token)
            Toast.makeText(this, token, Toast.LENGTH_SHORT).show()
        })



    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            Log.i("SAMPLE", "결제 결과 콜백\n$resJson")
        }
    }
}
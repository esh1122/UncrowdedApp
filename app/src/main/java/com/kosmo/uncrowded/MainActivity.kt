package com.kosmo.uncrowded

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.kakao.sdk.common.util.Utility
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import io.multimoon.colorful.CAppCompatActivity
import net.daum.mf.map.api.MapView


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("com.kosmo.uncrowded","MainActivity생성")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        analytics = Firebase.analytics
        askNotificationPermission()
        
//        //아임포트 결제 초기화
//        Iamport.init(this)
        //파이어베이스 초기화
        FirebaseApp.initializeApp(this)
        //
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.i("com.kosmo.uncrowded","Fetching FCM registration token failed")
                return@OnCompleteListener
            }
            //토큰 받아오기
            val token = task.result
            // Log and toast
            Log.i("com.kosmo.uncrowded",token)
        })

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomBar.onTabSelected = {
            when(it.title){
                "Main"->{
                    navController.navigate(R.id.action_to_mainFragment)
                }
                "Event"->{
                    navController.navigate(R.id.action_to_eventFragment)
                }
                "Location"->{
                    navController.navigate(R.id.action_to_locationFragment)
                }
            }
        }


    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {

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

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



}
//fire 베이스 구독과 구독 해지 메소드
//            binding.favoriteBtn.setOnClickListener {
//            if ((it as ToggleButton).isChecked){
//                Firebase.messaging.subscribeToTopic("location052")
//                    .addOnCompleteListener { task ->
//                        var msg = "Subscribed"
//                        if (!task.isSuccessful) {
//                            msg = "Subscribe failed"
//                        }
//                        Log.d("com.kosmo.uncrowded", msg)
//                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    }
//            }else{
//                Firebase.messaging.unsubscribeFromTopic("location052").addOnCompleteListener { task ->
//                    var msg = "Unsubscribed"
//                    if (!task.isSuccessful) {
//                        msg = "Unsubscribed failed"
//                    }
//                    Log.d("com.kosmo.uncrowded", msg)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        Log.d("com.kosmo.uncrowded", "keyhash : ${Utility.getKeyHash(this)}")
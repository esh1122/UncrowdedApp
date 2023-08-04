package com.kosmo.uncrowded

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.DragAndDropPermissions
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import io.multimoon.colorful.CAppCompatActivity


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var navController: NavController
    private var permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS)
    private var canSendNoti = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("com.kosmo.uncrowded","MainActivity생성")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        analytics = Firebase.analytics

        //권한 허용함수
        requestUserPermissions()

        //파이어베이스 초기화
        FirebaseApp.initializeApp(this)

        //파이어베이스 토큰 생성
        getFirebaseToken()

        //액션바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.mipmap.ic_launcher_foreground)
            it.setDisplayShowTitleEnabled(false)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val headerView = binding.navigationView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.close).setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }


    //firebase토큰 생성
    private fun getFirebaseToken(){
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
    }

    //버전에 따른 요청할 권한 삭제
    private fun removeUnneededPermissions(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            permissions.remove(Manifest.permission.POST_NOTIFICATIONS)
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                permissions.remove(Manifest.permission.ACCESS_FINE_LOCATION)
                permissions.remove(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    //권한 요청
    private fun requestUserPermissions(){
        val deniedPermissions = mutableListOf<String>()
        val shouldShowRequestPermissions = mutableListOf<String>()
        permissions.forEach {
            val checkPermission = ActivityCompat.checkSelfPermission(this, it) //0:권한 있다,-1:권한 없다
            if (checkPermission == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(it)//권한이 없는 경우 리스트에 저장
            }
            Log.i("com.kosmo.uncrowded","$it : ${shouldShowRequestPermissionRationale(it)}")
        }
        Log.i("com.kosmo.uncrowded","$deniedPermissions")
        if(deniedPermissions.isNotEmpty()){
            requestMultiplePermissionsLauncher.launch(deniedPermissions.toTypedArray())
        }
    }
    //
    private val requestMultiplePermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var sum = 0
        permissions.entries.forEach {
            when {
                it.key == Manifest.permission.ACCESS_FINE_LOCATION && it.value -> {
                    Log.d("com.kosmo.uncrowded", "${it.key} permission granted.")
                    sum +=1
                }
                it.key == Manifest.permission.ACCESS_COARSE_LOCATION && it.value -> {
                    Log.d("com.kosmo.uncrowded", "${it.key} permission granted.")
                    sum +=1
                }
                it.key == Manifest.permission.POST_NOTIFICATIONS && it.value -> {
                    Log.d("com.kosmo.uncrowded", "${it.key} permission granted.")
                    sum +=1
                }
                else -> {
                    Log.d("com.kosmo.uncrowded", "${it.key} denied.")
                }
            }
        }
        Log.d("com.kosmo.uncrowded","permissions.size : ${permissions.size}")
        if(sum != permissions.size) finish()
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
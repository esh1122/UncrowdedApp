package com.kosmo.uncrowded

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kakao.sdk.user.UserApiClient
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import com.kosmo.uncrowded.retrofit.login.LoginService
import com.kosmo.uncrowded.model.MemberDTO
import com.kosmo.uncrowded.view.MainFragmentDirections
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var navController: NavController
    private val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS)
    private val messagingTopics = mutableListOf<String>()


    private lateinit var member : MemberDTO

    private var isKakaoLogin = false

    private lateinit var locationManager : LocationManager


    val fragmentMember : MemberDTO
        get() = member

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("com.kosmo.uncrowded","MainActivity생성")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.getStringExtra("kakao") == resources.getString(R.string.uncrowded_key)){
            isKakaoLogin = true
            Log.i("com.kosmo.uncrowded","email 처리 : kakao")
            UserApiClient.instance.me { user, error ->
                if (user != null) {
                    val email = user.kakaoAccount!!.email!!
                    getUncrowdedUserByEmail(email){
                        member = it
                        noficationSetting(it)
                    }
                }
            }
        }else{
            Log.i("com.kosmo.uncrowded","email 처리 : preferences")
            val preferences = getSharedPreferences("usersInfo", MODE_PRIVATE)
            val email =preferences.getString("email",null)
            if (email != null) {
                getUncrowdedUserByEmail(email){
                    member = it
                    noficationSetting(it)
                }
            }
        }
        //firebase
        analytics = Firebase.analytics

        //버전에 따라 필요한 permission정리
        removeUnneededPermissions()
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

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

    }

    override fun onDestroy() {
        super.onDestroy()
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

    private fun getUncrowdedUserByEmail(email : String,password : String? = null, callback: (MemberDTO) -> Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(LoginService::class.java)
        val call =
            if (isKakaoLogin){
                service.loginFromKakao(MemberDTO(email))
            }else{
                if(password!=null){
                    service.login(MemberDTO(email,password))
                } else {
                    return
                }
            }
        call.enqueue(object : Callback<MemberDTO?>{
            override fun onResponse(call: Call<MemberDTO?>, response: Response<MemberDTO?>) {
                callback(response.body()!!)
            }
            override fun onFailure(call: Call<MemberDTO?>, t: Throwable) {
                Log.i("com.kosmo.uncrowded","멤버 데이터 끌어오기 실패 : ${t.message}")
            }
        })
    }

    private fun noficationSetting(member: MemberDTO){
        if(NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()){
            val favorites = member.favorites!!
            favorites.forEach {
                if(!messagingTopics.contains("${it.location_poi}")){
                    Firebase.messaging.subscribeToTopic("locationPOI${it.location_poi}")
                        .addOnCompleteListener { task ->
                            var msg = "Subscribed"
                            if (!task.isSuccessful) {
                                msg = "Subscribe failed"
                            }
                            messagingTopics.add("${it.location_poi}")
                            Log.d("com.kosmo.uncrowded", "$msg locationPOI${it.location_poi}")
                        }
                }else{

                }
            }
        }else {
            return
        }
    }

    companion object {
        // 위치 업데이트 간격
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 10000 // 10초
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10미터
    }

}
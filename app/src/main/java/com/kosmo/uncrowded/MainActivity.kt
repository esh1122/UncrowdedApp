package com.kosmo.uncrowded

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
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
import com.google.firebase.messaging.ktx.messaging
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kakao.sdk.user.UserApiClient
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import com.kosmo.uncrowded.model.MemberDTO
import com.kosmo.uncrowded.model.picture.PictureMenuCode
import com.kosmo.uncrowded.model.picture.PictureSelectorAdapter
import com.kosmo.uncrowded.retrofit.login.LoginService
import com.kosmo.uncrowded.retrofit.picture.CnnDialog
import com.kosmo.uncrowded.retrofit.picture.PictureService
import com.kosmo.uncrowded.retrofit.picture.ResponseCnn
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.GridHolder
import com.squareup.picasso.Picasso
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var navController: NavController
    private lateinit var dialog: DialogPlus
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

        setMember {
            Picasso.get().load("${resources.getString(R.string.login_fast_api)}profile_image?email=${it.email}")
                .into(binding.drawerLayout.findViewById<ImageView>(R.id.profile_image))
            binding.drawerLayout.findViewById<TextView>(R.id.profile_email).text = it.email.replace("@","\n@").trim()
            binding.drawerLayout.findViewById<TextView>(R.id.profile_name).text = it.name.trim()
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

        val imageActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // 이미지 처리 코드
                val imageBitmap =
                    if(data?.extras == null){
                        Log.i("Cnn","imageBitmap 갤러리")
                        val selectedImageUri= data?.data
                        val imageStream = selectedImageUri?.let { contentResolver.openInputStream(it) }
                        BitmapFactory.decodeStream(imageStream)
                    }else{
                        val extras = data.extras
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Log.i("Cnn","imageBitmap 카메라")
                            extras?.getParcelable("data", Bitmap::class.java)
                        } else {
                            Log.i("Cnn","imageBitmap null처리")
                            extras?.get("data") as? Bitmap
                        }
                    }
                imageBitmap?.let { imageBitmap->
                    getCnnImage(imageBitmap){cnn->
                        Log.i("MainActivity","cnn 호출")
                        CnnDialog(cnn).show(this.supportFragmentManager, "ConfirmDialog")
                    }
                }
            }
        }

        val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null) {
                    imageActivityResultLauncher.launch(intent)
                }
            } else {
                return@registerForActivityResult
            }
        }

        val galleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                imageActivityResultLauncher.launch(intent)
            } else {
                return@registerForActivityResult
            }
        }


        dialog = requireMethodToGetPicture{position->
            if(position == 0){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (intent.resolveActivity(packageManager) != null) {
                        imageActivityResultLauncher.launch(intent)
                    }
                }
            }else{
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    imageActivityResultLauncher.launch(intent)
                }
            }
            dialog.dismiss()
        }



        binding.navigationView.setNavigationItemSelectedListener {item->
            Log.i("MainFragment","메뉴 클릭")
            when(item.itemId){
                R.id.drawer_menu_picture->{
                    Log.i("MainFragment","0번째 메뉴 클릭")
                    dialog.show()
                }
                R.id.drawer_menu_chatbot->{

                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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
            Log.i("com.kosmo.uncrowded","token : $token")
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
                    return
                }
            }
        }else {
            return
        }
    }

    fun setMember(callback: (MemberDTO) -> Unit){
        if(intent.getStringExtra("kakao") == resources.getString(R.string.uncrowded_key)){
            isKakaoLogin = true
            Log.i("com.kosmo.uncrowded","email 처리 : kakao")
            UserApiClient.instance.me { user, error ->
                if (user != null) {
                    val email = user.kakaoAccount!!.email!!
                    getUncrowdedUserByEmail(email){
                        member = it
                        noficationSetting(it)
                        callback(it)
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
                    callback(it)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //코드로 등록한
        Log.i("MainActivity","OptionsItemSelected")
        when(item.itemId){
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)//xml설정(android:layout_gravity)과 같아야한다
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requireMethodToGetPicture(callback: (Int)->Unit): DialogPlus{
        return DialogPlus.newDialog(this).apply {
            val adapter = PictureSelectorAdapter(this@MainActivity,true,2)
            setContentHolder(GridHolder(2))
            isCancelable = true
            setGravity(Gravity.BOTTOM)
            setAdapter(adapter)
            setOnItemClickListener { dialog, item, view, position ->
                callback(position)
                dialog.dismiss()
            }
            setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        }.create()
    }

    private fun getCnnImage(bitmap: Bitmap,callback: (ResponseCnn) -> Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api))
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(PictureService::class.java)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val encodedString: String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val call = service.getCnnPeopleCount(encodedString)
        call.enqueue(object : Callback<ResponseCnn>{
            override fun onResponse(call: Call<ResponseCnn>, response: Response<ResponseCnn>) {
                if(response.isSuccessful){
                    callback(response.body()!!)
                }else{
                    Log.i("Picture","전송 데이터 오류 : ${response.body()}")
                }
            }

            override fun onFailure(call: Call<ResponseCnn>, t: Throwable) {
                Log.i("Picture","전송 오류 : ${t.message}")
            }
        })
    }

    companion object {
        // 위치 업데이트 간격
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 10000 // 10초
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10미터
    }

}
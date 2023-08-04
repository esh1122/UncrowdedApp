package com.kosmo.uncrowded.view

import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AccessTokenInfo
import com.kakao.sdk.user.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    var username: String? = null
    var password: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        //액션바 색상 변경-자바코드
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0x99FF0000));
        //자바코드로 버튼 배경 투명처리
        binding.btnLogin.getBackground().setAlpha(178)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setIcon(R.drawable.ic_lock_lock)
            .setTitle("로그인")
            .setView(R.layout.progress_layout).create()
        //버튼에 리스너 부착
        binding.btnLogin.setOnClickListener { v ->
            //프로그레스 다이얼로그 띄우기
            dialog.show()
            //사용자 입력값 받기
            username = binding.id.getText().toString().trim()
            password = binding.pwd.getText().toString().trim()
            //스프링 서버로 요청 보내기
            val retrofit = Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("http://192.168.0.10:9090").build() //스프링 REST API로 회원여부 판단을 위한 요청
            val service: LoginService = retrofit.create(LoginService::class.java)
            val call: Call<Map<String, Boolean>> =
                service.isLogin(username, password)
            call.enqueue(object :
                Callback<Map<String?, Boolean?>> {
                @SneakyThrows
                override fun onResponse(
                    call: Call<Map<String?, Boolean?>>,
                    response: Response<Map<String?, Boolean?>>
                ) {
                    if (response.isSuccessful) {
                        val result =
                            response.body()!!
                        val isLogin = result["isMember"]!!
                        if (isLogin) { //회원
                            //컨텐츠 화면(MainActivity)으로 전환
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                            val preferences = getSharedPreferences(
                                "usersInfo",
                                MODE_PRIVATE
                            )
                            preferences.edit().putString("username", username).commit()
                            preferences.edit().putString("password", password).commit()
                        } else { //비회원
                            AlertDialog.Builder(this@LoginActivity)
                                .setIcon(R.drawable.ic_lock_lock)
                                .setTitle("로그인")
                                .setMessage("아이디와 비번이 불일치합니다")
                                .setPositiveButton("확인", null).show()
                        }
                    } else {
                        try {
                            Log.i(
                                "com.kosmo.kosmoapp",
                                "응답에러:" + response.errorBody()!!.string()
                            )
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    SystemClock.sleep(2000)
                    dialog.dismiss()
                }

                override fun onFailure(
                    call: Call<Map<String?, Boolean?>>,
                    t: Throwable
                ) {
                    dialog.dismiss()
                    t.printStackTrace()
                }
            })
        }
        binding.kakaoLogin.setOnClickListener { v ->

            //토큰 존재 여부 파악( true라도 현재 사용자가 로그인 상태임을 보장하지 않습니다.
            if (AuthApiClient.getInstance().hasToken()) {
                UserApiClient.getInstance()
                    .accessTokenInfo { token: AccessTokenInfo?, error: Throwable? ->
                        if (error != null) {
                            if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
                                //로그인 필요
                                kakaoLogin()
                            } else {
                                //기타 에러
                                Log.i("com.kosmo.kosmoapp", "기타 에러 발생")
                            }
                        } else {
                            //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                            Log.i("com.kosmo.kosmoapp", "이미 로그인이 되어 있습니다")
                            //컨텐츠 화면(MainActivity)으로 전환
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                            val preferences =
                                getSharedPreferences(
                                    "usersInfo",
                                    MODE_PRIVATE
                                )
                            preferences.edit().putString("username", username).commit()
                            preferences.edit().putString("password", password).commit()
                        }
                        null
                    }
            } else {
                //로그인 필요
                kakaoLogin()
            }
        }
    } ///onCreate

    private fun kakaoLogin() {

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            UserApiClient.getInstance()
                .loginWithKakaoTalk(this@LoginActivity) { oAuthToken: OAuthToken?, error: Throwable? ->
                    if (error != null) {
                        Log.e("com.kosmo.kosmoapp", "로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk null
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.getInstance()
                            .loginWithKakaoAccount(applicationContext) { token: OAuthToken?, loginError: Throwable? ->
                                if (loginError != null) {
                                    //로그인실패
                                    Log.i("com.kosmo.kosmoapp", "로그인 실패: $loginError")
                                    AlertDialog.Builder(this@LoginActivity)
                                        .setIcon(R.drawable.ic_lock_lock)
                                        .setTitle("로그인")
                                        .setMessage("카카오계정으로 로그인 실패")
                                        .setPositiveButton("확인", null).show()
                                } else {
                                    //로그인성공
                                    //사용자정보요청
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "로그인 성공(토큰) : " + oAuthToken!!.accessToken
                                    )
                                    //컨텐츠 화면(MainActivity)으로 전환
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                                    val preferences =
                                        getSharedPreferences(
                                            "usersInfo",
                                            MODE_PRIVATE
                                        )
                                    preferences.edit().putString("username", username).commit()
                                    preferences.edit().putString("password", password).commit()
                                }
                                null
                            }
                    } else if (oAuthToken != null) {
                        Log.i(
                            "com.kosmo.kosmoapp",
                            "로그인 성공(토큰) : " + oAuthToken.accessToken
                        )
                        UserApiClient.getInstance()
                            .me { (id, _, kakaoAccount): User, meError: Throwable? ->
                                if (meError != null) {
                                    Log.e(
                                        "com.example.kakaologin",
                                        "사용자 정보 요청 실패",
                                        meError
                                    )
                                    AlertDialog.Builder(this@LoginActivity)
                                        .setIcon(R.drawable.ic_lock_lock)
                                        .setTitle("로그인")
                                        .setMessage("카카오계정으로 로그인 실패")
                                        .setPositiveButton("확인", null).show()
                                } else {
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "정보요청 성공(getId()):$id"
                                    )
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "user.getKakaoAccount():$kakaoAccount"
                                    )
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "이메일:" + kakaoAccount!!.email
                                    )
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "닉네임:" + kakaoAccount.profile!!.nickname
                                    )
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "프로필사진(640x640):" + kakaoAccount.profile!!.thumbnailImageUrl
                                    )
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "프로필사진(110X110):" + kakaoAccount.profile!!.thumbnailImageUrl
                                    )

                                    //컨텐츠 화면(MainActivity)으로 전환
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                                    val preferences =
                                        getSharedPreferences(
                                            "usersInfo",
                                            MODE_PRIVATE
                                        )
                                    preferences.edit().putString("username", username).commit()
                                    preferences.edit().putString("password", password).commit()
                                }
                                null
                            }
                    }
                    null
                }
        } else {
            // 카카오계정으로 로그인 공통 callback 구성
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
            UserApiClient.getInstance()
                .loginWithKakaoAccount(this) { oAuthToken: OAuthToken?, loginError: Throwable? ->
                    if (loginError != null) {
                        //로그인실패
                        Log.i("com.kosmo.kosmoapp", "로그인 실패: $loginError")
                    } else {
                        //로그인성공
                        //사용자정보요청
                        // Log.i("com.example.kakaologin", "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                    }
                    null
                }
        }
    } ///////////////////

    companion object {
        fun getKeyHash(context: Context): String? {
            val pm = context.packageManager
            try {
                val packageInfo =
                    pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                        ?: return null
                for (signature in packageInfo.signatures) {
                    try {
                        val md = MessageDigest.getInstance("SHA")
                        md.update(signature.toByteArray())
                        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                    } catch (e: NoSuchAlgorithmException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
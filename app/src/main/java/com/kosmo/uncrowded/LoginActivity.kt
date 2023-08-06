package com.kosmo.uncrowded

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AccessTokenInfo
import com.kakao.sdk.user.model.User
import com.kakao.util.maps.helper.Utility
import com.kosmo.uncrowded.databinding.ActivityLoginBinding
import com.kosmo.uncrowded.login.service.LoginService
import com.kosmo.uncrowded.model.MemberDTO
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var email: String = ""
    var password: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_lock_lock)
            .setTitle("로그인")
            .setView(R.layout.progress_layout).create()

        Log.d("com.kosmo.uncrowded","${Utility.getKeyHash(this)}")
        //버튼에 리스너 부착
        binding.btnUncrowdedLogin.setOnClickListener { v ->
            //프로그레스 다이얼로그 띄우기
            dialog.show()
            //사용자 입력값 받기
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()
            //스프링 서버로 요청 보내기
            val retrofit = Retrofit.Builder()
                .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
                .baseUrl(R.string.login_api_address_uncrowded.toString()).build() //스프링 REST API로 회원여부 판단을 위한 요청
            val service: LoginService = retrofit.create(LoginService::class.java)
            val call = service.login(MemberDTO(email, password))
            call.enqueue(object : Callback<MemberDTO?> {
                override fun onResponse(
                    call: Call<MemberDTO?>,
                    response: Response<MemberDTO?>
                ) {
                    if (response.isSuccessful) {
                        val member = response.body()
                        if (member != null) { //회원
                            //컨텐츠 화면(MainActivity)으로 전환
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                            saveEmailAndPassword(email, password)
                        } else { //비회원
                            AlertDialog.Builder(this@LoginActivity)
                                .setIcon(android.R.drawable.ic_lock_lock)
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
                    call: Call<MemberDTO?>,
                    t: Throwable
                ) {
                    dialog.dismiss()
                    t.printStackTrace()
                }
            })
        }
        binding.btnKakaoLogin.setOnClickListener { v ->
            //토큰 존재 여부 파악( true라도 현재 사용자가 로그인 상태임을 보장하지 않습니다.
            if (AuthApiClient.instance.hasToken()) {
                UserApiClient.instance
                    .accessTokenInfo { token: AccessTokenInfo?, error: Throwable? ->
                        if (error != null) {
                            if (error is KakaoSdkError && error.isInvalidTokenError()) {
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
                                Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    putExtra("kakao",resources.getString(R.string.uncrowded_key))
                                }
                            startActivity(intent)
                            //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                            UserApiClient.instance.me { user, error ->
                                saveEmailAndPassword(user!!.kakaoAccount!!.email!!, resources.getString(R.string.uncrowded_key))
                            }
                        }
                    }
            } else {
                //로그인 필요
                kakaoLogin()
            }
        }
    } ///onCreate

    private fun kakaoLogin() {

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance
                .loginWithKakaoTalk(this@LoginActivity) { oAuthToken: OAuthToken?, error: Throwable? ->
                    if (error != null) {
                        Log.e("com.kosmo.kosmoapp", "로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance
                            .loginWithKakaoAccount(applicationContext) { token: OAuthToken?, loginError: Throwable? ->
                                if (loginError != null) {
                                    //로그인실패
                                    Log.i("com.kosmo.kosmoapp", "로그인 실패: $loginError")
                                    AlertDialog.Builder(this@LoginActivity)
                                        .setIcon(android.R.drawable.ic_lock_lock)
                                        .setTitle("로그인")
                                        .setMessage("카카오계정으로 로그인 실패")
                                        .setPositiveButton("확인", null).show()
                                } else {
                                    //로그인성공
                                    //사용자정보요청
                                    Log.i(
                                        "com.kosmo.kosmoapp",
                                        "로그인 성공(토큰) : " + token!!.accessToken
                                    )
                                    //컨텐츠 화면(MainActivity)으로 전환
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java).apply {
                                            putExtra("kakao",resources.getString(R.string.uncrowded_key))
                                        }
                                    startActivity(intent)
                                    //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                                    saveEmailAndPassword(email, password)
                                }
                            }
                    } else if (oAuthToken != null) {
                        Log.i(
                            "com.kosmo.kosmoapp",
                            "로그인 성공(토큰) : " + oAuthToken.accessToken
                        )
                        UserApiClient.instance
                            .me { user: User?, meError: Throwable? ->
                                if (meError != null) {
                                    AlertDialog.Builder(this@LoginActivity)
                                        .setIcon(android.R.drawable.ic_lock_lock)
                                        .setTitle("로그인")
                                        .setMessage("카카오계정으로 로그인 실패")
                                        .setPositiveButton("확인", null).show()
                                } else {
                                    //컨텐츠 화면(MainActivity)으로 전환
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java).apply{
                                            putExtra("kakao",resources.getString(R.string.uncrowded_key))
                                        }
                                    startActivity(intent)
                                    //다른 화면에서 로그인 여부 판단을 위한 아이디 저장
                                    saveEmailAndPassword(email, password)
                                }
                            }
                    }
                }
        } else {
            // 카카오계정으로 로그인 공통 callback 구성
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
            UserApiClient.instance
                .loginWithKakaoAccount(this) { _: OAuthToken?, loginError: Throwable? ->
                    if (loginError != null) {
                        //로그인실패
                        Log.i("com.kosmo.kosmoapp", "로그인 실패: $loginError")
                    } else {
                        //로그인성공
                        //사용자정보요청
                        // Log.i("com.example.kakaologin", "로그인 성공(토큰) : " + oAuthToken.getAccessToken());

                    }
                }
        }
    } ///////////////////

    private fun saveEmailAndPassword(email: String,password: String){
        val preferences =
            getSharedPreferences(
                "usersInfo",
                MODE_PRIVATE
            )
        preferences.edit().putString("email", email).apply()
        preferences.edit().putString("password", password).apply()

    }

}
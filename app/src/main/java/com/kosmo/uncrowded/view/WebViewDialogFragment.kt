package com.kosmo.uncrowded.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentWebviewDialogBinding
import com.kosmo.uncrowded.retrofit.login.LoginService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class WebViewDialogFragment : DialogFragment() {
    private var binding : FragmentWebviewDialogBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebviewDialogBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { binding->
            val webView = binding.webView
            webView.webViewClient = WebViewClient() // 웹뷰 내부에서 URL을 열기 위함
            webView.webChromeClient = object : WebChromeClient(){
                override fun onCloseWindow(window: WebView?) {
                    super.onCloseWindow(window)
                    this@WebViewDialogFragment.dismiss()
                }
            }
            webView.settings.javaScriptEnabled=true
            getUncrowdedToken{
                val cookieManager= CookieManager.getInstance()
                cookieManager.setAcceptCookie(true) // 쿠키 허용


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.setAcceptThirdPartyCookies(webView, true) // 서드파티 쿠키 허용
                }

                val cookieString = "${resources.getString(R.string.token)}=$it;"
                cookieManager.setCookie("http://192.168.0.33:9090/approach/Chat", cookieString)
                webView.loadUrl("http://192.168.0.33:9090/approach/Chat")
            }
        }
    }

    private fun getUncrowdedToken(callback:(String?)->Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
        val service = retrofit.create(LoginService::class.java)
        val call = service.getUncrowdedToken((requireActivity() as MainActivity).fragmentMember)
        call.enqueue(object : Callback<Map<String,String>>{
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                response.body()?.let {
                    val token = it[resources.getString(R.string.token)]
                    callback(token)
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.i("UncrowdedToken","전송 실패 : ${t.message}")
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}

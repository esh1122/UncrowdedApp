package com.kosmo.uncrowded

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful

class UncrowdedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaults  = Defaults(
            primaryColor = ThemeColor.BLACK,
            accentColor = ThemeColor.WHITE,
            useDarkTheme = false,
            translucent = false)
        initColorful(this, defaults)
        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, resources.getString(R.string.kakao_native_key))
        Log.i("com.kosmo.uncrowded","Application생성")
    }
}
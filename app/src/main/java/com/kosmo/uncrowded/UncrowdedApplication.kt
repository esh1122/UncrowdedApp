package com.kosmo.uncrowded

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.util.Log
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful

class UncrowdedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaults : Defaults = Defaults(
            primaryColor = ThemeColor.BLACK,
            accentColor = ThemeColor.WHITE,
            useDarkTheme = false,
            translucent = false)
        initColorful(this, defaults)
        Log.i("com.kosmo.uncrowded","현재 시각 : ${System.currentTimeMillis()}")
    }
}
package com.kosmo.uncrowded

import android.os.Bundle
import android.util.Log
import com.kosmo.uncrowded.databinding.ActivityMainBinding
import io.multimoon.colorful.CAppCompatActivity


class MainActivity : CAppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root);
        Log.i("com.kosmo.uncrowded","현재 시각 : ${System.currentTimeMillis()}")
    }
}
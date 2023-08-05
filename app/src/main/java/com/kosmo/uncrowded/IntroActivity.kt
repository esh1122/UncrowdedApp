package com.kosmo.uncrowded

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kosmo.uncrowded.databinding.ActivityIntroBinding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //2초 지연후 화면전환
        //스레드 실행을 위한 ScheduledExecutorService객체 생성
        val worker = Executors.newSingleThreadScheduledExecutor()
        //스레드 정의
        val runnable = Runnable {
            val intent = Intent(this@IntroActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) //매니페스트에 android:noHistory="true"설정과 같다
            startActivity(intent)
        }
        //3초후에 스레드 실행
        val delay = 3
        worker.schedule(runnable, delay.toLong(), TimeUnit.SECONDS)
    }

    override fun onResume() {
        super.onResume()
        showRotateUntilMilliSecond(binding.loading ,3000)
    }

    private fun showRotateUntilMilliSecond(imageView : View, milliSecond : Int){
        val animator = ObjectAnimator.ofFloat(imageView,"rotation",0f, 720f)
        animator.duration = milliSecond.toLong()
        animator.start()
    }


}
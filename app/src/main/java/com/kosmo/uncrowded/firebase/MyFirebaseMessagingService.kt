package com.kosmo.uncrowded.firebase

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kosmo.uncrowded.MainActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("com.kosmo.uncrowded.firebase", "Refreshed token: $token")
//        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)

        //super.onMessageReceived(message);
        //푸쉬 메시지를 저장하는 맵
        val pushMessage: MutableMap<String, String?> = HashMap()
        Log.i("com.kosmo.fcmmessaging", "From:" + remoteMessage.from)
        var map = mutableMapOf<String,String>()
        if (remoteMessage.data.isNotEmpty()) { //데이타 메시지를 받은 경우
            //※※※사용자 웹(우리가 만든 웹 앱) UI 폼 하위요소의 파라미터명이 키 이다
            //파이어베이스 웹 UI는 데이타(선택사항)의 키 입력값이 키이다.
            map = remoteMessage.data
        } ////////////////
        map["title"] = "${map["location_name"]} 혼잡"
        map["body"] = "고객님이 구독하신 ${map["location_name"]} 지역이 혼잡합니다. 조심하시길 바랍니다."
        //상태바에 알림을 표시하기 위한 메소드 호출

        //상태바에 알림을 표시하기 위한 메소드 호출
        showNotification(map)
    }

    private fun showNotification(map: Map<String, String>) {
        val jsonMap = Json.encodeToString(map)
        val uri = Uri.parse("uncrowded://detail.location.com/$jsonMap") // 예: Uri.parse("http://www.example.com")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        //인텐트에 부가 정보 저장("title","body")키는 필수고 나머지키는 사용자 정의 키이다)
        //화면전환을 인텐트의 플래그 설정
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        //펜딩 인텐트로 설정
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder: NotificationCompat.Builder = createNotificationCompatBuilder(
            map["title"]!!,
            map["body"]!!
        )

        //실행할 펜딩 인텐트 설정
        builder.setContentIntent(pendingIntent)
        //Notification객체 생성
        val notification = builder.build()
        //통지하기
        //NotificationManager의 notify()메소드로 Notification객체 등록
        //notify(Notification을 구분하기 위한 구분자,Notification객체);
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) { //버전체크 코드
            //오레오 부터 아래 코드 추가해야 함 시작
            val notificationChannel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.enableLights(true) //스마트폰에 노티가 도착했을때 빛을 표시할지 안할지 설정
            notificationChannel.lightColor = Color.RED //위 true설정시 빛의 색상
            notificationChannel.enableVibration(true) //노티 도착시 진동 설정
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100) //진동 시간(1000분의 1초)
            //오레오 부터 아래 코드 추가해야 함 끝
            //노피케이션 매니저와 연결
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(1, notification)
    } //////////////////showNotification

    private fun createNotificationCompatBuilder(
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_dialog_email) //노티 도착시 상태바에 표시되는 아이콘
            .setContentTitle(title) //노티 드래그시 보이는 제목
            .setContentText(content) //노티 드래그시 보이는 내용
            .setAutoCancel(true) //노티 드래그후 클릭시 상태바에서 자동으로 사라지도록 설정
            .setDefaults(Notification.DEFAULT_VIBRATE) //노티시 알림 방법
    }


}
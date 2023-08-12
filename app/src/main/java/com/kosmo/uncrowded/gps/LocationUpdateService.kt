import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build

import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.kosmo.uncrowded.R

class LocationUpdateService(
    val locationManager : LocationManager
) : Service() {
    private val TAG = "LocationUpdateService"
    private val LOCATION_UPDATE_INTERVAL: Long = 30000 // 30 seconds

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example Channel"
            val descriptionText = "Example Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let { location ->
                    // 위치 정보를 이용하여 특정 위치를 체크하고, 노티피케이션을 발신하는 로직을 구현
                    checkLocationAndSendNotification(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun requestLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            Log.e(TAG, "Error requesting location updates: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationAndSendNotification(location: Location) {
        // 특정 위치를 체크하고, 노티피케이션을 발신하는 로직을 구현합니다.
        // 예를 들면, location.latitude와 location.longitude를 사용하여 특정 위치와 비교합니다.
        // 특정 위치에 도달하면 노티피케이션을 생성하고 노출합니다.
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_logo)  // 알림 아이콘
            .setContentTitle("현재 위치는 위험한 위치입니다")             // 알림 제목
            .setContentText("This is a sample notification.") // 알림 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 알림 우선 순위

        // 알림을 표시합니다.
//        with(NotificationManagerCompat.from(this)) {
//            notify(notificationId, builder.build())
//        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
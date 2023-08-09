import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager

import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*

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

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
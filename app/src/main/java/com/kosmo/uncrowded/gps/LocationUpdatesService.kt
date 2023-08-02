package com.kosmo.uncrowded.gps

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.kosmo.uncrowded.R

class LocationUpdatesService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()

        // Create the Foreground Service notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Updates")
            .setSmallIcon(R.drawable.ic_logo)
            .build()

        // Start the Foreground Service
        startForeground(NOTIFICATION_ID, notification)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create LocationRequest
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Start location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        Log.i("com.kosmo.uncrowded","LastLocation : ${locationResult.lastLocation}")
                        Log.i("com.kosmo.uncrowded","Locations : ${locationResult.locations}")
                    }
                },
                Looper.getMainLooper())
            return
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val CHANNEL_ID = "LocationUpdatesChannel"
        const val NOTIFICATION_ID = 123
    }
}

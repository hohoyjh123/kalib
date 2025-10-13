package com.yesjnet.gwanak.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.orhanobut.logger.Logger
import org.koin.core.component.KoinComponent

class GpsUtil private constructor() : KoinComponent {

    private object AnalyticsUtilHolder {
        val INSTANCE = GpsUtil()
    }

    private lateinit var context: Context
    private lateinit var mSettingsClient: SettingsClient
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var locationManager: LocationManager
    private lateinit var locationRequest: LocationRequest

    fun init(context: Context) {
        this.context = context
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mSettingsClient = LocationServices.getSettingsClient(context)
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval((10 * 1000).toLong())
        locationRequest.setFastestInterval((2 * 1000).toLong())
        mLocationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()
    }

    fun isEnabledGPS(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun requestTurnOnGPS() {
        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnFailureListener((OnFailureListener { e: Exception ->
                val statusCode = (e as ApiException).statusCode
                Logger.d("GpsUtil onFailure statusCode = $statusCode")
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult((context as Activity)!!, GpsUtil.GPS_REQUEST)
                    } catch (sie: SendIntentException) {
                        Logger.d("GpsUtil PendingIntent unable to execute request.")
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "GpsUtil Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                        Logger.e(errorMessage)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } as OnFailureListener)!!)
    }


    companion object {
        val instance: GpsUtil
            get() = AnalyticsUtilHolder.INSTANCE

        const val GPS_REQUEST = 119
    }

}
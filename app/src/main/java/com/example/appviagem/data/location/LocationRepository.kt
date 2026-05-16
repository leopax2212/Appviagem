package com.example.appviagem.data.location

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LocationRepository(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5_000L // intervalo em ms
    ).apply {
        setMinUpdateDistanceMeters(10f)
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setWaitForAccurateLocation(true)
    }.build()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getCityFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        var result: String? = null
        val latch = CountDownLatch(1)

        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
            result = addresses.firstOrNull()?.locality
                ?: addresses.firstOrNull()?.subAdminArea
                        ?: addresses.firstOrNull()?.adminArea
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        result
    }

    // Versao para APIs mais antigas (< 33)
    @Suppress("DEPRECATION")
    private suspend fun getCityFromCoordinatesLegacy(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: addresses?.firstOrNull()?.adminArea
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCityFromLocation(latitude: Double, longitude: Double): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getCityFromCoordinates(latitude, longitude)
        } else {
            getCityFromCoordinatesLegacy(latitude, longitude)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun locationWithCityFlow(): Flow<LocationInfo> =
        locationFlow()
            .map { location ->
                val city = getCityFromLocation(location.latitude, location.longitude)
                LocationInfo(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    city = city,
                    state = null,
                    country = null
                )
            }
            .distinctUntilChangedBy { it.city }

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun locationFlow(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }.flowOn(Dispatchers.IO)
}
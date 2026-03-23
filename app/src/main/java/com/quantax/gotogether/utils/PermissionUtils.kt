package com.quantax.gotogether.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

object PermissionUtils {

    @Composable
    fun rememberLocationPermissionLauncher(
        onPermissionResult: (Boolean) -> Unit
    ): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
        return rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            onPermissionResult(granted)
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun fetchLocation(
        context: Context,
        onLocationDetailsFetched: (String, String?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Permission not granted")
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    reverseGeocode(context, location.latitude, location.longitude, onLocationDetailsFetched, onError)
                } else {
                    onError("Unable to get current location")
                }
            }.addOnFailureListener {
                onError(it.message ?: "Location error")
            }
        } catch (e: SecurityException) {
            onError("Security exception: ${e.message}")
        }
    }

    private fun reverseGeocode(
        context: Context,
        lat: Double,
        lon: Double,
        onLocationDetailsFetched: (String, String?) -> Unit,
        onError: (String) -> Unit
    ) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val formatted = formatAddress(address?.locality, address?.countryName)
                        val locality = address?.locality
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            onLocationDetailsFetched(formatted, locality)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                    val formatted = formatAddress(address?.locality, address?.countryName)
                    val locality = address?.locality
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onLocationDetailsFetched(formatted, locality)
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Geocoder error: ${e.message}")
                }
            }
        }
    }

    private fun formatAddress(city: String?, country: String?): String {
        return when {
            city != null && country != null -> "$city, $country"
            city != null -> city
            country != null -> country
            else -> "Somewhere on Earth"
        }
    }
}
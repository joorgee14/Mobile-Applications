package com.example.myapplication.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.content.Context

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.ui.zIndex

import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.myapplication.BuildConfig
import com.example.myapplication.R

@Composable
fun GymsMapScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            hasLocationPermission = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // MapView
        AndroidView(
            modifier = Modifier
                .matchParentSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(Bundle())
                    onResume()
                    getMapAsync { map ->
                        googleMap = map

                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true
                        map.uiSettings.isZoomControlsEnabled = false

                        if (hasLocationPermission) {
                            map.isMyLocationEnabled = true

                            val locationRequest = LocationRequest.create().apply {
                                interval = 10000
                                fastestInterval = 5000
                                priority = Priority.PRIORITY_HIGH_ACCURACY
                                maxWaitTime = 15000
                            }

                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                object : LocationCallback() {
                                    override fun onLocationResult(result: LocationResult) {
                                        val location = result.lastLocation
                                        location?.let {
                                            val userLatLng = LatLng(it.latitude, it.longitude)
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                                            map.addMarker(
                                                MarkerOptions()
                                                    .position(userLatLng)
                                                    .title(context.getString(R.string.you_are_here))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                            )
                                            fusedLocationClient.removeLocationUpdates(this)

                                            fetchNearbyGyms(ctx, map, userLatLng)
                                        }
                                    }
                                },
                                ctx.mainLooper
                            )
                        }
                    }
                }.also { mapView = it }
            }
        )

        // Back button - always visible on top
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = context.getString(R.string.back),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        // AI PROMPT: Give me the code to add zoom buttons
        // Zoom buttons - also on top
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 64.dp)
                .zIndex(1f)
        ) {
            Button(onClick = {
                googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
            }) {
                Text(context.getString(R.string.zoom_in))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
            }) {
                Text(context.getString(R.string.zoom_out))
            }
        }
    }
}

fun fetchNearbyGyms(context: Context, map: GoogleMap, userLatLng: LatLng) {
    val apiKey = BuildConfig.MAPS_API_KEY
    val location = "${userLatLng.latitude},${userLatLng.longitude}"
    val radius = 3000
    val type = "gym"
    val url =
        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$location&radius=$radius&type=$type&key=$apiKey"

    Thread {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val json = org.json.JSONObject(response)
            val results = json.getJSONArray("results")

            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val name = result.getString("name")
                val geometry = result.getJSONObject("geometry")
                val locationObj = geometry.getJSONObject("location")
                val lat = locationObj.getDouble("lat")
                val lng = locationObj.getDouble("lng")

                val gymLatLng = LatLng(lat, lng)

                (context as android.app.Activity).runOnUiThread {
                    map.addMarker(
                        MarkerOptions()
                            .position(gymLatLng)
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}

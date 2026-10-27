package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.data.model.RoutePoint
import com.example.data.model.RouteTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class LocationTracker(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _currentPoint = MutableStateFlow<RoutePoint?>(null)
    val currentPoint: StateFlow<RoutePoint?> = _currentPoint.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _activeRoutePoints = MutableStateFlow<List<RoutePoint>>(emptyList())
    val activeRoutePoints: StateFlow<List<RoutePoint>> = _activeRoutePoints.asStateFlow()

    private val _savedRoutes = MutableStateFlow<List<RouteTrack>>(emptyList())
    val savedRoutes: StateFlow<List<RouteTrack>> = _savedRoutes.asStateFlow()

    private var trackingStartTime: Long = 0L

    init {
        // Seed default sample route (e.g. scenic city tour)
        seedSampleRoutes()
    }

    private fun seedSampleRoutes() {
        val baseLat = 28.6139 // New Delhi coordinates
        val baseLng = 77.2090
        val samplePoints = listOf(
            RoutePoint(baseLat, baseLng, 215.0, 12.5f, System.currentTimeMillis() - 7200000),
            RoutePoint(baseLat + 0.005, baseLng + 0.004, 218.0, 18.2f, System.currentTimeMillis() - 6000000),
            RoutePoint(baseLat + 0.012, baseLng + 0.008, 220.0, 22.0f, System.currentTimeMillis() - 4800000),
            RoutePoint(baseLat + 0.018, baseLng + 0.015, 217.0, 15.5f, System.currentTimeMillis() - 3600000),
            RoutePoint(baseLat + 0.024, baseLng + 0.022, 225.0, 10.0f, System.currentTimeMillis() - 2400000)
        )
        val sampleTrack = RouteTrack(
            id = "track_1",
            name = "Connaught Place to India Gate Trail",
            startTime = System.currentTimeMillis() - 7200000,
            endTime = System.currentTimeMillis() - 2400000,
            points = samplePoints,
            totalDistanceMeters = 3450.0,
            averageSpeedKmh = 15.6
        )
        _savedRoutes.value = listOf(sampleTrack)
        _currentPoint.value = RoutePoint(baseLat, baseLng, 215.0, 0f)
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (hasGps) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, this)
            } else if (hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 2f, this)
            }

            // Also check last known
            val lastLoc = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                null
            }

            if (lastLoc != null) {
                val pt = RoutePoint(lastLoc.latitude, lastLoc.longitude, lastLoc.altitude, lastLoc.speed)
                _currentPoint.value = pt
                _activeRoutePoints.value = listOf(pt)
            }

            trackingStartTime = System.currentTimeMillis()
            _isTracking.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide simulated GPS update if permissions are missing or emulator without GPS fix
            simulateGpsMove()
            trackingStartTime = System.currentTimeMillis()
            _isTracking.value = true
        }
    }

    fun stopTracking(routeName: String = "Recorded Route") {
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isTracking.value = false
        val pts = _activeRoutePoints.value
        if (pts.size >= 2) {
            val dist = calculateTotalDistance(pts)
            val durHours = ((System.currentTimeMillis() - trackingStartTime) / 1000.0 / 3600.0).coerceAtLeast(0.001)
            val avgSpeed = (dist / 1000.0) / durHours
            val track = RouteTrack(
                id = "track_${System.currentTimeMillis()}",
                name = routeName,
                startTime = trackingStartTime,
                endTime = System.currentTimeMillis(),
                points = pts,
                totalDistanceMeters = dist,
                averageSpeedKmh = avgSpeed
            )
            _savedRoutes.value = listOf(track) + _savedRoutes.value
        }
        _activeRoutePoints.value = emptyList()
    }

    fun simulateGpsMove() {
        val curr = _currentPoint.value ?: RoutePoint(28.6139, 77.2090)
        val nextLat = curr.latitude + (Math.random() - 0.48) * 0.002
        val nextLng = curr.longitude + (Math.random() - 0.48) * 0.002
        val newPoint = RoutePoint(nextLat, nextLng, 215.0 + Math.random() * 5, (10 + Math.random() * 15).toFloat())
        _currentPoint.value = newPoint
        if (_isTracking.value) {
            _activeRoutePoints.value = _activeRoutePoints.value + newPoint
        }
    }

    override fun onLocationChanged(location: Location) {
        val pt = RoutePoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            speed = location.speed,
            timestamp = location.time
        )
        _currentPoint.value = pt
        if (_isTracking.value) {
            _activeRoutePoints.value = _activeRoutePoints.value + pt
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun calculateTotalDistance(points: List<RoutePoint>): Double {
        var distance = 0.0
        for (i in 0 until points.size - 1) {
            distance += haversineMeters(points[i], points[i + 1])
        }
        return distance
    }

    private fun haversineMeters(p1: RoutePoint, p2: RoutePoint): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

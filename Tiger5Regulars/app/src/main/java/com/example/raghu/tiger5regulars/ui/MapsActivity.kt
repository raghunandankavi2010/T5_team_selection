package com.example.raghu.tiger5regulars.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.raghu.tiger5regulars.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationListener: LocationListener? = null
    private var mMarkerOptions: MarkerOptions? = null
    private var mOrigin: LatLng? = null
    private var mDestination: LatLng = LatLng(12.874440,77.538552)
    private var mPolyline: Polyline? = null
    private lateinit var model: MapsActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        model = ViewModelProviders.of(this)[MapsActivityViewModel::class.java]
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        myLocation
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 100) {
            if (!verifyAllPermissions(grantResults)) {
                Toast.makeText(applicationContext, "No sufficient permissions", Toast.LENGTH_LONG).show()
            } else {
                myLocation
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun verifyAllPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // Getting LocationManager object from System Service LOCATION_SERVICE
    private val myLocation: Unit
        get() { // Getting LocationManager object from System Service LOCATION_SERVICE
            mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    mOrigin = LatLng(location.latitude, location.longitude)
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mOrigin, 12f))
                    if (mOrigin != null && mDestination != null) drawRoute()
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            val currentApiVersion = Build.VERSION.SDK_INT
            if (currentApiVersion >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                    mMap!!.isMyLocationEnabled = true
                    mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, mLocationListener)
                    mMap!!.setOnMapLongClickListener { latLng ->
                        mMap!!.clear()
                        mMarkerOptions = MarkerOptions().position(mDestination!!).title("Tiger5")
                        mMap!!.addMarker(mMarkerOptions)
                        if (mOrigin != null) drawRoute()
                    }
                } else {
                    requestPermissions(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ), 100)
                }
            }
        }

    private fun drawRoute() { // Getting URL to the Google Directions API
        val url = getDirectionsUrl(mOrigin, mDestination)
        model.download(url)
        model.data.observe(this, Observer<List<List<HashMap<String, String>>>> { value ->
            value?.let {
                var points: ArrayList<LatLng?>? = null
                var lineOptions: PolylineOptions? = null
                // Traversing through all the routes
                for (i in value.indices) {
                    points = ArrayList()
                    lineOptions = PolylineOptions()
                    // Fetching i-th route
                    val path = value[i]
                    // Fetching all the points in i-th route
                    for (j in path.indices) {
                        val point = path[j]
                        val lat = point["lat"]!!.toDouble()
                        val lng = point["lng"]!!.toDouble()
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points)
                    lineOptions.width(8f)
                    lineOptions.color(Color.RED)
                }
                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {
                    if (mPolyline != null) {
                        mPolyline!!.remove()
                    }
                    mPolyline = mMap!!.addPolyline(lineOptions)
                } else Toast.makeText(applicationContext, "No route is found", Toast.LENGTH_LONG).show()}
        })

    }

    private fun getDirectionsUrl(origin: LatLng?, dest: LatLng?): String { // Origin of route
        val strOrigin = "origin=" + origin!!.latitude + "," + origin.longitude
        // Destination of route
        val strDest = "destination=" + dest!!.latitude + "," + dest.longitude
        // Key
        val key = "key=" + getString(R.string.google_maps_key)
        // Building the parameters to the web service
        val parameters = "$strOrigin&$strDest&$key"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }
}
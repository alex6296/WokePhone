package com.example.wokephone

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.wokephone.utilz.JsonReader
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //vars:
    private var bikePoints: List<LocationPoint>? = null

    //layout elements
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var mapTextView: TextView
    private lateinit var bikeDataButton : Button
    //location utilities
    private var LOCATION_REFRESH_TIME: Long = 1000
    private var LOCATION_REFRESH_DISTANCE: Float = 1000F
    private lateinit var mLocationManager: LocationManager
    private val mLocationListener: LocationListener = LocationListener {
        currentLocation = it
        //CODE ON LOCATION UPDATE HERE
        val text = "Lati: " + it.latitude + "\nLong: " + it.longitude
        mapTextView.text = text

    }
    //Utilz
    private val jsonReader = JsonReader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //load file
        bikePoints = jsonReader.loadFile(this, "biking.json")

        //Layout
        mapTextView = findViewById(R.id.MaptextField)
        bikeDataButton = findViewById(R.id.bikeData)
        bikeDataButton.setOnClickListener {
            addBikeRoute()
        }

        //permission checker
        Dexter.withContext(this@MapsActivity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

                    //check valid permissions again because android is stupid
                    if (ActivityCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        //start location updates
                        mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, mLocationListener
                        )
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    //TODO
                }
            }).check()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }

    fun addBikeRoute(){
        val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
        if (bikePoints != null) {
            for (z in 0 until bikePoints!!.size) {
                val point: LatLng = bikePoints!![z].toLatLng()
                options.add(point)
            }
            val line = mMap.addPolyline(options)

            val firstL = bikePoints!![0].toLatLng()
            val midL = bikePoints!![bikePoints!!.size/2].toLatLng()
            val lastL = bikePoints!![bikePoints!!.size-1].toLatLng()
            mMap.addMarker(MarkerOptions().position(firstL).title("Start"))
            mMap.addMarker(MarkerOptions().position(lastL).title("End"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midL,10.5f))
        }
    }
}
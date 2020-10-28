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
import kotlin.Comparator
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //vars:
    private var bikePoints: List<LocationPoint>? = null

    //layout elements
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var mapTextView: TextView
    //buttons
    private lateinit var rawButton : Button
    private lateinit var meanButton : Button
    private lateinit var medianButton : Button
    private lateinit var clearButton : Button

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
        setButtons()


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

    private fun setButtons() {
        rawButton = findViewById(R.id.bikeData)
        rawButton.setOnClickListener {
            addBikeRoute()
        }
        meanButton = findViewById(R.id.meanButton)
        meanButton.setOnClickListener {
            addMeanBikeRoute()
        }
        medianButton = findViewById(R.id.medianbutton)
        medianButton.setOnClickListener {
            addMedianBikeRoute()
        }
        clearButton = findViewById(R.id.clearbutton)
        clearButton.setOnClickListener {
            mMap.clear()
        }
    }

    private fun addMedianBikeRoute() {
        val options = PolylineOptions().width(5f).color(Color.GREEN).geodesic(true)
        if (bikePoints != null) {
            val latPoints =  ArrayList<Double>()
            val longPoints = ArrayList<Double>()

            //create lat and long lists
            for (element in bikePoints!!) {
                val point: LatLng = element.toLatLng()
                    latPoints.add(point.latitude)
                    longPoints.add(point.longitude)
            }

            //GetMedians
            val latMedianPoints =  ArrayList<Double>()
            val longMedianPoints = ArrayList<Double>()

            //get median for lat
            for (z in 0 until latPoints.size) {
               val box = getFivePointsBefore(latPoints, z)
                box.sortWith { c1, c2 ->
                    java.lang.Double.compare(c1, c2)
                }
                val medianPoint = median(box)
                latMedianPoints.add(medianPoint)
            }
            //get median for long
            for (z in 0 until longPoints.size) {
                val box = getFivePointsBefore(longPoints, z)
                box.sortWith { c1, c2 ->
                    java.lang.Double.compare(c1, c2)
                }
                val medianPoint = median(box)
                longMedianPoints.add(medianPoint)
            }
            //add points to route
            for (z in 0 until bikePoints!!.size) {
                val point: LatLng = LatLng(latMedianPoints[z],longMedianPoints[z])
                options.add(point)
            }
            val line = mMap.addPolyline(options)

            butifyRoute(bikePoints)
        }
    }

    private fun getFivePointsBefore(bikePoints: List<Double>, index: Int): ArrayList<Double> {
        var res = ArrayList<Double>()
        var start = index-5

        //avoid index out of bounds
        if(index-5 < 0){
            start = 0
        }

        for (z in start until index+1){
            res.add(bikePoints[z])
        }
        return res
    }

    // the array double[] m MUST BE SORTED
    private fun median(m: ArrayList<Double>): Double {
        val middle = m.size / 2
        return if (m.size % 2 == 1) {
            m[middle]
        } else {
            (m[middle - 1] + m[middle]) / 2.0
        }
    }

    private fun addMeanBikeRoute() {
        val options = PolylineOptions().width(5f).color(Color.RED).geodesic(true)
        if (bikePoints != null) {
            val latPoints =  ArrayList<Double>()
            val longPoints = ArrayList<Double>()

            //create lat and long lists
            for (element in bikePoints!!) {
                val point: LatLng = element.toLatLng()
                latPoints.add(point.latitude)
                longPoints.add(point.longitude)
            }

            //GetMedians
            val latMedianPoints =  ArrayList<Double>()
            val longMedianPoints = ArrayList<Double>()

            //get median for lat
            for (z in 0 until latPoints.size) {
                val box = getFivePointsBefore(latPoints, z)
                val medianPoint = mean(box.toDoubleArray())
                latMedianPoints.add(medianPoint)
            }
            //get median for long
            for (z in 0 until longPoints.size) {
                val box = getFivePointsBefore(longPoints, z)
                val medianPoint = mean(box.toDoubleArray())
                longMedianPoints.add(medianPoint)
            }
            //add points to route
            for (z in 0 until bikePoints!!.size) {
                val point: LatLng = LatLng(latMedianPoints[z],longMedianPoints[z])
                options.add(point)
            }
            val line = mMap.addPolyline(options)

            butifyRoute(bikePoints)
        }
    }

    fun mean(m: DoubleArray): Double {
        var sum = 0.0
        for (i in m.indices) {
            sum += m[i]
        }
        return sum / m.size
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

            butifyRoute(bikePoints)
        }
    }

    fun butifyRoute(points: List<LocationPoint>?){
        val firstL = points!![0].toLatLng()
        val midL = points[points.size / 2].toLatLng()
        val lastL = points[points.size - 1].toLatLng()
        mMap.addMarker(MarkerOptions().position(firstL).title("Start"))
        mMap.addMarker(MarkerOptions().position(lastL).title("End"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midL, 10.5f))
    }
}
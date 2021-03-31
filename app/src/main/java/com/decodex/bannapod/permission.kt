package com.decodex.bannapod

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.decodex.bannapod.constants.Companion.rc_location
import com.google.android.gms.location.*
import java.util.*

class permission : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient       //For Location
    lateinit var locationRequest: LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(CheckPermission() && isLocationEnabled()){   //If true, gets location and Goes to Main Activity
            getLastLocation()

        }else{
            val permission_textView = findViewById<TextView>(R.id.permission_status)
            val button_give_permission = findViewById<Button>(R.id.getpos)
            val info_textView = findViewById<TextView>(R.id.info)


            if(CheckPermission()){
                if(!isLocationEnabled()){
                    permission_textView.text = "PLEASE ENABLE LOCATION"
                }
            }

            button_give_permission.setOnClickListener {
                if(!CheckPermission()) {
                    RequestPermission()
                }
                //Code below shifted to OnRequestpermissionResult(), As User had to CLick the Button twice

                if(CheckPermission() && isLocationEnabled()){
                    permission_textView.text = "Getting User Location"
                    info_textView.text = "PLEASE WAIT"

                    getLastLocation()
                }

            }

        }
    }

    //Returns bool, true if all permissions are granted
    private fun CheckPermission():Boolean{
        if(
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false

    }

    //Requests Permission
    fun RequestPermission(){
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
                rc_location
        )
    }

    //Returns bool if Location Enabled
    fun isLocationEnabled():Boolean{
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //Has code inside, check it. It gets last location after user grants permission.
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if(requestCode == rc_location){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission","Location Permission Granted")

                //Code from Onclick Listner

                val permission_text = findViewById<TextView>(R.id.permission_status)
                val info_text = findViewById<TextView>(R.id.info)

                if(CheckPermission()){
                    if(!isLocationEnabled()){
                        permission_text.text = "PLEASE ENABLE LOCATION"
                    }
                }
                Log.d("Permission","Permission : ${CheckPermission()}")
                Log.d("Permission","Location Enabled : ${isLocationEnabled()}")

                if(CheckPermission() && isLocationEnabled()){
                    permission_text.text = "Getting User Location"
                    info_text.text = "PLEASE WAIT..."

                    getLastLocation()
                }
            }
        }
    }



    //Requests New Location
    fun NewLocationData(){
        locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )

    }

    //Gets last location if available, else Requests New location
    fun getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location: Location? = task.result

                    if(location == null){
                        Log.d("Permission", "Location Null, Requesting Location Updates")
                        NewLocationData()

                    }else{
                        user_latitude = location.latitude.toString()
                        user_longitude = location.longitude.toString()
                        Log.d("Permission", "Last Known User Location Fetched (Long, Lat) : $user_longitude $user_latitude ")

                        goto_Main_Activity()
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location", Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }


    //New Location Listner (On Location result)
    private val locationCallback = object : LocationCallback(){

        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation

            user_latitude = lastLocation.latitude.toString()
            user_longitude = lastLocation.longitude.toString()
            Log.d("Permission", "New User Location Fetched(long, lat) : $user_longitude $user_latitude ")

            goto_Main_Activity()

        }
    }


    //Takes longitude, latitude and returns Cityname
    private fun getCityName(lat: Double,long: Double):String{

        var cityName:String = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)

        cityName = Adress.get(0).locality
        return cityName
    }

    //Closes this activity, Opens Main Activity
    private fun goto_Main_Activity(){
        val MainActivity_intent = Intent(this@permission, MainActivity::class.java)

        MainActivity_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(MainActivity_intent)
        finish()
    }

}
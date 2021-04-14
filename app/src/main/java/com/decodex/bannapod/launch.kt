package com.decodex.bannapod

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_launch.*
import java.util.*

class launch : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient       //For Location
    lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checklogin()
    }

    fun checklogin() {

        startup_textview.setText("Checking Login Status")

        val firebase_auth =
            FirebaseAuth.getInstance()                          //Firebase auth Instance

        //Checks if already logged in
        if (firebase_auth.currentUser == null) { //User Not Logged IN

            welcome_textview.setText("WELCOME")
            startup_textview.setText("")
            progressBar1.isVisible = false
            start_button.visibility = View.VISIBLE

            start_button.setOnClickListener {

                startActivityForResult(                                             //Launches Firebase UI acitivty for Login
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("in")
                                    .setWhitelistedCountries(Arrays.asList("+91")).build()
                            )
                        ).setTheme(R.style.GreenTheme).build(), constants.rc_login
                )
            }


        } else {
            permission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //After Firebase UI activity result
        progressBar1.visibility = View.VISIBLE
        startup_textview.setText("")
        welcome_textview.setText("")
        start_button.visibility = View.INVISIBLE

        if (requestCode == constants.rc_login) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode === RESULT_OK) {

                val user_collection = Firebase.firestore.collection("users")
                val auth = FirebaseAuth.getInstance()
                val phone_no = auth.currentUser.phoneNumber


                val doc = user_collection.document(phone_no)

                doc.get().addOnSuccessListener {

                    if (it.data == null) {    //If no user data found, signup user

                        val signup_intent = Intent(this, SignUp::class.java)
                        signup_intent.putExtra("contact", phone_no.toString())
                        signup_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        startActivity(signup_intent)
                        finish()

                    } else {
                        permission()
                    }

                }.addOnFailureListener {
                    create_alert_dialogue("Failed to save user data")
                    Log.e("Sign up", "Failed to save user data. Error : ${it.message}")
                }

            } else {
                // Sign in failed
                if (response == null) { //User pressed back button
                    create_alert_dialogue("User Pressed Back Button")
                    Log.e("Sign up", "Sign up failed , user pressed back button")
                    //return

                } else if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) { //Network error

                    create_alert_dialogue("Please check your network and try again")
                    Log.e("Sign up", "Sign up failed, No network : ${response.error!!.message}")


                    //return
                } else { //other error

                    create_alert_dialogue("${response.error!!.message}")
                    Log.e("Sign up", "Sign up failed : ${response.error!!.message}")

                    //return
                }

            }
        }
    }

    fun permission() {

        startup_textview.setText("Checking Permissions")

        if (isLocationEnabled() && CheckPermission()) {
            getLastLocation()

        } else if (CheckPermission() && !isLocationEnabled()) {

            create_Locationalert_dialogue()

        } else {
            RequestPermission()
        }
    }

    //Has code inside, check it. It gets last location after user grants permission.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == constants.rc_location) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (CheckPermission()) {
                    if (!isLocationEnabled()) {
                        create_Locationalert_dialogue()

                    }
                }

                if (CheckPermission() && isLocationEnabled()) {
                    getLastLocation()

                } else {
                    val launch_intent = Intent(this, launch::class.java)
                    launch_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(launch_intent)
                    finish()
                }
            } else {
                create_alert_dialogue("Location Permission Denied. Please Grant Permissions and Try again.")

            }
        }
    }

    fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            constants.rc_location
        )
    }

    fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun CheckPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    //Gets last location if available, else Requests New location
    fun getLastLocation() {

        startup_textview.setText("Getting User Location")

        if (CheckPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result

                    if (location == null) {
                        Log.d("Launch", "Location Null, Requesting Location Updates")
                        NewLocationData()

                    } else {
                        user_latitude = location.latitude.toString()
                        user_longitude = location.longitude.toString()
                        Log.d(
                            "Permission",
                            "Last Known User Location Fetched (Long, Lat) : $user_longitude $user_latitude "
                        )

                        goto_Main_Activity()
                    }
                }.addOnFailureListener {
                    create_alert_dialogue("Failed to get User Location. Please Retry. ${it.message}")
                    Log.e("Location", "Failed to get user location : ${it.message}")

                }

            } else {

                create_Locationalert_dialogue()
            }
        } else {

            permission()
        }
    }

    //Requests New Location
    fun NewLocationData() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )

    }

    //New Location Listner (On Location result)
    var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            var lastLocation: Location = locationResult.lastLocation
            user_latitude = lastLocation.latitude.toString()
            user_longitude = lastLocation.longitude.toString()
            Log.d(
                "Permission",
                "New User Location Fetched(long, lat) : $user_longitude $user_latitude "
            )

            goto_Main_Activity()
        }
    }

    //Closes this activity, Opens Main Activity
    private fun goto_Main_Activity() {
        val MainActivity_intent = Intent(this, MainActivity::class.java)
        //MainActivity_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(MainActivity_intent)
        finish()
    }

    fun create_alert_dialogue(message: String = "") {

        val error_dialogue = android.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)        //Set icon method available
            .setPositiveButton("Retry") { _, _ ->
                val launch_intent = Intent(this, launch::class.java)
                launch_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(launch_intent)
                finish()
            }.setCancelable(false).create()

        error_dialogue.show()
    }

    fun create_Locationalert_dialogue(message: String = "Please Enable Location and Try Again") {

        val error_dialogue = android.app.AlertDialog.Builder(this)
            .setTitle("Location Disabled")
            .setMessage(message)        //Set icon method available
            .setPositiveButton("Retry") { _, _ ->
                checklogin()
            }.setCancelable(false).create()

        error_dialogue.show()
    }


}


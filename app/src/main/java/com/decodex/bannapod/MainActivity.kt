package com.decodex.bannapod

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.decodex.bannapod.constants.Companion.TOPIC
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

val TAG = "Main Activity"

var user_longitude = "NOT AVAILABLE"        //User Lat, Long
var user_latitude = "NOT AVAILABLE"
val auth = FirebaseAuth.getInstance()
val phone_no = auth.currentUser.phoneNumber

//todo support all screen sizes



class MainActivity : AppCompatActivity() {

    lateinit var navBar_toggle: ActionBarDrawerToggle

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (navBar_toggle.onOptionsItemSelected(item)) {
            return true
        }
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //For slidable menu

        navBar_toggle = ActionBarDrawerToggle(this,
            Main_DrawerLayout, R.string.nav_drawer_opened, R.string.nav_drawer_closed)

        Main_DrawerLayout.addDrawerListener(navBar_toggle)
        navBar_toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //For backbutton after opening the drawer

        //Menu Items
        Nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navItem_home -> {
                    val inn = Intent(this, MainActivity::class.java)
                    startActivity(inn)
                }
                R.id.navItem_doDonts -> {
                    val inn = Intent(this, do_donts::class.java)
                    startActivity(inn)
                }


                R.id.navItem_aboutUs -> startActivity(Intent(this, about_us::class.java))

            }
            true //we returned out of expression. we clicked something
        }

        doDonts_btn.setOnClickListener {
            val inn = Intent(this, do_donts::class.java)
            startActivity(inn)
        }



        main_userLocationcity.text = "${getCityName(
            user_latitude.toDouble(),
            user_longitude.toDouble()
        )}"

        main_location_coordinates.text = "$user_latitude $user_longitude"

        getUserdata()       //Gets user data
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        //Sos button
        btn_sendSos.setOnClickListener {

            create_confirm_dialogue()
        }

    }

    fun sos_button(){

        val time = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

        val user_collection = Firebase.firestore.collection("users")
        val doc = user_collection.document(phone_no)
        var last_time = ""

        Log.d("afd", "ddd")

        doc.get().addOnSuccessListener {

            val user = it.toObject<user>()
            last_time = user!!.last_request_time
            Log.d("afd", "fff")



            val last_req = check_last_request(time.format(Date()), last_time)

            if(last_req != -1){

                val error_dialogue = android.app.AlertDialog.Builder(this)
                    .setTitle("Cannot Send Request")
                    .setMessage("You Already Sent a Request. Please wait ${(900-last_req)/60} minutes before sending a new request.")        //Set icon method available
                    .setPositiveButton("OK") { _, _ ->

                    }
                    .setCancelable(false).create()

                error_dialogue.show()


            }else{

                Log.d("lala", "reached")
                val timee = SimpleDateFormat("HH:mm:ss")
                val datee = SimpleDateFormat("dd/MM/yyyy")

                val request = Notification_data(
                    phone_no.toString(), main_message.text.toString(), user_longitude,
                    user_latitude, time.format(Date()), main_UserName.text.toString(), getCityName(
                        user_latitude.toDouble(), user_longitude.toDouble()
                    ), timee.format(Date()), datee.format(Date())
                )


                save_request(request, time.format(Date()))

                Push_notification(
                    Notification_data(
                        phone_no.toString(),
                        main_message.text.toString(),
                        user_longitude,
                        user_latitude,
                        time.format(Date())
                    ),
                    "/topics/admin"
                ).also {
                    send_notification(it)
                }
            }



            btn_sendSos.resetSlider()

        }.addOnFailureListener {
            Log.d("afd", "${it.message}")

        }



    }

    //Sends notification over FCM
    private fun send_notification(notification: Push_notification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = Retrofit_instance.api.post_notification(notification)

                if (response.isSuccessful) {

                    Log.d(TAG, "Response Successful : ${Gson().toJson(response)}")
                } else {

                    Log.e(TAG, " Error while sending notification : ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {

                Log.e(TAG, " Error while sending notification : ${e.message.toString()}")
            }
        }

    //Gets user data, displays it to main screen. If user got deleted, Logout.
    private fun getUserdata() {

        val user_collection = Firebase.firestore.collection("users")
        val doc = user_collection.document(phone_no)

        doc.get().addOnSuccessListener {

            if (it.data == null) {

                auth.signOut()
                val launch_intent = Intent(this, launch::class.java)
                //auth_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(launch_intent)
                finish()
            } else {

                val user = it.toObject<user>()
                val name = "${user!!.firstname} ${user.lastname}"
                main_UserName.text = name
            }
        }
    }



    //Takes Lat,Long , Returns City Name
    private fun getCityName(lat: Double, long: Double): String {
        var cityName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat, long, 3)

        cityName = Adress.get(0).locality
        return cityName
    }

    fun check_last_request(current_datetime:String, last_datetime:String) : Int{



        val current_datetime_arr = current_datetime.split(" ")
        val current_date = current_datetime_arr[0]
        val current_time = current_datetime_arr[1]
        val current_date_arr = current_date.split("/")
        val current_time_arr = current_time.split(":")

        val last_datetime_arr = last_datetime.split(" ")
        val last_date = last_datetime_arr[0]
        val last_time = last_datetime_arr[1]
        val last_date_arr = last_date.split("/")
        val last_time_arr = last_time.split(":")

        if(current_date == last_date){
            val hour_c = current_time_arr[0].toInt()
            val min_c = current_time_arr[1].toInt()
            val sec_c = current_time_arr[2].toInt()

            val hour_l = last_time_arr[0].toInt()
            val min_l = last_time_arr[1].toInt()
            val sec_l = last_time_arr[2].toInt()

            val total_lSec = hour_l*60*60 + min_l*60 + sec_l
            val total_csec = hour_c*60*60 + min_c*60 + sec_c

            val diff = total_csec - total_lSec

            if(diff > 900){
                return -1
            }else{
                return diff
            }

        }

        return -1
    }

    //Takes in Data, Saves it to Firebase Collection
    private fun save_request(request: Notification_data, request_time: String) = CoroutineScope(Dispatchers.IO).launch {
        try {

            val date = SimpleDateFormat("dd.MM.yyyy")

            val requests_collection = Firebase.firestore.collection("requests")
            val user_collection = Firebase.firestore.collection("users")
            val time = SimpleDateFormat("HH:mm:ss")

            requests_collection.document("${time.format(Date())} $phone_no")
                .set(request)
                .addOnCompleteListener {

                Toast.makeText(this@MainActivity, "Request Sent", Toast.LENGTH_LONG).show()

            }.addOnFailureListener {

                Log.e(TAG, "Request Sending Failed : ${it.message}")
                Toast.makeText(this@MainActivity, "Something Went Wrong. ${it.message}", Toast.LENGTH_LONG).show()
            }.await()

            user_collection.document("$phone_no")
                .update("last_request_time", request_time)
                .addOnCompleteListener {


                }.addOnFailureListener {

                    Log.e(TAG, "Request Time Sending Failed : ${it.message}")
                    Toast.makeText(this@MainActivity, "Something Went Wrong. ${it.message}", Toast.LENGTH_LONG).show()
                }.await()

        } catch (e: Exception) {
            Log.e(TAG, "Error while sending request : ${e.message}")

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun create_confirm_dialogue(message: String = "Are you sure ?") {


        val error_dialogue = android.app.AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage(message)        //Set icon method available
            .setPositiveButton("Send Request") { _, _ ->
                sos_button()
            }.setNegativeButton("Cancel"){ _, _ ->
                btn_sendSos.resetSlider()
            }
            .setCancelable(false).create()

        error_dialogue.show()
    }


}
package com.decodex.bannapod
//Firebase Authentication, For login and signup
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.decodex.bannapod.constants.Companion.rc_login
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.*

const val auth_tag = "Authentication"       //Tag for Log

class Authentication : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val firebase_auth = FirebaseAuth.getInstance()  //Firebase auth Instance

        //Checks if already logged in
        if(firebase_auth.currentUser != null){  //User Already Logged IN

            val permission_intent = Intent(this, permission::class.java)
            permission_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(permission_intent)
            finish()

        }else { //User NOT logged IN

            auth_loginButton.setOnClickListener {
                startActivityForResult( //Launches Firebase UI acitivty for Login
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("in")
                                    .setWhitelistedCountries(Arrays.asList("+91")).build()
                            )
                        ).build(), rc_login
                )
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //After Firebase UI activity result

        if (requestCode == rc_login) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode === RESULT_OK) {

                val user_collection = Firebase.firestore.collection("users")
                val auth = FirebaseAuth.getInstance()
                val phone_no = auth.currentUser.phoneNumber


                val doc = user_collection.document(phone_no)

                doc.get().addOnSuccessListener {

                    if(it.data == null){    //If no user data found, signup user

                        val signup_intent = Intent(this@Authentication, SignUp::class.java)
                        signup_intent.putExtra("contact", phone_no.toString())
                        signup_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        startActivity(signup_intent)
                        finish()
                    }else{

                        val permission_intent = Intent(this, permission::class.java)
                        permission_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        startActivity(permission_intent)
                        finish()
                    }
                }

            } else {
                // Sign in failed
                if (response == null) {     //User pressed back button

                    auth_debug.text = "Failed !! User Pressed Back Button"
                    Log.e("Authentication", "Failed !! User Pressed Back Button")
                    return
                }else if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) { //Network error

                    auth_debug.text = "Failed !! Network Error"
                    return
                }else{ //other error

                    auth_debug.text = "Failed !! ${response.error}"
                }
                Log.e("Authentication", "Sign-in error: ", response.error)
            }
        }
    }


}
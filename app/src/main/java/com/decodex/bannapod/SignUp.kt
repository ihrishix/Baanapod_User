package com.decodex.bannapod

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val getintent = intent
        val contact = intent.getStringExtra("contact")



        btn_signup.setOnClickListener {
            val firstname = signup_firstname.text.toString()
            val lastname = signup_lastname.text.toString()
            val age = signup_age.text.toString()

            if(firstname.isEmpty()){
                Toast.makeText(this, "FirstName cannot be Empty", Toast.LENGTH_SHORT).show()
            }
            if(lastname.isEmpty()){
                Toast.makeText(this, "LastName cannot be Empty", Toast.LENGTH_SHORT).show()
            }
            if(age.isEmpty() || age.toInt() < 1){
                Toast.makeText(this, "Invalid Age", Toast.LENGTH_SHORT).show()
            }

            if(!(firstname.isEmpty() || lastname.isEmpty() || age.isEmpty() || age.toInt() < 0)){
                val user = user(firstname, lastname, age.toInt(), contact.toString())
                saveUser(user, contact!!)
            }


        }

    }

    private fun saveUser(user: user, contact:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val user_collection = Firebase.firestore.collection("users")
            user_collection.document(contact).set(user).addOnSuccessListener {

                    val launch_intent = Intent(this@SignUp, launch::class.java)
                    launch_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(launch_intent)
                    finish()
            }.await()



        }catch (e:Exception){
            create_alert_dialogue("${e.message}")
            Log.e("Sign Up", "Error while saving new user data : ${e.message}")
        }
    }

    fun create_alert_dialogue(message:String = ""){

        val error_dialogue = android.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)        //Set icon method available
            .setPositiveButton("Retry") { _ ,_ ->
                val launch_intent = Intent(this, launch::class.java)
                launch_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(launch_intent)
                finish()
            }.setCancelable(false).create()

        error_dialogue.show()
    }
}
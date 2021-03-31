package com.decodex.bannapod

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        //Todo check if text view are empty and put dropdown menu for age

        btn_signup.setOnClickListener {
            val firstname = signup_firstname.text.toString()
            val lastname = signup_lastname.text.toString()
            val age = signup_age.text.toString()

            val user = user(firstname, lastname, age.toInt(), contact.toString())

            saveUser(user, contact!!)


        }

    }

    private fun saveUser(user: user, contact:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val user_collection = Firebase.firestore.collection("users")
            user_collection.document(contact).set(user).await()

            withContext(Dispatchers.Main){
                Toast.makeText(this@SignUp, "Data Saved", Toast.LENGTH_LONG).show()

                //Goto Permission activity with data
                val permission_intent = Intent(this@SignUp, permission::class.java)
                permission_intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(permission_intent)
                finish()
            }

        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@SignUp, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
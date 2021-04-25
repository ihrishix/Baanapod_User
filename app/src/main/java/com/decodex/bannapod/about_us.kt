package com.decodex.bannapod

//About Us Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.internal.InternalTokenProvider
import kotlinx.android.synthetic.main.activity_about_us.*
import retrofit2.http.HTTP

class about_us : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        //Goes to insta handle

        insta_handle.setOnClickListener {
            val insta_uri = Uri.parse("https://www.instagram.com/decodex_2021/")
            startActivity(Intent(Intent.ACTION_VIEW, insta_uri))
        }

        //Opens email realted apps

        email.setOnClickListener {
            val email_intent = Intent(Intent.ACTION_SENDTO).apply {
                val mail = "mailto:decodex.sup@gmail.com"
                setData(Uri.parse(mail))
            }

            try {
                startActivity(email_intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
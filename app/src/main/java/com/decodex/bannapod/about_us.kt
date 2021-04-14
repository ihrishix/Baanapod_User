package com.decodex.bannapod

//About Us Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.internal.InternalTokenProvider
import kotlinx.android.synthetic.main.activity_about_us.*

class about_us : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        insta_handle.setOnClickListener {
            val insta_uri = Uri.parse("https://www.instagram.com/decodex_2021/")
            startActivity(Intent(Intent.ACTION_VIEW, insta_uri))
        }

        email.setOnClickListener {
            val email_intent = Intent(Intent.ACTION_SENDTO)
            email_intent.setType("message/rfc822")
            email_intent.putExtra(Intent.EXTRA_EMAIL, "decodex_support@gmail.com")

            startActivity(Intent.createChooser(email_intent, "Send Email"))
        }
    }
}
package com.example.tempsdeflors

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            val window = window
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
            decorView.systemUiVisibility = View.STATUS_BAR_HIDDEN
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // Canvia de pantalla
            val intent = Intent(this, MainActivity::class.java)

            /*val intent = if (savedPin  == null) {
                Intent(this, SetPasswordActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }*/
            startActivity(intent)
            finish()
        }, 8000) // 2000 ms = 2 segons
    }
}
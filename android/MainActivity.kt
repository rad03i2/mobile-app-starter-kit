package com.radwan.starter

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "Mobile App Starter Kit"
            textSize = 24f
            setPadding(32, 64, 32, 32)
        }

        setContentView(title)
    }
}

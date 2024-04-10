package com.raghav.cameramanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.raghav.cameramanager.databinding.ActivityClientBinding

class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

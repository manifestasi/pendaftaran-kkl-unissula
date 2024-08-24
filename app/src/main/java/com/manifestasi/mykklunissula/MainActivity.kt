package com.manifestasi.mykklunissula

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.manifestasi.mykklunissula.databinding.ActivityMainBinding
import com.manifestasi.mykklunissula.presentation.auth.AuthViewModel
import com.manifestasi.mykklunissula.presentation.auth.LoginActivity
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.KklDalamNegeriActivity
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.KklLuarNegeriActivity
import com.manifestasi.mykklunissula.presentation.pendaftarkkl.PendaftarActivity
import com.manifestasi.mykklunissula.presentation.profile.EditKKLLuarNegeriActivity
import com.manifestasi.mykklunissula.presentation.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[(AuthViewModel::class.java)]

        if (authViewModel.getCurrentUser() == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding.btnLn.setOnClickListener {
            startActivity(Intent(this,KklLuarNegeriActivity::class.java))
        }

        binding.btnDn.setOnClickListener {
            startActivity(Intent(this,KklDalamNegeriActivity::class.java))
        }

        binding.btnPendaftar.setOnClickListener {
            /* menuju list pendaftar */
            startActivity(Intent(this,PendaftarActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logoutUser().observe(this@MainActivity){ result ->
                if (result){
                    finishAffinity()
                } else {
                    Toast.makeText(this@MainActivity, "Oops logout gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
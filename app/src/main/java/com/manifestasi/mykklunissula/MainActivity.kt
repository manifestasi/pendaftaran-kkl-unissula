package com.manifestasi.mykklunissula

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        //inisialisasi viewmodel untuk mengambil fungsi ambil pengguna yang sedang login
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
            startActivity(Intent(this,PendaftarActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            showConfirmDialogLogout()
        }
    }

    //fungsi untuk menampilkan konfimasi dialog logout
    private fun showConfirmDialogLogout() {

        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.valid_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        val saveButton: Button = dialogView.findViewById(R.id.btn_yakin)
        val batalButton: Button = dialogView.findViewById(R.id.btn_batal)
        val text: TextView = dialogView.findViewById(R.id.tv_title)

        text.text = getString(R.string.logout)
        saveButton.text = getString(R.string.yakin)
        saveButton.setOnClickListener {
            authViewModel.logoutUser().observe(this@MainActivity){ result ->
                if (result){
                    dialog.dismiss()
                    finishAffinity()
                } else {
                    Toast.makeText(this@MainActivity, "Oops logout gagal", Toast.LENGTH_SHORT).show()
                }
            }

        }
        batalButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }
}
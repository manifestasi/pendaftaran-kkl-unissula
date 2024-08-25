package com.manifestasi.mykklunissula.presentation.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.manifestasi.mykklunissula.MainActivity
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityRegisterBinding
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewModel()

        setAction()
    }

    @SuppressLint("SetTextI18n")
    private fun setAction(){
        binding.apply {
            btnRegister.setOnClickListener {
                val nim: String = etNim.text.toString()
                val password: String = etPass.text.toString()
                val confirmPassword: String = etConfirmPass.text.toString()

                //proses register
                authViewModel.registerUser(nim, password, confirmPassword)
                authViewModel.registerResult.observe(this@RegisterActivity){ result ->
                    when (result){

                        //register ketika sedang login
                        is Resource.Loading -> {
                            btnRegister.isEnabled = false
                            btnRegister.text = "Loading..."
                        }

                        //register ketika berhasil
                        is Resource.Success -> {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, "Register berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        }

                        //register ketika gagal
                        is Resource.ErrorMessage -> {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                            btnRegister.text = "Register"
                        }
                        else -> {
                            btnRegister.isEnabled = true
                            btnRegister.text = "Register"
                        }
                    }
                }
            }
        }
    }

    //inisialisasi authviemodel untuk memanggil fungsi register
    private fun initViewModel(){
        authViewModel = ViewModelProvider(this)[AuthViewModel::class]
    }
}
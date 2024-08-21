package com.manifestasi.mykklunissula.presentation.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
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

    private fun setAction(){
        binding.apply {
            btnRegister.setOnClickListener {
                val nim: String = etNim.text.toString()
                val password: String = etPass.text.toString()
                val confirmPassword: String = etConfirmPass.text.toString()

                authViewModel.registerUser(nim, password, confirmPassword)
                authViewModel.registerResult.observe(this@RegisterActivity){ result ->
                    when (result){
                        is Resource.Loading -> {
                            btnRegister.isEnabled = false
                            Toast.makeText(this@RegisterActivity, "Loading...", Toast.LENGTH_LONG).show()
                        }
                        is Resource.Success -> {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, "Register berhasil, silahkan login", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.ErrorMessage -> {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            btnRegister.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun initViewModel(){
        authViewModel = ViewModelProvider(this)[AuthViewModel::class]
    }
}
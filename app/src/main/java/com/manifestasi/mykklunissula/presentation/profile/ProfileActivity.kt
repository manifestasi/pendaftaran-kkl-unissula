package com.manifestasi.mykklunissula.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityProfileBinding
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.cvKklluar.setOnClickListener {
            startActivity(Intent(this, EditKKLLuarNegeriActivity::class.java))
        }

        binding.cvKklldalam.setOnClickListener {
            startActivity(Intent(this, EditKKLDalamNegeriActivity::class.java))
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, EditKKLLuarNegeriActivity::class.java))
        }

        binding.btnNext2.setOnClickListener {
            startActivity(Intent(this, EditKKLDalamNegeriActivity::class.java))
        }

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.toolbar.tvTitlePage.text = getString(R.string.profile)

        initData()

    }

    private fun initData(){
        val uuid = profileViewModel.getCurrentUser()?.uid ?: ""

        profileViewModel.getDataPendaftarKklLuarNegeri(uuid)
        profileViewModel.resultKklLuarNegeri.observe(this){ result ->
            when(result){
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    if (result.data !== null){
                        Log.d("ProfileActivity", "luarnegeri: ${result.data}")
                        binding.apply {
                            tvStatusLn.visibility = View.VISIBLE
                            tvStatusLn.text = if (result.data.status == "1"){
                                "Under Review"
                            } else if (result.data.status == "2") {
                                "Accepted"
                            } else if (result.data.status == "3") {
                                "Rejected"
                            } else {
                                ""
                            }

                            val color = if (result.data.status == "1"){
                                R.color.review
                            } else if (result.data.status == "2") {
                                R.color.accept
                            } else if (result.data.status == "3") {
                                R.color.rejected
                            } else {
                                0
                            }

                            tvStatusLn.setTextColor(ContextCompat.getColor(this@ProfileActivity, color))
                        }
                    }

                }
                is Resource.ErrorMessage -> {
//                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", result.message)
                }
                else -> {}
            }
        }

        profileViewModel.getDataPendaftarKklDalamNegeri(uuid)
        profileViewModel.resultKklDalamNegeri.observe(this){ result ->
            when(result){
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (result.data !== null){
                        Log.d("ProfileActivity", "dalamnegeri: ${result.data}")
                        binding.apply {
                            tvStatusDn.visibility = View.VISIBLE
                            tvStatusDn.text = if (result.data.status == "1"){
                                "Under Review"
                            } else if (result.data.status == "2") {
                                "Accepted"
                            } else if (result.data.status == "3") {
                                "Rejected"
                            } else {
                                ""
                            }

                            val color = if (result.data.status == "1"){
                                R.color.review
                            } else if (result.data.status == "2") {
                                R.color.accept
                            } else if (result.data.status == "3") {
                                R.color.rejected
                            } else {
                                0
                            }

                            tvStatusDn.setTextColor(ContextCompat.getColor(this@ProfileActivity, color))
                        }
                    }
                }
                is Resource.ErrorMessage -> {
                    Log.e("ProfileActivity", result.message)
                }
                else -> {}
            }
        }
    }
}
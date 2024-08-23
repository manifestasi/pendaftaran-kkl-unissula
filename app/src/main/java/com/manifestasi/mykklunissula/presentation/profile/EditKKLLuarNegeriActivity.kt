package com.manifestasi.mykklunissula.presentation.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityEditKklluarNegeriBinding
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditKKLLuarNegeriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditKklluarNegeriBinding
    private val viewmodel: EditKKLViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditKklluarNegeriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadData()
    }

    fun loadData() {
        viewmodel.getDataFromFirestore()

        viewmodel.dataResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Loading data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    val data = resource.data
                    binding.etNama.setText(data?.get("nama") as String)
                    binding.etNim.setText(data["nim"] as String)
                    binding.etNohp.setText(data["noHp"] as String)
                    val jenisKelamin = data["jenisKelamin"] as String
                    when (jenisKelamin) {
                        "Laki-Laki" -> binding.radioGroup.check(R.id.radio_button_1)
                        "Perempuan" -> binding.radioGroup.check(R.id.radio_button_2)
                    }
                    binding.etSmtkelas.setText(data["smtKelas"] as String)
                    binding.etKotakeberangkatan.setText(data["kotaBerangkat"] as String)
                    binding.etKotakepulangan.setText(data["kotaPulang"] as String)
                    binding.etEmail.setText(data["email"] as String)
                    val ktpUrl = data["ktpUrl"] as? String
                    val fotoUrl = data["fotoUrl"] as? String
                    val pasporUrl = data["pasporUrl"] as? String

                    ktpUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.ivPlaceholder)
                    }
                    fotoUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.ivPlaceholderFoto)
                    }
                    pasporUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.ivPlaceholderPaspor)
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(
                        this,
                        "Failed to load data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }
}
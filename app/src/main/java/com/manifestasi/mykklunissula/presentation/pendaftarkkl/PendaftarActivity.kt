package com.manifestasi.mykklunissula.presentation.pendaftarkkl

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.data.model.DataListPendaftar
import com.manifestasi.mykklunissula.data.repository.PendaftarRepository
import com.manifestasi.mykklunissula.databinding.ActivityPendaftarBinding
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PendaftarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendaftarBinding
    private lateinit var pendaftarViewModel: PendaftarViewModel
    private lateinit var dataPendaftar: HashMap<String, ArrayList<DataListPendaftar>>
    private lateinit var listPendaftarAdapter: ListPendaftarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendaftarBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewModel()
        setUpData()
        setAction()

    }

    //fungsi untuk proses pencarian pengguna
    private fun setAction(){
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null){
                    try {
                        filter(newText)
                    } catch (e: Exception){
                        Toast.makeText(this@PendaftarActivity, "Ops.. something wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                return true
            }

        })

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    //fungsi untuk mengambil data pengguna
    private fun setUpData(){
        pendaftarViewModel.getALlDataPendaftarFromFirestore()
        pendaftarViewModel.resultPendaftar.observe(this){ result ->
            when(result){
                is Resource.Loading -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.rvPendaftar.visibility = View.GONE
                }
                is Resource.Success -> {
                    dataPendaftar = result.data!!

                    Log.d("PendaftarActivity", dataPendaftar[PendaftarRepository.TEMP]!!.toString())
                    Log.d("PendaftarActivity original", dataPendaftar[PendaftarRepository.ORIGINAL]!!.toString())

                    binding.loading.visibility = View.GONE
                    binding.rvPendaftar.visibility = View.VISIBLE
                    showRecycleList()
                }
                is Resource.ErrorMessage -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    binding.loading.visibility = View.GONE
                    binding.rvPendaftar.visibility = View.VISIBLE
                }
                else -> {
                    binding.loading.visibility = View.GONE
                    binding.rvPendaftar.visibility = View.VISIBLE
                }
            }
        }
    }

    //fungsi untuk menampilkan daftar pengguna
    private fun showRecycleList(){
        binding.rvPendaftar.layoutManager = LinearLayoutManager(this)
        listPendaftarAdapter = ListPendaftarAdapter()
        binding.rvPendaftar.adapter = listPendaftarAdapter
        listPendaftarAdapter.submitList(dataPendaftar[PendaftarRepository.TEMP])
    }

    //fungsi untuk melakukan filter data berdasarkan nama pengguna
    private fun filter(newText: String?){
        val listM: ArrayList<DataListPendaftar> = ArrayList()
        val getDataOriginal = dataPendaftar[PendaftarRepository.ORIGINAL]
        for (item in getDataOriginal!!){
            if (item.name!!.lowercase().contains(newText?.lowercase() ?: "")){
                listM.add(item)
            }
        }

        if(listM.isEmpty()){
//            Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show()
        } else {
            listPendaftarAdapter.submitList(listM)
        }
    }

    //inisialisasi viewmodel
    private fun initViewModel(){
        pendaftarViewModel = ViewModelProvider(this)[PendaftarViewModel::class]
    }
}
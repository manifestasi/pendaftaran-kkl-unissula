package com.manifestasi.mykklunissula.presentation.profile

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.manifestasi.mykklunissula.BuildConfig
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityEditKklluarNegeriBinding
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.PendaftaranKKLViewModel
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class EditKKLLuarNegeriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditKklluarNegeriBinding
    private val viewmodel: EditKKLViewModel by viewModels()
    private var currentImageUri: Uri? = null
    private var ktpImageUri: Uri? = null
    private var fotoImageUri: Uri? = null
    private var pasporImageUri: Uri? = null
    private var previousKtpUrl: String? = null
    private var previousFotoUrl: String? = null
    private var previousPasporUrl: String? = null
    private var scanType: ScanType? = null
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
        binding.rlKtp.setOnClickListener {
            scanType = ScanType.KTP
            startGallery()
        }
        binding.rlFoto.setOnClickListener {
            scanType = ScanType.FOTO
            startGallery()
        }
        binding.rlPaspor.setOnClickListener {
            scanType = ScanType.PASPOR
            startGallery()
        }
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSimpan.setOnClickListener { showConfirmDialog() }
        binding.btnHapus.setOnClickListener { showConfirmDialogDelete() }
        loadData()
    }


    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            val resizedImageUri = resizeImage(uri)
            if (resizedImageUri != null) {
                currentImageUri = resizedImageUri
                showImage()
            } else {
                Log.d("Photo Picker", "Failed to resize image")
            }
        } else {
            Log.d("Photo Picker", "No media selected")
        }
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

                    previousKtpUrl = ktpUrl
                    previousFotoUrl = fotoUrl
                    previousPasporUrl = pasporUrl

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

    private fun showImage() {
        currentImageUri?.let {
            when (scanType) {
                ScanType.KTP -> {
                    ktpImageUri = it
                    updateImage(ScanType.KTP, it, previousKtpUrl)
                    binding.rlKtp.visibility = View.VISIBLE
                    binding.ivPlaceholder.setImageURI(it)
                }

                ScanType.FOTO -> {
                    fotoImageUri = it
                    updateImage(ScanType.FOTO, it, previousFotoUrl)
                    binding.rlFoto.visibility = View.VISIBLE
                    binding.ivPlaceholderFoto.setImageURI(it)
                }

                ScanType.PASPOR -> {
                    pasporImageUri = it
                    updateImage(ScanType.PASPOR, it, previousPasporUrl)
                    binding.rlPaspor.visibility = View.VISIBLE
                    binding.ivPlaceholderPaspor.setImageURI(it)
                }

                else -> {
                    Log.e("MainActivity", "Unknown scan type")
                }
            }
        }
    }

    private fun resizeImage(uri: Uri): Uri? {
        try {
            // Dapatkan input stream dari URI gambar yang dipilih
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Tentukan resolusi target, misalnya 800x800 piksel
            val targetWidth = 800
            val targetHeight = (originalBitmap.height * targetWidth) / originalBitmap.width

            // Resize bitmap
            val resizedBitmap =
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false)

            // Simpan bitmap hasil resize ke file sementara
            val uniqueFileName = "resized_image_${System.currentTimeMillis()}.jpg"
            val file = File(cacheDir, uniqueFileName)
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                80,
                outputStream
            ) // 80 adalah kualitas gambar (0-100)
            outputStream.close()

            // Dapatkan URI dari file sementara
            return FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressindicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showConfirmDialog() {

        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.valid_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        val saveButton: Button = dialogView.findViewById(R.id.btn_yakin)
        val batalButton: Button = dialogView.findViewById(R.id.btn_batal)
        saveButton.setOnClickListener {
            updateData()
            dialog.dismiss()
        }
        batalButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun showConfirmDialogDelete() {

        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.valid_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        val saveButton: Button = dialogView.findViewById(R.id.btn_yakin)
        val batalButton: Button = dialogView.findViewById(R.id.btn_batal)
        saveButton.text = getString(R.string.hapus)
        saveButton.setOnClickListener {
            deleteImage()
            dialog.dismiss()
        }
        batalButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun showSuccessDialog() {

        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.valid_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        val saveButton: Button = dialogView.findViewById(R.id.btn_yakin)
        val batalButton: Button = dialogView.findViewById(R.id.btn_batal)
        val title: TextView = dialogView.findViewById(R.id.tv_title)
        val icon: ImageView = dialogView.findViewById(R.id.iv_icon)

        saveButton.text = getString(R.string.lihat)
        batalButton.visibility = View.GONE
        title.text =
            getString(R.string.selamat_data_anda_berhasil_tersimpan_admin_akan_segera_menghubungi_anda_silahkan_klik_untuk_melihat_data_anda)
        icon.setImageResource(R.drawable.ic_smile)

        saveButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }

    fun updateData() {
        val nama = binding.etNama.text.toString()
        val nim = binding.etNim.text.toString()
        val noHp = binding.etNohp.text.toString()
        val jenisKelamin = when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radio_button_1 -> "Laki-Laki"
            R.id.radio_button_2 -> "Perempuan"
            else -> ""
        }
        val smtKelas = binding.etSmtkelas.text.toString()
        val kotaBerangkat = binding.etKotakeberangkatan.text.toString()
        val kotaPulang = binding.etKotakepulangan.text.toString()
        val email = binding.etEmail.text.toString()

        val data = mutableMapOf<String, Any>(
            "nama" to nama,
            "nim" to nim,
            "noHp" to noHp,
            "jenisKelamin" to jenisKelamin,
            "smtKelas" to smtKelas,
            "kotaBerangkat" to kotaBerangkat,
            "kotaPulang" to kotaPulang,
            "email" to email
        )

        previousKtpUrl?.let {
            data["ktpUrl"] = it
        }

        previousFotoUrl?.let {
            data["fotoUrl"] = it
        }

        previousPasporUrl?.let {
            data["pasporUrl"] = it
        }

        viewmodel.updateDataInFirestore(COLLECTION_PATH, data)

        viewmodel.saveResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    Toast.makeText(this, "Updating data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    showSuccessDialog()
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to update data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    private fun deleteData() {
        viewmodel.deleteDataFromFirestore(COLLECTION_PATH)

        viewmodel.deleteResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    Toast.makeText(this, "Updating data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    showSuccessDialog()
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to update data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    private fun deleteImage() {
        viewmodel.deleteImage()

        viewmodel.deleteResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    Toast.makeText(this, "Updating data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    deleteData()
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to update data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }


    private fun validateFormAndUpload() {
        // Validasi form
        val nama = binding.etNama.text.toString()
        val nim = binding.etNim.text.toString()
        val noHp = binding.etNohp.text.toString()
        val jenisKelamin = when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radio_button_1 -> "Laki-Laki"
            R.id.radio_button_2 -> "Perempuan"
            else -> ""
        }
        val smtKelas = binding.etSmtkelas.text.toString()
        val kotaBerangkat = binding.etKotakeberangkatan.text.toString()
        val kotaPulang = binding.etKotakepulangan.text.toString()
        val email = binding.etEmail.text.toString()

        // Cek apakah semua field form sudah diisi
        if (nama.isEmpty() || nim.isEmpty() || noHp.isEmpty() || jenisKelamin.isEmpty() ||
            smtKelas.isEmpty() || kotaBerangkat.isEmpty() || kotaPulang.isEmpty() || email.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika form valid, lanjutkan ke upload gambar

    }

    fun updateImage(scanType: ScanType, imageUri: Uri, previousUrl: String?) {
        viewmodel.updateImage(scanType, imageUri, previousUrl)

        viewmodel.uploadResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    Toast.makeText(this, "Updating image...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    val imageUrl = resource.data?.get(scanType)
                    if (scanType == ScanType.KTP) {
                        previousKtpUrl = imageUrl
                    } else if (scanType == ScanType.FOTO) {
                        previousFotoUrl = imageUrl
                    } else if (scanType == ScanType.PASPOR) {
                        previousPasporUrl = imageUrl
                    }
                    // Update Firestore with new image URL

                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to update image: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    companion object {
        private const val COLLECTION_PATH = "daftar_KKLluarnegeri"
    }

}
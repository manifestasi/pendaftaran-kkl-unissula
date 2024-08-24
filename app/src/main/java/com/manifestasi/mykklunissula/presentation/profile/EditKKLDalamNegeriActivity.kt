package com.manifestasi.mykklunissula.presentation.profile

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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.manifestasi.mykklunissula.BuildConfig
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityEditKkldalamNegeriBinding
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class EditKKLDalamNegeriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditKkldalamNegeriBinding
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
        binding = ActivityEditKkldalamNegeriBinding.inflate(layoutInflater)
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

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSimpan.setOnClickListener { showConfirmDialog() }
        binding.btnHapus.setOnClickListener { showConfirmDialogDelete() }

        binding.toolbar.tvTitlePage.text= getString(R.string.data_kkl_dalam_negeri)
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

    private fun loadData() {
        viewmodel.getDataFromFirestore(COLLECTION_PATH)

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
                    binding.rlKtp.visibility = View.VISIBLE
                    binding.ivPlaceholder.setImageURI(it)
                }

                ScanType.FOTO -> {
                    fotoImageUri = it
                    binding.rlFoto.visibility = View.VISIBLE
                    binding.ivPlaceholderFoto.setImageURI(it)
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
        val text: TextView = dialogView.findViewById(R.id.tv_title)

        text.text = getString(R.string.updateQuestion)
        saveButton.text = getString(R.string.update)
        saveButton.setOnClickListener {
            validateFormAndUpload()
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
        val text: TextView = dialogView.findViewById(R.id.tv_title)

        text.text= getString(R.string.hapusdata)
        saveButton.text = getString(R.string.hapus)
        saveButton.setOnClickListener {
            deleteImage()
            deleteData()
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

        saveButton.text = getString(R.string.oke)
        batalButton.visibility = View.GONE
        title.text = getString(R.string.updateData)
        icon.setImageResource(R.drawable.ic_smile)

        saveButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun showSuccessDialogDelete() {

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

        saveButton.text = getString(R.string.oke)
        batalButton.visibility = View.GONE
        title.text = getString(R.string.dataterhapus)
        icon.setImageResource(R.drawable.ic_smile)

        saveButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()

    }

    private fun deleteData() {
        viewmodel.deleteDataFromFirestore(COLLECTION_PATH)

        viewmodel.deleteResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    Toast.makeText(this, "Delete data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    showSuccessDialogDelete()
                    Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to delete data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    private fun deleteImage() {
        viewmodel.deleteImage(COLLECTION_PATH)

        viewmodel.deleteResultimage.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }

                is Resource.Success -> {
                    showLoading(false)

                }

                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to delete image: ${resource.exception.message}",
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
        val email = binding.etEmail.text.toString()

        // Cek apakah semua field form sudah diisi
        if (nama.isEmpty() || nim.isEmpty() || noHp.isEmpty() || jenisKelamin.isEmpty() ||
            smtKelas.isEmpty() || email.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        updateAllImagesAndData()
    }

    private fun updateAllImagesAndData() {
        // Tampilkan loading
        showLoading(true)
        Toast.makeText(this, "Updating data...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // Kumpulan URL gambar baru yang akan diupdate
                val updatedImageUrls = mutableMapOf<ScanType, String?>()

                // Update gambar KTP jika ada
                ktpImageUri?.let {
                    val newKtpUrl = viewmodel.updateImage(COLLECTION_PATH,ScanType.KTP, it, previousKtpUrl)
                    updatedImageUrls[ScanType.KTP] = newKtpUrl
                }

                // Update gambar Foto jika ada
                fotoImageUri?.let {
                    val newFotoUrl = viewmodel.updateImage(COLLECTION_PATH,ScanType.FOTO, it, previousFotoUrl)
                    updatedImageUrls[ScanType.FOTO] = newFotoUrl
                }

                // Update gambar Paspor jika ada
                pasporImageUri?.let {
                    val newPasporUrl = viewmodel.updateImage(COLLECTION_PATH,ScanType.PASPOR, it, previousPasporUrl)
                    updatedImageUrls[ScanType.PASPOR] = newPasporUrl
                }

                // Setelah semua gambar diupload, persiapkan data untuk diupdate ke Firestore
                val data = mutableMapOf<String, Any>(
                    "nama" to binding.etNama.text.toString(),
                    "nim" to binding.etNim.text.toString(),
                    "noHp" to binding.etNohp.text.toString(),
                    "jenisKelamin" to when (binding.radioGroup.checkedRadioButtonId) {
                        R.id.radio_button_1 -> "Laki-Laki"
                        R.id.radio_button_2 -> "Perempuan"
                        else -> ""
                    },
                    "smtKelas" to binding.etSmtkelas.text.toString(),
                    "email" to binding.etEmail.text.toString()
                )

                // Masukkan URL gambar yang baru ke dalam data
                updatedImageUrls[ScanType.KTP]?.let { data["ktpUrl"] = it }
                updatedImageUrls[ScanType.FOTO]?.let { data["fotoUrl"] = it }
                updatedImageUrls[ScanType.PASPOR]?.let { data["pasporUrl"] = it }

                // Update data di Firestore
                viewmodel.updateDataInFirestore(COLLECTION_PATH, data)

                // Observasi hasil update
                viewmodel.saveResult.observe(this@EditKKLDalamNegeriActivity) { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            showSuccessDialog()
                            Toast.makeText(this@EditKKLDalamNegeriActivity, "Data updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            Toast.makeText(
                                this@EditKKLDalamNegeriActivity,
                                "Failed to update data: ${resource.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@EditKKLDalamNegeriActivity, "Failed to update data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val COLLECTION_PATH = "daftar_KKLdalamnegeri"
    }
}
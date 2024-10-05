package com.manifestasi.mykklunissula.presentation.pendaftarankkl


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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.manifestasi.mykklunissula.BuildConfig
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityKklDalamNegeriBinding
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class KklDalamNegeriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKklDalamNegeriBinding
    private val viewmodel: PendaftaranKKLViewModel by viewModels()
    private var currentImageUri: Uri? = null
    private var ktpImageUri: Uri? = null
    private var fotoImageUri: Uri? = null
    private var buktiImageUri: Uri? = null
    private var scanType: ScanType? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityKklDalamNegeriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnScanktp.setOnClickListener {
            scanType = ScanType.KTP
            startGallery()
        }
        binding.btnScanfoto.setOnClickListener {
            scanType = ScanType.FOTO
            startGallery()
        }
        binding.btnScanbukti.setOnClickListener {
            scanType = ScanType.BUKTI
            startGallery()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSimpan.setOnClickListener { showConfirmDialog() }

        observeImage()
        observeData()

    }

    //fungsi untuk membuka gallery
    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    //fungsi untuk menerima hasil dari gallery
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

    //fungsi untuk menampilkan gambar yang sudah dipilih dari gallery
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

                ScanType.BUKTI -> {
                    buktiImageUri = it
                    binding.rlBukti.visibility = View.VISIBLE
                    binding.ivPlaceholderBukti.setImageURI(it)
                }

                else -> {
                    Log.e("MainActivity", "Unknown scan type")
                }
            }
        }
    }

    //fungsi untuk validasi data apakah ada yang belum di isi atau tidak
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

        // Jika form valid, lanjutkan ke upload gambar
        uploadAllImages()
    }

    //fungsi untuk mengupload gambar ke server
    private fun uploadAllImages() {
        // Cek apakah semua gambar sudah dipilih
        if (ktpImageUri == null || fotoImageUri == null || buktiImageUri == null) {
            Toast.makeText(this, "All images must be selected", Toast.LENGTH_SHORT).show()
            return
        }
        val imageUris = mapOf(
            ScanType.KTP to ktpImageUri,
            ScanType.FOTO to fotoImageUri,
            ScanType.BUKTI to buktiImageUri
        )

        // Kirim gambar ke ViewModel untuk di-upload
        viewmodel.uploadMultipleImages(COLLECTION_PATH, imageUris)
    }

    //fungsi untuk menyimpan data yang telah diisi
    private fun saveFormData(imageUrls: Map<ScanType, String>) {
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

        /* get uid from user active */
        val uid = viewmodel.getCurrentUser()?.uid ?: ""

        if (nama.isEmpty() || nim.isEmpty() || noHp.isEmpty() || jenisKelamin.isEmpty() ||
            smtKelas.isEmpty() || email.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "id" to uid,
            "nama" to nama,
            "nim" to nim,
            "noHp" to noHp,
            "jenisKelamin" to jenisKelamin,
            "smtKelas" to smtKelas,
            "email" to email,
            "ktpUrl" to imageUrls[ScanType.KTP],
            "fotoUrl" to imageUrls[ScanType.FOTO],
            "buktiUrl" to imageUrls[ScanType.BUKTI],
            "status" to "1"
        )

        viewmodel.saveDataToFirestore(data, COLLECTION_PATH)
    }

    //fungsi untuk mengamati ketika terjadi perubahan gambar
    private fun observeImage() {
        // Observasi hasil upload gambar
        viewmodel.uploadResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Tampilkan loading saat upload
                    showLoading(true)
                    Toast.makeText(this, "Uploading images...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    // Gambar berhasil diupload, lanjutkan dengan menyimpan data ke Firestore
                    showLoading(false)
                    val imageUrls = resource.data
                    if (imageUrls != null) {
                        saveFormData(imageUrls)
                    }
                }

                is Resource.Error -> {
                    // Tampilkan pesan
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to upload image: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    //fungsi untuk mengamati ketika terjadi perubahan data
    private fun observeData() {
        // Observasi hasil simpan data ke Firestore
        viewmodel.saveResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    // Tampilkan loading saat menyimpan data
                    Toast.makeText(this, "Saving data...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    showLoading(false)
                    showSuccessDialog()
                    // Data berhasil disimpan
                    Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    showLoading(false)
                    // Tampilkan pesan error
                    Toast.makeText(
                        this,
                        "Failed to save data: ${resource.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    //fungsi untuk mengurangi dimensi gambar
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

    //fungsi untuk menampilkan loading
    private fun showLoading(isLoading: Boolean) {
        binding.progressindicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    //fungsi untuk menampilkan konfirmasi dialog
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
            validateFormAndUpload()
            dialog.dismiss()
        }
        batalButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    //fungsi untuk menampilkan informasi dialog ketika berhasil
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
        title.text = getString(R.string.selamat)
        icon.setImageResource(R.drawable.ic_smile)

        saveButton.setOnClickListener {
            dialog.dismiss()
            clearData()
            finish()
        }
        dialog.show()

    }

    //fungsi untuk membersihkan data yang di isi
    private fun clearData() {
        binding.etNama.setText("")
        binding.etNim.setText("")
        binding.etNohp.setText("")
        binding.etSmtkelas.setText("")
        binding.etEmail.setText("")
        binding.etNama.setText("")

        binding.radioGroup.clearCheck()

        binding.ivPlaceholder.setImageDrawable(null)
        binding.ivPlaceholderFoto.setImageDrawable(null)
        binding.ivPlaceholderBukti.setImageDrawable(null)

        binding.rlKtp.visibility = View.GONE
        binding.rlFoto.visibility = View.GONE
        binding.rlBukti.visibility = View.GONE
    }

    companion object {
        private const val COLLECTION_PATH = "daftar_KKLdalamnegeri"
    }
}
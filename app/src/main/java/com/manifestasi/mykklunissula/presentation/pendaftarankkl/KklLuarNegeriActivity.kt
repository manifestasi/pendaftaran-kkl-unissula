package com.manifestasi.mykklunissula.presentation.pendaftarankkl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.manifestasi.mykklunissula.R
import com.manifestasi.mykklunissula.databinding.ActivityKklLuarNegeriBinding
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.manifestasi.mykklunissula.util.CameraActivity
import com.manifestasi.mykklunissula.util.CameraActivity.Companion.CAMERAX_RESULT
import com.manifestasi.mykklunissula.util.getImageUri

class KklLuarNegeriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKklLuarNegeriBinding
    private var currentImageUri: Uri? = null
    private var scanType: ScanType? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityKklLuarNegeriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.btnScanktp.setOnClickListener {
            scanType = ScanType.KTP
            startCameraX()
        }
        binding.btnScanfoto.setOnClickListener {
            scanType = ScanType.FOTO
            startCameraX()
        }
        binding.btnScanpaspor.setOnClickListener {
            scanType = ScanType.PASPOR
            startCameraX()
        }


    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            when (scanType) {
                ScanType.KTP -> {
                    binding.rlKtp.visibility = View.VISIBLE
                    binding.ivPlaceholder.setImageURI(it)
                }

                ScanType.FOTO -> {
                    binding.rlFoto.visibility = View.VISIBLE
                    binding.ivPlaceholderFoto.setImageURI(it)
                }

                ScanType.PASPOR -> {
                    binding.rlPaspor.visibility = View.VISIBLE
                    binding.ivPlaceholderPaspor.setImageURI(it)
                }

                else -> {
                    Log.e("MainActivity", "Unknown scan type")
                }
            }
        }
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}

enum class ScanType {
    KTP, FOTO, PASPOR
}
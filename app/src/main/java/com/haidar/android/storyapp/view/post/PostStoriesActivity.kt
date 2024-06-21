package com.haidar.android.storyapp.view.post

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.haidar.android.storyapp.createCustomTempFile
import com.haidar.android.storyapp.databinding.ActivityPostStoriesBinding
import com.haidar.android.storyapp.uriToFile
import com.haidar.android.storyapp.view.ViewModelFactory
import com.haidar.android.storyapp.view.post.CameraActivity.Companion.CAMERAX_RESULT
import kotlinx.coroutines.launch
import java.io.File


class PostStoriesActivity : AppCompatActivity() {
    private val viewModel by viewModels<PostStoriesViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityPostStoriesBinding

    private var getFile: File? = null
    private lateinit var currentImagePath: String

    private var storyLat: Double = 0.0
    private var storyLon: Double = 0.0

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.loading.observe(this) { isLoading ->
            binding.pbPost.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { isError ->
            if (isError) {
                Toast.makeText(this, "Error uploading story", Toast.LENGTH_SHORT).show()
            }
        }

        checkPermission()
        fetchLocation()

        startGallery()
        startCamera()
        startCameraX()
        setupPost()

        binding.cbUseLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("Location", "${storyLat.toString()}, ${storyLon.toString()}")
                setupPostWithLocation()
            }
        }

    }

    private fun startGallery() {
        binding.btnGallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            val picImage = Intent.createChooser(intent, "Pilih Gambar")
            launcherIntentGallery.launch(picImage)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@PostStoriesActivity)
            getFile = myFile
            binding.ivStoryPreview.setImageURI(selectedImg)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSIONS) {
            if (!permissionGranted()) {
                Toast.makeText(
                    this@PostStoriesActivity,
                    "Berikan akses kepada kamera",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun permissionGranted() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!permissionGranted()) {
            ActivityCompat.requestPermissions(
                this@PostStoriesActivity,
                PERMISSIONS,
                CODE_PERMISSIONS
            )
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentImagePath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)

            binding.ivStoryPreview.setImageBitmap(result)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        binding.btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(packageManager)

            createCustomTempFile(applicationContext).also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@PostStoriesActivity,
                    "com.haidar.android.storyapp.camera",
                    it
                )
                currentImagePath = it.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == CAMERAX_RESULT) {
            val imageUriString = result.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)
            val imageUri = imageUriString?.toUri() as Uri
            val myFile = uriToFile(imageUri, this)
            getFile = myFile

            binding.ivStoryPreview.setImageURI(imageUri)
        }
    }

    private fun startCameraX() {
        binding.btnCameraX.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            launcherIntentCameraX.launch(intent)
        }
    }

    fun setupPost() {
        binding.btnPost.setOnClickListener {
            val desc = binding.etDescription.text.toString()
            if (!TextUtils.isEmpty(desc) && getFile != null) {
                lifecycleScope.launch {
                    try {
                        viewModel.postStory(getFile!!, desc, storyLat, storyLon)
                        viewModel.loading.observe(this@PostStoriesActivity) { isLoading ->
                            if (!isLoading) {
                                Toast.makeText(
                                    applicationContext,
                                    "Berhasil Memposting",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("post", e.toString())
                    }
                }
            } else {
                Toast.makeText(this, "Harap Lengkapi Data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setupPostWithLocation() {
        binding.btnPost.setOnClickListener {
            if (isLocationEnabled()) {
                val desc = binding.etDescription.text.toString()
                if (!TextUtils.isEmpty(desc) && getFile != null) {
                    lifecycleScope.launch {
                        try {
                            viewModel.postStory(getFile!!, desc, storyLat, storyLon)
                            viewModel.loading.observe(this@PostStoriesActivity) { isLoading ->
                                if (!isLoading) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Berhasil Memposting",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("post", e.toString())
                        }
                    }
                } else {
                    Toast.makeText(this, "Harap Lengkapi Data", Toast.LENGTH_SHORT).show()
                }
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Tidak dapat mengambil lokasi")
                builder.setMessage("Aktifkan GPS untuk mengirim lokasi terkini")
                builder.setPositiveButton("Buka Pengaturan") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                builder.setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }

        }
    }

    private fun fetchLocation() {
        val lastLocation = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        lastLocation.addOnSuccessListener {
            if (it != null) {
                storyLat = it.latitude
                storyLon = it.longitude
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }


    companion object {
        val PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        const val CODE_PERMISSIONS = 10
        const val SPACE_TIME = 1000L
    }

}
package com.haidar.android.storyapp.view.maps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.haidar.android.storyapp.R
import com.haidar.android.storyapp.databinding.ActivityMapsBinding
import com.haidar.android.storyapp.view.ViewModelFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var isMapLoaded = false

    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
                Log.d("Maps", "load style gagal")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Maps", "Style not found", e)
        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        viewModel.listStories.observe(this) { stories ->
            for (story in stories) {
                val location = LatLng(story.lat, story.lon)
                addCustomMarker(story.photoUrl, location, story.name, story.description)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            }
            showLoading(false)
            isMapLoaded = true
        }

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingDescTv.visibility = View.VISIBLE
            binding.pbMaps.visibility = View.VISIBLE
            binding.mapContainer.visibility = View.INVISIBLE
        } else {
            binding.loadingDescTv.visibility = View.INVISIBLE
            binding.pbMaps.visibility = View.INVISIBLE
            binding.mapContainer.visibility = View.VISIBLE
        }
    }

    private fun addCustomMarker(url: String, location: LatLng, title: String, snippet: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val scaledBitmap = Bitmap.createScaledBitmap(resource, 100, 100, false)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                            .title(title)
                            .snippet(snippet)
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(title)
                            .snippet(snippet)
                    )
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(title)
                            .snippet(snippet)
                    )
                }

            })
    }
}
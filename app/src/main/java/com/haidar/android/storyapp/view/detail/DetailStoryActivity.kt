package com.haidar.android.storyapp.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.databinding.ActivityDetailStoryBinding
import com.haidar.android.storyapp.dateFormat

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        val storyItem = intent.getParcelableExtra<ListStoryItem>(DETAIL_STORY) as ListStoryItem
        setupView(storyItem)
    }

    private fun setupView(storyItem: ListStoryItem) {
        Glide.with(this@DetailStoryActivity)
            .load(storyItem.photoUrl)
            .into(binding.ivStoryDetail)

        storyItem.apply {
            binding.tvUsername.text = name
            binding.tvDescription.text = description
            binding.tvStoryDate.text = createdAt.dateFormat()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val DETAIL_STORY = "DETAIL_STORY"
    }


}
package com.haidar.android.storyapp.view.main

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidar.android.storyapp.R
import com.haidar.android.storyapp.adapter.LoadingStateAdapter
import com.haidar.android.storyapp.adapter.StoryAdapter
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.databinding.ActivityMainBinding
import com.haidar.android.storyapp.view.ViewModelFactory
import com.haidar.android.storyapp.view.detail.DetailStoryActivity
import com.haidar.android.storyapp.view.detail.DetailStoryActivity.Companion.DETAIL_STORY
import com.haidar.android.storyapp.view.maps.MapsActivity
import com.haidar.android.storyapp.view.post.PostStoriesActivity
import com.haidar.android.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: ViewModelFactory
    private val viewModel: MainViewModel by viewModels { factory }
    private var isLoading = false

    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        factory = ViewModelFactory.getInstance(this)

        binding.fabPost.setOnClickListener {
            val intent = Intent(this, PostStoriesActivity::class.java)
            postActivityResultLauncher.launch(intent)
        }
        binding.fabMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        viewModel.getSession().observe(this) { user ->
            if (user.userId!!.isEmpty()) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        setupView()
        showListStories()
        refreshHandler()
    }


    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun showListStories() {
        storyAdapter = StoryAdapter()
        val rvStories = binding.rvStories

        isLoading = true
        binding.pbStories.visibility = View.VISIBLE
        binding.rvStories.visibility = View.GONE

        rvStories.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                storyAdapter.retry()
            }
        )
        rvStories.layoutManager = LinearLayoutManager(this)

        viewModel.getListStory.observe(this) {
            storyAdapter.submitData(lifecycle, it)
        }

        storyAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(
                data: ListStoryItem,
                sharedViews: Array<Pair<View, String>>
            ) {
                val intent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                intent.putExtra(DETAIL_STORY, data)
                startActivity(intent)
            }
        })

        isLoading = false
        binding.pbStories.visibility = View.GONE
        binding.rvStories.visibility = View.VISIBLE
    }

    private fun refresh() {
        binding.swipeRefresh.isRefreshing = true
        storyAdapter.refresh()
        Timer().schedule(1000) {
            binding.swipeRefresh.isRefreshing = false
            binding.rvStories.smoothScrollToPosition(0)
        }
    }


    private fun refreshHandler() {
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
    }


    private val postActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                delay(PostStoriesActivity.SPACE_TIME)
                refresh()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dropdown, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
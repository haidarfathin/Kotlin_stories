package com.haidar.android.storyapp.view.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidar.android.storyapp.R
import com.haidar.android.storyapp.adapter.StoryAdapter
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.databinding.ActivityMainBinding
import com.haidar.android.storyapp.view.ViewModelFactory
import com.haidar.android.storyapp.view.detail.DetailStoryActivity
import com.haidar.android.storyapp.view.detail.DetailStoryActivity.Companion.DETAIL_STORY
import com.haidar.android.storyapp.view.post.PostStoriesActivity
import com.haidar.android.storyapp.view.signup.SignupActivity
import com.haidar.android.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.fabPost.setOnClickListener {

            startActivity(Intent(this, PostStoriesActivity::class.java))
        }

        viewModel.getSession().observe(this) { user ->
            if (user.userId!!.isEmpty()) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        viewModel.listStories.observe(this) {
            if (it.isEmpty()) {
                Toast.makeText(this, "Unknown User", Toast.LENGTH_SHORT).show()
            } else {
                showListStories(this, it)
            }
        }

        viewModel.loading.observe(this) {
            showLoading(it)
        }

        setupView()
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            binding.pbStories.visibility = View.VISIBLE
            binding.rvStories.visibility = View.GONE

            viewModel.getStories()

            binding.pbStories.visibility = View.GONE
            binding.rvStories.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbStories.visibility = View.VISIBLE
            binding.rvStories.visibility = View.GONE
        } else {
            binding.pbStories.visibility = View.GONE
            binding.rvStories.visibility = View.VISIBLE
        }
    }


    private fun showListStories(context: Context, stories: List<ListStoryItem>) {
        val storiesRv = binding.rvStories

        isLoading = true
        binding.pbStories.visibility = View.VISIBLE
        binding.rvStories.visibility = View.GONE

        val listStoriesAdapter = StoryAdapter(stories)
        storiesRv.adapter = listStoriesAdapter
        storiesRv.layoutManager = LinearLayoutManager(context)

        listStoriesAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(
                data: ListStoryItem,
                sharedViews: Array<Pair<View, String>>
            ) {
                val intent = Intent(context, DetailStoryActivity::class.java)
                intent.putExtra(DETAIL_STORY, data)
                startActivity(intent)
            }
        })

        isLoading = false
        binding.pbStories.visibility = View.GONE
        binding.rvStories.visibility = View.VISIBLE
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
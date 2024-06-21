package com.haidar.android.storyapp.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.paging.StoryRepository
import com.haidar.android.storyapp.di.Injection
import com.haidar.android.storyapp.view.login.LoginViewModel
import com.haidar.android.storyapp.view.main.MainViewModel
import com.haidar.android.storyapp.view.maps.MapsViewModel
import com.haidar.android.storyapp.view.post.PostStoriesViewModel

class ViewModelFactory(
    private val userRepo: UserRepository,
    private val storyRepo: StoryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyRepo, userRepo) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepo) as T
            }

            modelClass.isAssignableFrom(PostStoriesViewModel::class.java) -> {
                PostStoriesViewModel(userRepo) as T
            }

            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(userRepo) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(
                        Injection.provideUserRepository(context),
                        Injection.provideStoryRepository(context),
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}
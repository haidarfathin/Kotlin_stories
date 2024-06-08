package com.haidar.android.storyapp.di

import android.content.Context
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.paging.StoryRepository
import com.haidar.android.storyapp.data.paging.database.StoryDatabase
import com.haidar.android.storyapp.data.pref.UserPreference
import com.haidar.android.storyapp.data.pref.dataStore

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig().getApiServices()
        val userRepository = provideUserRepository(context)
        return StoryRepository(context, apiService, database, userRepository)
    }
}
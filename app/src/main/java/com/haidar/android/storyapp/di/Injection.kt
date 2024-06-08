package com.haidar.android.storyapp.di

import android.content.Context
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.pref.UserPreference
import com.haidar.android.storyapp.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }
}
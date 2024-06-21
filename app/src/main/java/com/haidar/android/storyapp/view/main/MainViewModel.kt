package com.haidar.android.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.model.UserModel
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.data.paging.StoryRepository
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepo: StoryRepository, private var userRepo: UserRepository) :
    ViewModel() {
    val getListStory: LiveData<PagingData<ListStoryItem>> =
        storyRepo.getListStories().cachedIn(viewModelScope)

    fun getSession(): LiveData<UserModel> {
        return userRepo.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepo.logout()
        }
    }
}
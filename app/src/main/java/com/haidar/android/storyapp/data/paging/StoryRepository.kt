package com.haidar.android.storyapp.data.paging

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiService
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import com.haidar.android.storyapp.data.paging.database.StoryDatabase

class StoryRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val userRepo: UserRepository
) {
    fun getListStories(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(context, storyDatabase, apiService, userRepo),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }

        ).liveData
    }
}
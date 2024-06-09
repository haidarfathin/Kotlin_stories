package com.haidar.android.storyapp.view.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.model.response.GetStoriesResponse
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    init {
        getStories()
    }

    private fun getStories() {
        viewModelScope.launch {
            repository.getSession().collect { userModel ->
                val token = userModel.token
                val client = token?.let {
                    ApiConfig().getApiServices()
                        .getStories(token = "Bearer $it", size = 50, location = 0)
                }

                client?.enqueue(object : Callback<GetStoriesResponse> {
                    override fun onResponse(
                        call: Call<GetStoriesResponse>,
                        response: Response<GetStoriesResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            _listStories.value = response.body()?.listStory
                        } else {
                            Log.e("MapsViewModel", "onFailure: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<GetStoriesResponse>, t: Throwable) {
                        Log.e("MapsViewModel", "onFailure: ${t.message.toString()}")
                    }

                })
            }
        }
    }
}
package com.haidar.android.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.model.UserModel
import com.haidar.android.storyapp.data.model.response.GetStoriesResponse
import com.haidar.android.storyapp.data.model.response.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    init {
        getStories()
    }

    fun getStories() {
        _loading.value = false
        _error.value = false
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
                        if (response.body()?.error == false) {
                            _listStories.value = response.body()?.listStory
                            _error.value = false
                            _loading.value = false
                        } else {
                            _error.value = true
                            _loading.value = false
                        }
                    }

                    override fun onFailure(call: Call<GetStoriesResponse>, t: Throwable) {
                        _loading.value = false
                        _error.value = true
                    }

                })
            }
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}
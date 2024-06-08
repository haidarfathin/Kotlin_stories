package com.haidar.android.storyapp.view.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.model.response.PostStoriesResponse
import com.haidar.android.storyapp.reduceFileImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Callback
import java.io.File
import retrofit2.Call
import retrofit2.Response

class PostStoriesViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error


    fun postStory(imageFile: File, desc: String) {
        _loading.value = true
        val file = reduceFileImage(imageFile)

        val description = desc.toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        viewModelScope.launch {
            repository.getSession().collect { userModel ->
                val token = userModel.token

                val client = token?.let {
                    ApiConfig().getApiServices().postStories(
                        token = "Bearer $it",
                        file = imageMultipart,
                        description = description,
                        lat = 0F,
                        lon = 0F
                    )
                }

                client?.enqueue(object : Callback<PostStoriesResponse> {
                    override fun onResponse(
                        call: Call<PostStoriesResponse>,
                        response: Response<PostStoriesResponse>
                    ) {
                        if (response.body()?.error == false) {
                            _loading.value = false
                            _error.value = false
                        } else {
                            _loading.value = false
                            _error.value = true
                        }
                    }

                    override fun onFailure(call: Call<PostStoriesResponse>, t: Throwable) {
                        _loading.value = false
                        _error.value = true
                    }

                })
            }
        }


    }
}
package com.haidar.android.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.model.response.LoginResponse
import com.haidar.android.storyapp.data.model.response.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading : LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error : LiveData<Boolean> = _error

    private val _success = MutableLiveData<Boolean>()
    val success : LiveData<Boolean> = _success

    private val _registerResult = MutableLiveData<RegisterResponse>()
    val registerResult :LiveData<RegisterResponse> = _registerResult

    fun userSignup(name: String, email: String, password: String){
        _loading.value = true
        _error.value = false

        val apiService = ApiConfig().getApiServices().postRegister(name, email, password)
        apiService.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
               _loading.value = false
                if(response.isSuccessful && response.body()?.error == false){
                    _registerResult.value = response.body()
                    _success.value = true
                } else {
                    _error.value = true
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _loading.value = false
                _error.value = true
            }
        })
    }
}
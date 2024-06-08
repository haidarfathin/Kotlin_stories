package com.haidar.android.storyapp.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haidar.android.storyapp.data.UserRepository
import com.haidar.android.storyapp.data.api.ApiConfig
import com.haidar.android.storyapp.data.model.UserModel
import com.haidar.android.storyapp.data.model.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading : LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error : LiveData<Boolean> = _error

    private val _success = MutableLiveData<Boolean>()
    val success : LiveData<Boolean> = _success

    private val _authResult = MutableLiveData<LoginResponse>()
    val authResult :LiveData<LoginResponse> = _authResult

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun login(email: String, password: String) {
        _loading.value = true
        _error.value = false

        val apiService = ApiConfig().getApiServices().postLogin(email, password)
        apiService.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _loading.value = false
                if(response.isSuccessful && response.body()?.error == false){
                    _authResult.value = response.body()
                } else {
                    _error.value = true
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _loading.value = false
                _error.value = true
            }
        })
    }

}
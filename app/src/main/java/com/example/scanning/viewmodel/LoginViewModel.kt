package com.example.scanning.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scanning.ActivityViewState
import com.example.scanning.api.API
import com.example.scanning.api.ApiService
import com.example.scanning.model.APIResponse
import com.example.scanning.model.LoginModel
import com.example.scanning.utils.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginViewModel:ViewModel() {
    private val viewState: MutableLiveData<ActivityViewState> =
        MutableLiveData(ActivityViewState.COMPLETE)
    val viewStateLiveData: LiveData<ActivityViewState>
        get() = viewState

    fun setViewState(state: ActivityViewState) {
        viewState.postValue(state)
    }
    val loginLiveData:MutableLiveData<LoginModel> = MutableLiveData()
    val errorLiveData:MutableLiveData<Event<String>> = MutableLiveData()
    private val loginService = API.getInstance().create(ApiService::class.java)
    fun login(email:String,password:String) {
        setViewState(ActivityViewState.LOADING)
        loginService.login(email,password).enqueue (object : Callback<APIResponse<LoginModel>>{
            override fun onResponse(
                call: Call<APIResponse<LoginModel>>,
                response: Response<APIResponse<LoginModel>>
            ) {
                setViewState(ActivityViewState.COMPLETE)
                Log.d("tuandm", "onResponse: ${response.code()} - ${response.message()}")
                val loginModel = response.body()?.data
                if (loginModel !=null){
                    loginLiveData.value = loginModel!!
                } else errorLiveData.value = Event("Something wrong!")
            }

            override fun onFailure(call: Call<APIResponse<LoginModel>>, t: Throwable) {
                setViewState(ActivityViewState.COMPLETE)
                errorLiveData.value = Event(t.message?:"Something wrong!")
            }

        })
    }
}
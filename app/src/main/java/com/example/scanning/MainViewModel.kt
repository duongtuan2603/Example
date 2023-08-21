package com.example.scanning

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanning.api.API
import com.example.scanning.api.ApiService
import com.example.scanning.model.APIResponse
import com.example.scanning.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val viewState: MutableLiveData<ActivityViewState> =
        MutableLiveData(ActivityViewState.COMPLETE)
    val viewStateLiveData: LiveData<ActivityViewState>
        get() = viewState

    fun setViewState(state: ActivityViewState) {
        viewState.postValue(state)
    }

    val errorLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    private val service = API.getInstance().create(ApiService::class.java)

    val barcodeValue: MutableLiveData<String> = MutableLiveData("")
    val listImageUri: MutableLiveData<ArrayList<Uri>> = MutableLiveData()
    val checkBarcodeSuccessfully: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val createPackageSuccessfully: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun checkBarcode(token: String, barcodeValue: String) {
        setViewState(ActivityViewState.LOADING)
        if (barcodeValue.isNotEmpty() && barcodeValue != this.barcodeValue.value) {
            this.barcodeValue.value = barcodeValue
        }
        service.checkQR(token, barcodeValue).enqueue(object : Callback<APIResponse<Boolean>> {
            override fun onResponse(
                call: Call<APIResponse<Boolean>>, response: Response<APIResponse<Boolean>>
            ) {
                setViewState(ActivityViewState.COMPLETE)
                Log.d(
                    "tuandm",
                    "onResponseCheckBarCode: ${response.code()} - ${response.message()} - ${response.body()}"
                )
                val body = response.body()
                if (body != null) {
                    val success = body.data ?: false
                    if (success) {
                        checkBarcodeSuccessfully.value = Event(true)
                    } else {
                        errorLiveData.value = Event(body.message)
                    }
                } else errorLiveData.value = Event(response.message() ?: "Something wrong!")
            }

            override fun onFailure(call: Call<APIResponse<Boolean>>, t: Throwable) {
                setViewState(ActivityViewState.COMPLETE)
                errorLiveData.value = Event(t.message ?: "Something wrong!")
            }

        })
    }

    fun createPackage(
        token: String, requestBody: MultipartBody, qr: String, note: String, count: String,weight:String,price:String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            service.createProduct(
                token,
                requestBody.parts,
                qr.toRequestBody(MultipartBody.FORM),
                note.toRequestBody(MultipartBody.FORM),
                count.toRequestBody(MultipartBody.FORM),
                weight.toRequestBody(MultipartBody.FORM),
                price.toRequestBody(MultipartBody.FORM),
            ).enqueue(object : Callback<APIResponse<Any>> {
                override fun onResponse(
                    call: Call<APIResponse<Any>>, response: Response<APIResponse<Any>>
                ) {
                    Log.d("tuandm", "onResponseCreate: ${response.code()} - ${response.message()} - ${response.body()}")
                    setViewState(ActivityViewState.COMPLETE)
                    val success = response.body()?.data
                    if (success != null) {
                        createPackageSuccessfully.value = Event(true)
                    } else errorLiveData.value =
                        Event(response.message() ?: "Something wrong!")
                }

                override fun onFailure(call: Call<APIResponse<Any>>, t: Throwable) {
                    setViewState(ActivityViewState.COMPLETE)
                    errorLiveData.value = Event(t.message ?: "Something wrong!")
                }
            })
        }
    }
}
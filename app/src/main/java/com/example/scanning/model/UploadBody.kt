package com.example.scanning.model

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

data class UploadBody(
    val count:Int = 1,
    val note:String = "",
    val qr:String = "",
    @SerializedName("images[]")
    val images:List<MultipartBody.Part?> = listOf()
)
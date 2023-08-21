package com.example.scanning.api

import com.example.scanning.model.APIResponse
import com.example.scanning.model.LoginModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/login")
    fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<APIResponse<LoginModel>>

    @GET("api/products/checkqr/{code}")
    fun checkQR(
        @Header("Authorization") token: String,
        @Path("code") barcode: String
    ): Call<APIResponse<Boolean>>

    @Multipart
    @POST("api/products")
    fun createProduct(
        @Header("Authorization") token: String,
        @Part images: List<MultipartBody.Part>,
        @Part("qr") qr: RequestBody,
        @Part("note") note: RequestBody,
        @Part("count") count: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("price") price: RequestBody
    ): Call<APIResponse<Any>>
}
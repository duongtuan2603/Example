package com.example.scanning.model

data class APIResponse<T>(
    val code:Int = 0,
    val message:String = "",
    val data:T?=null
)

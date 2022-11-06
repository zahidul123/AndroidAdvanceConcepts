package com.example.diroomnavigation.Utils

import com.google.gson.Gson

var BASE_URL="https://5e510330f2c0d300147c034c.mockapi.io/"

public fun errorHandle(error: String?): String {
    error?.let {
        val errorResponse = Gson().fromJson(error, NormalResponse::class.java)
        return errorResponse.Message ?: "Something Went Wrong"
    } ?: run {
        return "Something Went Wrong"
    }
}

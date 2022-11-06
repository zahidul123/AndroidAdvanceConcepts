package com.example.diroomnavigation.NetworkApi

import com.example.diroomnavigation.Model.User
import retrofit2.Response
import retrofit2.http.GET

interface ApiServiceInterface {


    @GET("users")
    suspend fun getUsers(): Response<List<User>>


}
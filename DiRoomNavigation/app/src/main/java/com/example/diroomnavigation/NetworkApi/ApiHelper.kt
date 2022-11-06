package com.example.diroomnavigation.NetworkApi

import com.example.diroomnavigation.Model.User
import retrofit2.Response

interface ApiHelper {
    suspend fun getUsers(): Response<List<User>>
}
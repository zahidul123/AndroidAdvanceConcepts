package com.example.diroomnavigation.NetworkApi

import com.example.diroomnavigation.Model.User
import retrofit2.Response
import javax.inject.Inject

class DIApiHelperImpl  @Inject constructor(val apiServiceInterface: ApiServiceInterface) : ApiHelper{
    override suspend fun getUsers(): Response<List<User>> {
        return apiServiceInterface.getUsers()
    }
}
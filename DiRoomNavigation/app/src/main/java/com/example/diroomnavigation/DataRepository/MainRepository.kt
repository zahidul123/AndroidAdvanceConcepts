package com.example.diroomnavigation.DataRepository

import com.example.diroomnavigation.NetworkApi.ApiHelper
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getUsers() =  apiHelper.getUsers()

}
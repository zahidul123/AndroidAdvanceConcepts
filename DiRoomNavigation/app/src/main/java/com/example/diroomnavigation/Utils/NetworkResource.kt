package com.example.diroomnavigation.Utils

data class NetworkResource <out T>(val status: Status, val data: T?, val message: String?, val errorData: T?) {

    companion object {

        fun <T> success(data: T?): NetworkResource<T> {
            return NetworkResource(status = Status.SUCCESS, data=data, message = null,errorData = null)
        }

        fun<T> error(errorData:T?,message: String?): NetworkResource<T> {
            return  NetworkResource(status= Status.ERROR, data = null, message = message, errorData = errorData)
        }


        fun <T> loading(data: T?): NetworkResource<T> {
            return NetworkResource(status = Status.LOADING,data= data, message = null,errorData = null)
        }

    }

}
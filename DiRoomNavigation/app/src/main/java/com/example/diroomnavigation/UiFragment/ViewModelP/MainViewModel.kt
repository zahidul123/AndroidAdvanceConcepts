package com.example.diroomnavigation.UiFragment.ViewModelP

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.diroomnavigation.DataRepository.MainRepository
import com.example.diroomnavigation.Model.User
import com.example.diroomnavigation.Utils.NetworkHelper
import com.example.diroomnavigation.Utils.NetworkResource
import com.example.diroomnavigation.Utils.errorHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {
     private val _users = MutableLiveData<NetworkResource<List<User>>>()
     val users: LiveData<NetworkResource<List<User>>>
         get() = _users

     init {
         fetchUsers()
     }

    public fun fetchUsers() = liveData<NetworkResource<List<User>>> {
        emit(NetworkResource.loading(null))

        try {
            val response=mainRepository.getUsers()

            if (response.isSuccessful){
                //this@liveData.emit(NetworkResource.success(data = response))
                emit(NetworkResource.success(data=response.body()))
            }else{
                emit(NetworkResource.error(errorData = null,message = errorHandle(response.message())))
            }
        }catch (e: Exception){

        }

    }
    /* {

         viewModelScope.launch {
             _users.postValue(NetworkResource.loading(null))
             if (networkHelper.isNetworkConnected()) {
                 mainRepository.getUsers().let {
                     if (it.isSuccessful) {
                         _users.postValue(NetworkResource.success(it.body()))
                     } else _users.postValue(NetworkResource.error(it.errorBody().toString(), null))
                 }
             } else _users.postValue(NetworkResource.error("No internet connection", null))
         }
     }*/

}
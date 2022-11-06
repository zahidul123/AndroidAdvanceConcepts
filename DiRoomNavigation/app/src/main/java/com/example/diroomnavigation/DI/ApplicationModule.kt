package com.example.diroomnavigation.DI

import com.example.diroomnavigation.BuildConfig
import com.example.diroomnavigation.NetworkApi.ApiHelper
import com.example.diroomnavigation.NetworkApi.ApiServiceInterface
import com.example.diroomnavigation.NetworkApi.DIApiHelperImpl
import com.example.diroomnavigation.Utils.BASE_URL
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/*
*
* module class is a core concept where instance were says
* how to create an object or function of a class
*
*/



@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    fun provideBaseUrl() = BASE_URL


    @Provides
    @Singleton
    fun provideOkHttpClient() =
        if (BuildConfig.DEBUG) {

            //val loggingInterceptor = HttpLoggingInterceptor()
            //loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            OkHttpClient.Builder()
                .callTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request().newBuilder().build()
                    chain.proceed(request)
                })
                .build()
        } else {
            OkHttpClient
                .Builder()
                .build()
        }

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiServiceInterface =
        retrofit.create(ApiServiceInterface::class.java)


    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: DIApiHelperImpl): ApiHelper = apiHelper

}
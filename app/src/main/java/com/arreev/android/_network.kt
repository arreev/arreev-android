
package com.arreev.android

import javax.inject.*
import android.net.*
import android.content.*

/*
 * https://developer.android.com/training/basics/network-ops/
 */
class Network
{
    @Inject lateinit var context: android.content.Context

    init {
        ArreevApplication.appComponent.inject( this )
    }

    fun isAvailable() : Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService( Context.CONNECTIVITY_SERVICE ) as ConnectivityManager
        val connected = connectivityManager.activeNetworkInfo?.isConnected ?: false
        return connected
    }
}

/*
 * https://github.com/square/okhttp/wiki/Interceptors
 */
class ResponseInterceptor : okhttp3.Interceptor
{
    override fun intercept( chain:okhttp3.Interceptor.Chain ) : okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed( request )

        when ( response.code() ) {
            400 -> {}
            401 -> {}
            402 -> {}
            403 -> {}

            500 -> {}
            501 -> {}
            502 -> {}
            503 -> {}
        }

        return response
    }
}

fun retrofit( baseUrl:String ) : retrofit2.Retrofit {
    class Logging : okhttp3.logging.HttpLoggingInterceptor.Logger {
        override fun log( m:String ) {
            android.util.Log.d( "HTTP",m )
        }
    }

    val loggingInterceptor:okhttp3.logging.HttpLoggingInterceptor = okhttp3.logging.HttpLoggingInterceptor( Logging() )
    loggingInterceptor.setLevel( okhttp3.logging.HttpLoggingInterceptor.Level.BODY )

    val okHttpClient = okhttp3.OkHttpClient.Builder()
            .readTimeout(15000, java.util.concurrent.TimeUnit.MILLISECONDS )
            .addInterceptor( loggingInterceptor )
            .addInterceptor( ResponseInterceptor() )
            .build()

    val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()

    val retrofit:retrofit2.Retrofit = retrofit2.Retrofit.Builder()
            .addCallAdapterFactory( retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory.create() )
            .addConverterFactory( retrofit2.converter.gson.GsonConverterFactory.create( gson ) )
            .baseUrl( baseUrl )
            .client( okHttpClient )
            .build()

    return retrofit
}

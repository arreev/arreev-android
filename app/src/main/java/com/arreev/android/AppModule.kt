
package com.arreev.android

import java.util.concurrent.*
import javax.inject.*

import retrofit2.adapter.rxjava2.*
import retrofit2.converter.gson.*
import android.content.*
import com.google.gson.*
import okhttp3.logging.*
import retrofit2.*
import okhttp3.*
import dagger.*

/**
 * Created by GUBO on 7/26/2017.
 */
@Module
class AppModule( val application:ArreevApplication )
{
    @Provides
    @Singleton
    fun provideApplicationContext() : Context = application.applicationContext;

    @Provides
    @Singleton
    fun provideAccount() : Account = Account()

    @Provides
    @Singleton
    fun provideEventBus() : EventBus = EventBus()

    @Provides
    @Singleton
    fun provideFleetsPresenter() : FleetsPresenter = FleetsPresenter()

    @Provides
    @Singleton
    fun provideTransportersPresenter() : TransportersPresenter = TransportersPresenter()

    @Provides
    @Singleton
    fun provideTransporting() : Transporting = Transporting()

    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit {
        class Logging : HttpLoggingInterceptor.Logger {
            override fun log( m:String ) {
                android.util.Log.d( "HTTP",m )
            }
        }

        val loggingInterceptor:HttpLoggingInterceptor = HttpLoggingInterceptor( Logging() )
        loggingInterceptor.setLevel( HttpLoggingInterceptor.Level.BODY )

        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(15000, TimeUnit.MILLISECONDS )
                .addInterceptor( loggingInterceptor )
                .build()

        val gson = GsonBuilder().setPrettyPrinting().create()

        val retrofit : Retrofit = Retrofit.Builder()
                .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                .addConverterFactory( GsonConverterFactory.create( gson ) )
                .baseUrl( "https://api.arreev.com" )
                .client( okHttpClient )
                .build()

        return retrofit
    }
}
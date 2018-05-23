
package com.arreev.android.api

import javax.inject.*

import io.reactivex.*
import io.reactivex.schedulers.*
import io.reactivex.android.schedulers.*

import retrofit2.*
import retrofit2.http.*

import com.arreev.android.*

/**
 * Created by jeffschulz on 5/7/18.
 */
class FetchTransporter
{
    private data class Response(
            val transporter:Transporter,
            val status:Int?,
            val message:String?
    )

    @Inject lateinit var retrofit:Retrofit

    init {
        ArreevApplication.appComponent.inject( this )
    }

    private interface get
    {
        @GET( "/transporter" )
        fun get(
                @Query( "id" ) id:String
        ) :Observable<Response>
    }

    fun fetch( ownerid:String,id:String, start:Int,count:Int ) : Observable<Transporter> {
        return retrofit.newBuilder().baseUrl("https://api.arreev.com" )
                .build()
                .create( get::class.java )
                .get( id )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap { (transporter) -> Observable.just( transporter ) }
    }
}
package com.arreev.android

import javax.inject.*

import io.reactivex.android.schedulers.*
import com.arreev.android.model.*
import io.reactivex.schedulers.*
import io.reactivex.functions.*
import retrofit2.http.*
import io.reactivex.*
import retrofit2.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class FetchTransporter
{
    @Inject lateinit var retrofit:Retrofit

    private data class Response(
            val status: Int?,
            val message: String?,
            val transporter: Transporter?,
            val debug: String?
    )

    private interface get
    {
        @GET( "/transporter" )
        fun get(
                @Query( "id" ) id:String
        ) :Observable<Response>
    }

    init {
        ArreevApplication.appComponent.inject( this )
    }

    fun fetch( id:String ) : Observable<Transporter> {
        val map = Function<Response,Observable<Transporter>> {
            response -> Observable.just(response.transporter ?: Transporter() )
        }

        val observable : Observable<Transporter> = retrofit.create( get::class.java )
                .get( id )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( map )
        return observable
    }
}
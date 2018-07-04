
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
class FetchWaypoints
{
    private data class Response(
            val waypoints:List<Waypoint>,
            val status:Int?,
            val message:String?
    )

    @Inject lateinit var retrofit:Retrofit

    init {
        ArreevApplication.appComponent.inject(this )
    }

    private interface get
    {
        @GET( "/waypoints" )
        fun get(
                @Query( "ownerid" ) ownerid:String,
                @Query( "routeid" ) routeid:String
        ) :Observable<Response>
    }

    fun fetch( ownerid:String,routeid:String, start:Int,count:Int ) : Observable<Waypoint> {
        return retrofit.newBuilder().baseUrl("https://api.arreev.com" )
                .build()
                .create( get::class.java )
                .get( ownerid,routeid )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( { (waypoints) -> Observable.fromIterable( waypoints ) } )
    }
}
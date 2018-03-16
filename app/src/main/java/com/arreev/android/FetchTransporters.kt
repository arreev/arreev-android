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
class FetchTransporters
{
    @Inject lateinit var retrofit:Retrofit

    private data class Response(
            val status: Int?,
            val message: String?,
            val transporters: List<Transporter>?,
            val debug: String?
    )

    private interface get
    {
        @GET( "/transporters" )
        fun get(
                @Query( "ownerid" ) ownerid:String,
                @Query( "fleetid" ) fleetid:String
        ) :Observable<Response>
    }

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun fetch( ownerid:String,fleetid:String, start:Int,count:Int ) : Observable<Transporter> {
        val map = Function<Response,Observable<Transporter>> {
            response -> Observable.fromIterable(response.transporters ?: listOf<Transporter>() )
        }

        val observable : Observable<Transporter> = retrofit.create( get::class.java )
                .get( ownerid,fleetid )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( map )
        return observable
    }
}
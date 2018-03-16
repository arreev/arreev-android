
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
class FetchFleets
{
    @Inject lateinit var retrofit:Retrofit

    private data class Response(
        val status: Int?,
        val message: String?,
        val fleets: List<Fleet>?,
        val debug: String?
    )

    private interface get
    {
        @GET( "/fleets" )
        fun get(
                @Query( "ownerid" ) ownerid:String
        ) :Observable<Response>
    }

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun fetch( ownerid:String, start:Int,count:Int ) : Observable<Fleet> {
        val map = Function<Response,Observable<Fleet>> {
            response -> Observable.fromIterable(response.fleets ?: listOf<Fleet>() )
        }

        val observable : Observable<Fleet> = retrofit.create( get::class.java )
                .get( ownerid )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( map )
        return observable
    }
}
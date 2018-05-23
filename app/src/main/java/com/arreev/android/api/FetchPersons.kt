
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
class FetchPersons
{
    private data class Response(
            val persons:List<Person>,
            val status:Int?,
            val message:String?
    )

    @Inject lateinit var retrofit:Retrofit

    init {
        ArreevApplication.appComponent.inject( this )
    }

    private interface get
    {
        @GET( "/persons" )
        fun get(
                @Query( "ownerid" ) ownerid:String
        ) :Observable<Response>
    }

    fun fetch( ownerid:String, start:Int,count:Int ) : Observable<Person> {
        return retrofit.newBuilder().baseUrl("https://api.arreev.com" )
                .build()
                .create( get::class.java )
                .get( ownerid )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( { (persons) -> Observable.fromIterable( persons ) } )
    }
}
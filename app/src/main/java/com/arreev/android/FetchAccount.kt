
package com.arreev.android

import javax.inject.*

import io.reactivex.android.schedulers.*
import com.arreev.android.model.Account
import io.reactivex.schedulers.*
import io.reactivex.functions.*
import retrofit2.http.*
import io.reactivex.*
import retrofit2.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class FetchAccount
{
    @Inject lateinit var retrofit:Retrofit

    private data class Response(
            val status: Int?,
            val message: String?,
            val account: Account?,
            val debug: String?
    )

    private interface get
    {
        @GET( "/account" )
        fun get(
                @Query( "sub" ) sub:String
        ) :Observable<Response>
    }

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun fetch( sub:String ) : Observable<Account> {
        val map = Function<Response,Observable<Account>> {
            response -> Observable.just(response.account ?: Account() )
        }

        val observable : Observable<Account> = retrofit.create( get::class.java )
                .get( sub )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .concatMap( map )
        return observable
    }
}
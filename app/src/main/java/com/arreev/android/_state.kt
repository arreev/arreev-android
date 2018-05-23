
package com.arreev.android

import javax.inject.*

import io.reactivex.*
import io.reactivex.subjects.*
import io.reactivex.schedulers.*
import io.reactivex.android.schedulers.*

const val FLEETID           = "FLEETID"
const val TRANSPORTERID     = "TRANSPORTERID"
const val TRACKINGID        = "TRACKINGID"
const val TRACKINGLOCATION  = "TRACKINGLOCATION"
const val NONE              = "NONE"

data class Client(
        val fleetid: String? = null,
        val transporterid: String? = null,
        val trackingid: String? = null,
        val trackinglocation: android.location.Location? = null,
        val modification: String = NONE
)

/**
 * Created by jeffschulz on 5/15/18.
 */
class State
{
//    @Inject lateinit var personsPresenter : PersonsPresenter
//    @Inject lateinit var ridePresenter : RidePresenter
    private var _client = BehaviorSubject.create<Client>()

    init {
        ArreevApplication.appComponent.inject( this )
        _client.onNext( Client() )
    }

    fun getFleet() = _client.value.fleetid
    fun getTransporter() = _client.value.transporterid
    fun getTracking() = _client.value.trackingid
    fun getTrackingLocation() = _client.value.trackinglocation

    fun clearAll() {
        supercede( Client() )
    }

    fun setFleet( fleetid:String? ) {
        val client = _client.value
        if ( client.fleetid?.equals( fleetid ) ?: false ) { return }

        val newclient = Client(
                fleetid,
                client.transporterid,
                client.trackingid,
                client.trackinglocation,
                FLEETID
        )

        supercede( newclient )
    }

    fun setTransporter( transporterid:String? ) {
        val client = _client.value
        if ( client.transporterid?.equals( transporterid ) ?: false ) { return }

        val newclient = Client(
                client.fleetid,
                transporterid,
                client.trackingid,
                client.trackinglocation,
                TRANSPORTERID
        )

        supercede( newclient )
    }

    fun setTracking( trackingid:String? ) {
        val client = _client.value
        if ( client.trackingid?.equals( trackingid ) ?: false ) { return }

        val newclient = Client(
                client.fleetid,
                client.transporterid,
                trackingid,
                client.trackinglocation,
                TRACKINGID
        )

        supercede( newclient )
    }

    fun setTrackingLocation( trackinglocation:android.location.Location? ) {
        val client = _client.value
        if ( client.trackinglocation?.equals( trackinglocation ) ?: false ) { return }

        val newclient = Client(
                client.fleetid,
                client.transporterid,
                client.trackingid,
                trackinglocation,
                TRACKINGLOCATION
        )

        supercede( newclient )
    }

    fun observeClient() = _client.ofType( Client::class.java )
            .subscribeOn( Schedulers.io() )
            .observeOn( AndroidSchedulers.mainThread() )

    private fun supercede(newclient:Client ) {
        Observable.just( newclient )
                .subscribeOn( Schedulers.computation() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( { c:Client -> _client.onNext( c ) } )
    }
}

package com.arreev.android

import javax.inject.*

import io.reactivex.schedulers.*
import io.reactivex.disposables.*

import com.google.firebase.database.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/16/18.
 */
class Updating
{
    @Inject lateinit var state: State

    private var transporterreference: DatabaseReference? = null
    private var disposable: Disposable? = null

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun open( transporterid:String ) {
        dbg("Updating.open $transporterid" )

        transporterreference = FirebaseDatabase.getInstance().getReference( transporterid )

        disposable = state.observeClient().observeOn( Schedulers.trampoline() ).subscribe( { client:Client -> onStateChange( client ) } )
    }

    private fun onStateChange( client:Client ) {
        when ( client.modification ) {
            TRACKINGLOCATION -> {
                // dbg("TRACKINGLOCATION ${client.trackinglocation} ${Thread.currentThread()}" )
                val lat = client.trackinglocation?.latitude ?: 0.0
                val lng = client.trackinglocation?.longitude ?: 0.0
                transporterreference?.updateChildren( mapOf("lat" to lat, "lng" to lng ) )
            }
        }
    }

    fun close() {
        disposable?.dispose()
        disposable = null

        transporterreference = null

        dbg("Updating.close" )
    }
}
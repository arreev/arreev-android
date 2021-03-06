
package com.arreev.android

import javax.inject.*
import android.content.*
import android.os.Looper

import com.google.android.gms.location.*

import gubo.slipwire.dbg

/**
 * Created by jeffschulz on 5/16/18.
 */
class Tracking : LocationCallback()
{
    @Inject lateinit var context :Context
    @Inject lateinit var state : State

    private var client: FusedLocationProviderClient? = null

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun open( ownerid:String,transporterid:String? ) {
        dbg("Tracking.open $ownerid $transporterid" )

        client = LocationServices.getFusedLocationProviderClient( context )

        val request = LocationRequest().apply {
            interval = 7500
            fastestInterval = 2500
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest( request )
        val settings = LocationServices.getSettingsClient( context )
        settings.checkLocationSettings( builder.build() )
                .addOnSuccessListener { response -> validate( response ) }
                .addOnFailureListener { exception -> dbg( exception as Any ) }

        try {
            client?.removeLocationUpdates(this )

            client?.lastLocation?.addOnSuccessListener { location -> dbg( location ) }
            client?.requestLocationUpdates( request,this, Looper.myLooper() )

            state.setTracking( transporterid )
        } catch ( x:SecurityException ) {
            dbg( x )
        }
    }

    override fun onLocationResult( result:LocationResult? ) {
        result ?: return
        state.setTrackingLocation( result.lastLocation ?: null )
        //fencing.update( result.lastLocation )
    }

    override fun onLocationAvailability( availability:LocationAvailability? ) {
        // dbg( "Availability.isLocationAvailable ${availability?.isLocationAvailable}" )
    }

    fun close() {
        client?.removeLocationUpdates(this )
        client = null

        state.setTracking( null )

        dbg("Tracking.close" )
    }

    private fun validate( response:LocationSettingsResponse? ) {
        response ?: return
        /*
        val states = response.locationSettingsStates
        dbg("states.isBlePresent ${states.isBlePresent}" )
        dbg("states.isBleUsable ${states.isBleUsable}" )
        dbg("states.isGpsPresent ${states.isGpsPresent}" )
        dbg("states.isGpsUsable ${states.isGpsUsable}" )
        dbg("states.isLocationPresent ${states.isLocationPresent}" )
        dbg("states.isLocationUsable ${states.isLocationUsable}" )
        dbg("states.isNetworkLocationPresent ${states.isNetworkLocationPresent}" )
        dbg("states.isNetworkLocationUsable ${states.isNetworkLocationUsable}" )
        */
    }
}

package com.arreev.android

import javax.inject.*

import android.app.*
import android.content.*
import com.google.firebase.database.*
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase



/**
 * Created by jeffschulz on 3/9/18.
 *
 * https://firebase.google.com/docs/database/
 * https://firebase.google.com/docs/database/android/start/
 * https://firebase.google.com/docs/database/android/offline-capabilities
 */
class Transporting : LocationCallback()
{
    @Inject lateinit var applicationContext:Context
    @Inject lateinit var eventBus:EventBus

    private var transporterreference: DatabaseReference? = null
    private var client: FusedLocationProviderClient? = null
    private var transporterid: String? = null

    private val database: FirebaseDatabase

    init {
        ArreevApplication.appComponent.inject(this )

        FirebaseDatabase.getInstance().setPersistenceEnabled( true );
        database = FirebaseDatabase.getInstance()
        val rootreference = database.getReference()
        rootreference.keepSynced(true );

        val connectedreference = FirebaseDatabase.getInstance().getReference(".info/connected" ) // special firebase location
        connectedreference.addValueEventListener( object : ValueEventListener {
            override fun onDataChange( snapshot:DataSnapshot ) {
                val connected = snapshot.getValue() as Boolean
                eventBus.send( DatabaseClientConnectionEvent( connected ) )
            }
            override fun onCancelled( error:DatabaseError ) {
                dbg( error )
            }
        })

        rootreference.addValueEventListener( object : ValueEventListener {
            override fun onDataChange( snapshot:DataSnapshot ) {
                update( snapshot )
            }
            override fun onCancelled( error:DatabaseError ) {
                dbg( error )
            }
        })
    }

    /**
     *
     */
    fun startup() {
        dbg("TTT Transporting.startup" )

        client = LocationServices.getFusedLocationProviderClient( applicationContext )
    }

    /**
     * TODO: update transporterid in db
     */
    fun activate( activity:Activity,id:String ) {
        dbg("Transporting.activate $transporterid" )

        client?.removeLocationUpdates(this )
        transporterid = null
        transporterreference = null

        if ( id == null ) { return }

        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        try {
            transporterid = id
            transporterreference = database.getReference( transporterid )
            client?.requestLocationUpdates( locationRequest, this, null )
        } catch ( x:SecurityException ) {
            dbg( x )
        }
    }

    /**
     * TODO: read transporterid from db
     */
    fun isTransporting( id:String? ) : Boolean {
        if ( this.transporterid != null ) {
            return this.transporterid.equals( id )
        }
        return false
    }

    override fun onLocationResult( result:LocationResult? ) {
        dbg("onLocationResult $result" )

        try {
            val lat = result?.lastLocation?.latitude ?: 0.0
            val lng = result?.lastLocation?.longitude ?: 0.0
            transporterreference?.updateChildren( mapOf("lat" to lat, "lng" to lng ) )
        } catch ( x:Throwable ) {
            dbg( x )
        }
    }

    override fun onLocationAvailability( availability:LocationAvailability? ) {
        dbg("onLocationAvailability $availability" )
    }

    /**
     * TODO: clear transporterid in db
     */
    fun cancel() {
        client?.removeLocationUpdates(this )
        transporterid = null
        transporterreference = null

        dbg("Transporting.cancel" )
    }

    /**
     *
     */
    fun shutdown() {
        client?.removeLocationUpdates(this )
        transporterid = null
        transporterreference = null

        dbg("TTT Transporting.shutdown" )
    }

    private fun update( snapshot:DataSnapshot ) {
        try {
            for ( c in snapshot.children ) {
                //dbg( "${c.key} ${c.value!!::class.java.simpleName} ${c.value}" )
                val map = c.value as HashMap<String,Double>
                val transporterid = c.key.toString()
                val lat = map.get( "lat" ) ?: 0.0
                val lng = map.get( "lng" ) ?: 0.0
                eventBus.send( UpdateTransporterLocationEvent( transporterid,lat,lng ) )
            }
        } catch ( x:Exception ) {
            dbg( x )
        }
    }
}
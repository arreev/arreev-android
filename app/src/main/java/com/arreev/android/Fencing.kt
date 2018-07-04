
package com.arreev.android

import android.os.*
import android.app.*
import javax.inject.*
import android.content.*

import com.google.android.gms.awareness.*
import com.google.android.gms.common.api.*
import com.google.android.gms.awareness.fence.*

import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

import gubo.slipwire.*
import com.arreev.android.api.*

/**
 * https://developers.google.com/awareness/overview?authuser=1
 *
 * Use Location API GeoFencing instead ?
 * https://developer.android.com/training/location/geofencing
 *
 * Created by jeffschulz on 6/28/18.
 */
class Fencing : GoogleApiClient.ConnectionCallbacks,ValueEventListener
{
    private var googleclient: GoogleApiClient? = null
    private var reference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null

    @Inject lateinit var context :Context
    @Inject lateinit var state : State

    private lateinit var pendingintent: PendingIntent
    private lateinit var fencereceiver: FenceReceiver

    init {
        ArreevApplication.appComponent.inject(this )

        database = FirebaseDatabase.getInstance()

        pendingintent = PendingIntent.getBroadcast(
                context,
                11111,
                Intent("com.arreev.android.Fencing" ),
                0
        )

        fencereceiver = FenceReceiver()
    }

    fun open( ownerid:String,transporterid:String ) {
        dbg("Fencing.open $ownerid $transporterid" )

        context.registerReceiver( FenceReceiver(), IntentFilter("com.arreev.android.Fencing" ))

        reference = database?.getReference("enroutes" )?.child( ownerid )

        googleclient = GoogleApiClient.Builder( context )
                .addApi( Awareness.API )
                .addConnectionCallbacks(this )
                .build()
        googleclient?.connect()
    }

    override fun onConnected( bundle:Bundle? ) {
        reference?.addValueEventListener(this )
    }

    override fun onConnectionSuspended( cause:Int ) {
        dbg("Fencing.onConnectionSuspended " + cause )
    }

    override fun onDataChange( snapshot:DataSnapshot? ) {
        snapshot ?: return

        closeFence()

        val value = snapshot.child("enroute" ).value
        if ( value != null ) {
            val routeid = value as String
            FetchRoute().fetch(routeid ?: "" ).subscribe(
                    { r:Route -> openFence( r ) },
                    { x:Throwable -> dbg( x ) }
            )
        }
    }

    override fun onCancelled( error:DatabaseError? ) {
        dbg( error );
    }

    fun close() {
        closeFence()

        try { context.unregisterReceiver( fencereceiver ) } catch ( x:IllegalArgumentException ) {}

        reference?.removeEventListener(this )
        reference = null

        googleclient?.disconnect()
        googleclient = null

        dbg("Fencing.close" )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            ROUTEID -> {}
        }
    }

    private fun openFence( route:Route? ) {
        closeFence()

        route ?: return

        val waypoints = mutableListOf<Waypoint>()
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val routeid = route.id ?: ""

        FetchWaypoints().fetch( ownerid ,routeid,0,500 )
                .subscribe(
                        { w:Waypoint -> waypoints.add( w ) },
                        { x:Throwable -> dbg( x ) },
                        { fenceWaypoints( waypoints ) }
                )
    }

    private fun fenceWaypoints( waypoints:List<Waypoint> ) {
        try {
            val builder = FenceUpdateRequest.Builder()

            waypoints.forEach { w -> run {
                val lat = w.latitude ?: 0.0
                val lng = w.longitude ?: 0.0
                val radius = 10.0
                val dwell = 5000L;
                if ( (lat != 0.0) && (lng!= 0.0) ) {
                    builder.addFence("${w.id}:${w.name}:enter",LocationFence.entering( lat,lng,radius ),pendingintent )
                    builder.addFence("${w.id}:${w.name}:in",LocationFence.`in`( lat,lng,radius,dwell ),pendingintent )
                    builder.addFence("${w.id}:${w.name}:exit",LocationFence.exiting( lat,lng,radius ),pendingintent )
                }
            } }

            val request = builder.build()
            Awareness.getFenceClient( context )
                    .updateFences( request )
                    .addOnSuccessListener {}
                    .addOnFailureListener { dbg("<awareness fences FAILED register>" ) }
        } catch ( x:SecurityException ) {
            dbg( x )
        }
    }

    private fun closeFence() {
        Awareness.getFenceClient( context ).updateFences( FenceUpdateRequest.Builder().removeFence( pendingintent ).build() )
    }
}


package com.arreev.android.routes

import javax.inject.*

import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import io.reactivex.disposables.*

import com.google.firebase.auth.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.R
import com.arreev.android.api.*

/**
 * Created by jeffschulz on 6/22/18.
 */
class WaypointsPresenter : Presenter<WaypointsDisplay>,DataSource<Waypoint>, WaypointsDisplay.Listener
{
    private val waypoints = mutableListOf<Waypoint>()
    private var display: WaypointsDisplay? = null
    private var disposable: Disposable?  = null
    private var marker: Marker? = null
    private var map: GoogleMap? = null

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    init { _init() }

    override fun bind( d:WaypointsDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Waypoint { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) {}
    override fun requestRefresh() {}

    override fun onWaypoint( route:Waypoint ) {}

    fun setMap( map:GoogleMap? ) { _setMap( map ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject(this )

        state.observeClient().subscribe( { client:Client -> stateChange( client ) } )
    }

    private fun _bind( d:WaypointsDisplay ) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 0
        display?.pageSize = 0
        display?.setItemCount( waypoints.size )

        if ( waypoints.isEmpty() ) fetch(0,100 )
    }

    private fun _unbind() {
        display?.listener = null
        display?.release()
        display = null

        waypoints.clear()
    }

    private fun _release() {
        disposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        waypoints.clear()

        marker?.remove()
        marker = null
        map = null

        dbg( "WaypointsPresenter.release" )
    }

    private fun _getDataFor( position:Int ) : Waypoint {
        try {
            return waypoints[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Waypoint( id = "0" )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            TRACKINGLOCATION -> {
                updateLocation( state.getTrackingLocation() )
            }
            ROUTEID -> {
                display?.setItemCount( 0 )
                waypoints.clear()
                fetch( 0,100 )
            }
        }
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        disposable?.dispose()

        val routeid = state.getRoute()
        if ( routeid == null ) { return }

        disposable = FetchWaypoints().fetch( ownerid ?: "",routeid ?: "", start,count ).subscribe(
                { w:Waypoint -> onNext( w ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun onNext( w:Waypoint ) {
        val indexof = waypoints.indexOf( w )
        when ( indexof ) {
            -1 -> { waypoints.add( w ) }
            else -> { waypoints[ indexof ] = w }
        }
    }

    private fun onComplete() {
        waypoints.sortBy { w -> w.index }
        display?.setItemCount( waypoints.size )
    }

    private fun onError() {
        eventbus.send( NetworkErrorEvent() )
    }

    private fun _setMap( map:GoogleMap? ) {
        marker?.remove()
        marker = null

        this.map = map

        display?.setMap( map )
    }

    private fun updateLocation( location:android.location.Location? ) {
        location ?: return

        if ( marker == null ) {
            val icon = BitmapDescriptorFactory.fromResource( R.drawable.markerme )
            val options = MarkerOptions()
                    .position( LatLng( location.latitude,location.longitude ) )
                    .icon( icon )
            marker = map?.addMarker( options )
        } else {
            marker?.position = LatLng( location.latitude,location.longitude )
        }
    }
}
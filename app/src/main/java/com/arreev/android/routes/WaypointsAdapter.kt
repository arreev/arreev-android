
package com.arreev.android.routes

import android.view.*
import android.graphics.*
import java.util.concurrent.*

import io.reactivex.*
import io.reactivex.android.schedulers.*

import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.R

/**
 * Created by jeffschulz on 6/22/18.
 */
class WaypointsAdapter( val view:View, val dataSource:DataSource<Waypoint> ) : WaypointsDisplay
{
    private val polylines = mutableListOf<Polyline>()
    private val markers = mutableListOf<Marker>()
    private var map: GoogleMap? = null
    private var numwaypoints = 0

    override var pageDelta:Int
        get() = 0
        set(value) {}

    override var pageSize:Int
        get() = 0
        set(value) {}

    override fun release() { _release() }

    override var listener:WaypointsDisplay.Listener?
        get() = listener
        set(value) {}

    override fun setItemCount( count:Int ) { _setItemCount( count ) }
    override fun setPosition( position:Int ) {}

    override fun setMap( m:GoogleMap? ) { _setMap( m ) }

    /**********************************************************************************************/

    private fun _release() {
        map = null
    }

    private fun _setItemCount( count:Int ) {
        numwaypoints = count
        update()
    }

    private fun _setMap( m:GoogleMap? ) {
        map = m
        update()
    }

    /*
     * https://developers.google.com/maps/documentation/android-sdk/intro
     * https://developers.google.com/maps/documentation/android-sdk/marker
     * https://developers.google.com/android/reference/com/google/android/gms/maps/package-summary
     */
    private fun update() {
        polylines.forEach( {
            l -> run {
                l.remove()
            }
        } )
        polylines.clear()

        markers.forEach( {
            m -> run {
                m.remove()
            }
        } )
        markers.clear()

        map ?: return

        /*
         * polylines
         */
        var prev: Waypoint? = null
        for ( n in 0 until numwaypoints ) {
            val w: Waypoint? = dataSource?.getDataFor( n )
            w ?: continue
            if ( validate( w ) ) {
                if ( prev != null ) {
                    val beg = LatLng(prev.latitude ?: 0.0,prev.longitude ?: 0.0 )
                    val end = LatLng(w.latitude ?: 0.0,w.longitude ?: 0.0 )
                    val o = PolylineOptions()
                            .add( beg,end )
                            .color( Color.BLUE )
                            .width(3F )
                    val p = map?.addPolyline( o )
                    if ( p != null ) polylines.add( p )
                }
                prev = w
            }
        }

        val iconvirgin = BitmapDescriptorFactory.fromResource( R.drawable.markerpurple )
        val iconvisited = BitmapDescriptorFactory.fromResource( R.drawable.markergreen )
        val iconterminal = BitmapDescriptorFactory.fromResource( R.drawable.markerred )

        /*
         * markers
         */
        for ( n in 0 until numwaypoints ) {
            val w: Waypoint? = dataSource?.getDataFor( n )
            w ?: continue
            if ( validate( w ) ) {
                val position = LatLng(w.latitude ?: 0.0,w.longitude ?: 0.0 )
                val title = w.name ?: ""
                val o = MarkerOptions()
                        .position( position )
                        .title( title )
                        .icon( if ( n == 0 ) iconvisited else if ( n == (numwaypoints-1) ) iconterminal else iconvirgin )
                val m = map?.addMarker( o )
                if ( m != null ) markers.add( m )
            }
        }

        rebounds()
    }

    private fun validate( w:Waypoint? ) : Boolean {
        var valid = true

        val lat = w?.latitude ?: 0.0
        val lng = w?.longitude ?: 0.0

        if ( (lat == 0.0) || (lng == 0.0) ) {
            valid =  false
        }

        return valid
    }

    private fun rebounds() {
        Observable.just("" )
                .delay( 250,TimeUnit.MILLISECONDS )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( {
                    if ( markers.size > 0 ) markers[0].showInfoWindow()
                    bounds()
                } )
    }

    private fun bounds() {
        if ( markers.size == 0 ) return

        try {
            val builder = LatLngBounds.Builder()
            markers.forEach( { m -> builder.include( m.position ) } )
            val bounds = builder.build()
            val cameraupdate = CameraUpdateFactory.newLatLngBounds( bounds,0 )
            map?.moveCamera( cameraupdate )
        } catch ( x:Throwable ) {
            dbg( x )
        }

        /*
         * https://developers.google.com/maps/documentation/android-sdk/views
         */
        Observable.just("" )
                .delay(750,TimeUnit.MILLISECONDS )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( {
                    val zoom = (map?.cameraPosition?.zoom ?: 12F) - .575F;
                    map?.animateCamera( CameraUpdateFactory.zoomTo( zoom ) )
                } )
    }
}
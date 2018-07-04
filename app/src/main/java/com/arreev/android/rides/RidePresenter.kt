
package com.arreev.android.rides

import javax.inject.*

import io.reactivex.*
import io.reactivex.disposables.*

import com.google.firebase.auth.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.api.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class RidePresenter : Presenter<RideDisplay>,DataSource<Ride>,RideDisplay.Listener
{
    private val rides = mutableListOf<Ride>()

    private var display: RideDisplay? = null
    private var disposable : Disposable?  = null
    private var firstVisiblePosition = 0
    private var total = 2500

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    init { _init() }

    override fun bind( d:RideDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Ride { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) { _getReadyFor( start,count ) }
    override fun requestRefresh() { _requestRefresh() }

    override fun setTrackingEnabled( ride:Ride ) { _setTrackingEnabled( ride ) }
    override fun setTrackingDisabled( ride:Ride ) { _setTrackingDisabled( ride ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject(this )

        state.observeClient().subscribe( { client:Client -> stateChange( client ) } )
    }

    private fun _bind(d:RideDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 0
        display?.pageSize = 0
        display?.setItemCount( rides.size )
        display?.setPosition( firstVisiblePosition )

        if ( rides.isEmpty() ) fetch(0,1 )
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null

        rides.clear()
    }

    private fun _release() {
        disposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        rides.clear()

        dbg("RidesPresenter.release" )
    }

    private fun _getDataFor( position:Int ):Ride {
        try {
            return rides[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Ride( id = "0" )
    }

    class Range( val start:Int,val count:Int )
    {
        fun same( start:Int,count:Int ) : Boolean {
            return (this.start == start) && (this.count == count)
        }
    }

    private var readyForRange = Range(0,0 )

    private fun _getReadyFor( start:Int,count:Int ) {
        val s = Math.max( start,0 )
        val c = Math.max( count,0 )
        if ( readyForRange.same( s,c ) ) { return }

        readyForRange = Range( s,c )
        val reach = Math.min( ( s + c ),total )
        if ( reach >= rides.size ) {
            fetch( s,c )
        }
    }

    private fun _requestRefresh() {}

    private fun _setTrackingEnabled( ride:Ride ) {
        ride.isTrackingEnabled.set( true )
        eventbus.send( StartTrackingServiceEvent( ride ) )
    }

    private fun _setTrackingDisabled( ride:Ride ) {
        ride.isTrackingEnabled.set( false )
        eventbus.send( StopTrackingServiceEvent( ride ) )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            TRANSPORTERID -> {
                display?.setItemCount( 0 )
                rides.clear()
                readyForRange = Range(0,0 )
                fetch( 0,1 )
            }
        }
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        disposable?.dispose()

        val transporterid = state.getTransporter()
        if ( transporterid == null ) { return }

        display?.busy = true
        disposable = FetchTransporter().fetch( ownerid ?: "",transporterid ?: "" )
                .flatMap { (id,name,imageURL) -> Observable.just( Ride( id,name,imageURL ) ) }
                .subscribe(
                { r:Ride -> onNext( r ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun update( ride:Ride ) {
        val trackingid = state.getTracking()
        ride.isTrackingEnabled.set( ride.id.equals( trackingid ) )

        ride.isFollowingEnabled.set( false )
    }

    private fun onNext( r:Ride ) {
        update( r )
        val indexof = rides.indexOf( r )
        when ( indexof ) {
            -1 -> { rides.add( r ) }
            else -> { rides[ indexof ] = r }
        }
    }

    private fun onError() {
        readyForRange = Range(0,0 )
        eventbus.send( NetworkErrorEvent() )
        display?.busy = false
    }

    private fun onComplete() {
        display?.setItemCount( rides.size )
        display?.busy = false
    }
}
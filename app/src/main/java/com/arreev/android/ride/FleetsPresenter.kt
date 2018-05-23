
package com.arreev.android.ride

import javax.inject.*

import com.google.firebase.auth.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.api.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class FleetsPresenter : Presenter<FleetsDisplay>,DataSource<Fleet>,FleetsDisplay.Listener
{
    private val fleets = mutableListOf<Fleet>()

    private var display: FleetsDisplay? = null
    private var firstVisiblePosition = 0
    private var total = 2500

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    init { _init() }

    override fun bind( d:FleetsDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Fleet { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) { _getReadyFor( start,count ) }
    override fun requestRefresh() { _requestRefresh() }

    override fun onFleet( fleet:Fleet ) { _onFleet( fleet ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject( this )
    }

    private fun _bind(d:FleetsDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 15
        display?.pageSize = 50
        display?.setItemCount( fleets.size )
        display?.setPosition( firstVisiblePosition )

        if ( fleets.isEmpty() ) fetch(0,100 )
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null
    }

    private fun _release() {
        display?.listener = null
        display?.release()
        display = null

        fleets.clear()

        dbg("FleetsPresenter.release" )
    }

    private fun _getDataFor( position:Int ):Fleet {
        try {
            return fleets[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Fleet( id = "0" )
    }

    class Range( val start:Int,val count:Int )
    {
        fun same( start:Int,count:Int ) : Boolean {
            return (this.start == start) && (this.count == count)
        }
    }

    private var readyForRange = Range(-1,-1 )

    private fun _getReadyFor( start:Int,count:Int ) {
        val s = Math.max( start,0 )
        val c = Math.max( count,0 )
        if ( readyForRange.same( s,c ) ) { return }

        readyForRange = Range( s,c )
        val reach = Math.min( ( s + c ),total )
        if ( reach >= fleets.size ) {
            fetch( s,c )
        }
    }

    private fun _requestRefresh() {}

    private fun _onFleet( fleet:Fleet ) {
        state.setFleet( fleet.id )
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        FetchFleets().fetch( ownerid ?: "", start,count ).subscribe(
                { f:Fleet -> onNext( f ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun onNext( f:Fleet ) {
        val indexof = fleets.indexOf( f )
        when ( indexof ) {
            -1 -> { fleets.add( f ) }
            else -> { fleets[ indexof ] = f }
        }
    }

    private fun onError() {
        readyForRange = Range(-1,-1 )
        eventbus.send( NetworkErrorEvent() )
    }

    private fun onComplete() {
        display?.setItemCount( fleets.size )
    }
}

package com.arreev.android.rides

import javax.inject.*

import io.reactivex.disposables.*

import com.google.firebase.auth.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.api.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class TransportersPresenter : Presenter<TransportersDisplay>,DataSource<Transporter>,TransportersDisplay.Listener
{
    private val transporters = mutableListOf<Transporter>()

    private var display: TransportersDisplay? = null
    private var disposable : Disposable?  = null
    private var firstVisiblePosition = 0
    private var total = 2500

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    init { _init() }

    override fun bind( d:TransportersDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Transporter { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) { _getReadyFor( start,count ) }
    override fun requestRefresh() { _requestRefresh() }

    override fun onTransporter( transporter:Transporter ) { _onTransporter( transporter ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject( this )

        state.observeClient().subscribe( { client:Client -> stateChange( client ) } )
    }

    private fun _bind(d:TransportersDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 15
        display?.pageSize = 50
        display?.setItemCount( transporters.size )
        display?.setPosition( firstVisiblePosition )

        if ( transporters.isEmpty() ) fetch(0,100 )
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null
    }

    private fun _release() {
        disposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        transporters.clear()

        dbg("TransportersPresenter.release" )
    }

    private fun _getDataFor( position:Int ):Transporter {
        try {
            return transporters[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Transporter( id = "0" )
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
        if ( reach >= transporters.size ) {
            fetch( s,c )
        }
    }

    private fun _requestRefresh() {}

    private fun _onTransporter( transporter:Transporter ) {
        state.setTransporter( transporter.id )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            FLEETID -> {
                display?.setItemCount( 0 )
                transporters.clear()
                readyForRange = Range(-1,-1 )
                fetch( 0,100 )
            }
        }
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        disposable?.dispose()

        val fleetid = state.getFleet()
        if ( fleetid == null ) { return }

        disposable = FetchTransporters().fetch(ownerid ?: "",fleetid ?: "", start,count ).subscribe(
                { t:Transporter -> onNext( t ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun onNext( t:Transporter ) {
        val indexof = transporters.indexOf( t )
        when ( indexof ) {
            -1 -> { transporters.add( t ) }
            else -> { transporters[ indexof ] = t }
        }
    }

    private fun onError() {
        readyForRange = Range(-1,-1 )
        eventbus.send( NetworkErrorEvent() )
    }

    private fun onComplete() {
        display?.setItemCount( transporters.size )
    }
}
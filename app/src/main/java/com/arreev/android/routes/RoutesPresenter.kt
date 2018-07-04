
package com.arreev.android.routes

import javax.inject.*

import io.reactivex.disposables.*

import com.google.firebase.auth.*
import com.google.firebase.database.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.api.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class RoutesPresenter : Presenter<RoutesDisplay>,DataSource<Route>,RoutesDisplay.Listener
{
    private val routes = mutableListOf<Route>()

    private var display: RoutesDisplay? = null
    private var disposable : Disposable?  = null
    private var firstVisiblePosition = 0
    private var total = 2500

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    private val enroutevaluelistener = object : ValueEventListener {
        override fun onDataChange( snapshot:DataSnapshot ) {
            if ( snapshot?.value is String ) {
                val routeid = snapshot?.value as String
                enroute( routeid )
            }
        }
        override fun onCancelled( error:DatabaseError ) { dbg( error ) }
    }

    init { _init() }

    override fun bind( d:RoutesDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Route { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) { _getReadyFor( start,count ) }
    override fun requestRefresh() { _requestRefresh() }

    override fun onRoute( route:Route ) { _onRoute( route) }
    override fun setEnRoute( active:Boolean ) { _setEnRoute( active ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject(this )

        state.observeClient().subscribe( { client:Client -> stateChange( client ) } )
    }

    private fun _bind(d:RoutesDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 0
        display?.pageSize = 0
        display?.setItemCount( routes.size )
        display?.setPosition( firstVisiblePosition )

        if ( routes.isEmpty() ) fetch(0,1 )
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        rmvEnRouteValueEventListener()

        display?.listener = null
        display?.release()
        display = null

        routes.clear()
    }

    private fun _release() {
        disposable?.dispose()

        rmvEnRouteValueEventListener()

        display?.listener = null
        display?.release()
        display = null

        routes.clear()

        dbg("RoutesPresenter.release" )
    }

    private fun _getDataFor( position:Int ):Route {
        try {
            return routes[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Route( id = "0" )
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
        if ( reach >= routes.size ) {
            fetch( s,c )
        }
    }

    private fun _requestRefresh() {}

    private fun _onRoute( route:Route ) {
        state.setRoute( route.id )
    }

    private fun _setEnRoute( active:Boolean ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        val routeid = state.getRoute()

        val reference = FirebaseDatabase.getInstance().getReference("enroutes" )
                .child("$ownerid" )
                .child("enroute" )
                .setValue( if ( active ) routeid else null )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            ROUTEID -> {
                rmvEnRouteValueEventListener()
                addEnRouteValueEventListener()
            }
        }
    }

//    private fun createEnRouteReferenceIfNecessary() {
//        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
//        val routeid = state.getRoute()
//
//        val reference = FirebaseDatabase.getInstance().getReference("enroutes" )
//                .child("$ownerid" )
//                .child("$routeid" )
//                .child("active" )
//
//        reference.addListenerForSingleValueEvent( object : ValueEventListener {
//            override fun onDataChange( snapshot:DataSnapshot? ) {
//                val value = snapshot?.value ?: null
//                if ( value == null ) {
//                    reference.setValue( false )
//                }
//            }
//            override fun onCancelled( error:DatabaseError? ) {}
//        } )
//    }

    private fun addEnRouteValueEventListener() {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseDatabase.getInstance().getReference("enroutes" )
                .child("$ownerid" )
                .child("enroute" )
                .addValueEventListener( enroutevaluelistener )
    }

    private fun rmvEnRouteValueEventListener() {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseDatabase.getInstance().getReference("enroutes" )
                .child("$ownerid" )
                .child("enroute" )
                .removeEventListener( enroutevaluelistener )
    }

    private fun enroute( routeid:String? ) {
        val active = if ( routeid != null ) routeid.equals( state.getRoute() ) else false
        display?.showEnRoute( active )
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        display?.busy = true
        FetchRoutes().fetch(ownerid ?: "", start,count ).subscribe(
                { r:Route -> onNext( r ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun onNext( r:Route ) {
        val indexof = routes.indexOf( r )
        when ( indexof ) {
            -1 -> { routes.add( r ) }
            else -> { routes[ indexof ] = r }
        }
    }

    private fun onError() {
        readyForRange = Range(0,0 )
        eventbus.send( NetworkErrorEvent() )
        display?.busy = false
    }

    private fun onComplete() {
        display?.setItemCount( routes.size )
        display?.busy = false
    }
}
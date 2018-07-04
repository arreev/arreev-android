
package com.arreev.android.routes

import android.os.*
import android.view.*
import javax.inject.*

import com.google.android.gms.maps.*

import io.reactivex.disposables.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.R
import com.arreev.android.databinding.RoutesfragmentBinding

/**
 * Created by jeffschulz on 6/21/18.
 */
class RoutesFragment : android.support.v4.app.Fragment()
{
    @Inject lateinit var routesPresenter: RoutesPresenter
    @Inject lateinit var waypointsPresenter: WaypointsPresenter
    @Inject lateinit var state: State

    var showcurrentroute = android.databinding.ObservableBoolean(false )
    var showroutes = android.databinding.ObservableBoolean(false )

    private var routesfragmentview : View? = null
    private var disposable : Disposable? = null

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        ArreevApplication.appComponent.inject(this )
    }

    override fun onCreateView( inflater:LayoutInflater?,container:ViewGroup?,savedInstanceState:Bundle?) : View? {
        routesfragmentview = inflater?.inflate( R.layout.routesfragment,container,false );

        val binding = android.databinding.DataBindingUtil.bind<RoutesfragmentBinding>( routesfragmentview  )
        binding.routesfragment = this

        routesPresenter.bind( RoutesAdapter(routesfragmentview ?: View( context ),routesPresenter ) )
        waypointsPresenter.bind( WaypointsAdapter(routesfragmentview ?: View( context ),waypointsPresenter ) )

        disposable = state.observeClient().subscribe( { client:Client -> stateChange( client ) } )

        val mapfragment = childFragmentManager.findFragmentById( R.id.currentroutemapfragment ) as SupportMapFragment
        mapfragment.getMapAsync { m -> mapReady( m ) }

        return routesfragmentview
    }

    override fun onStart() {
        super.onStart()

        if ( state.getTransporter() != null ) {
            showcurrentroute.set( true )
            showroutes.set( false )
        }
    }

    fun toggleRoutes() {
        showcurrentroute.set( false )
        showroutes.set( !showroutes.get() )
    }

    fun toggleCurrentRoute() {
        showroutes.set( false )
        showcurrentroute.set( !showcurrentroute.get() )
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable?.dispose()

        routesPresenter.unbind()
        waypointsPresenter.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()

        val changingConfigurations = activity?.isChangingConfigurations ?: false
        if ( !changingConfigurations ) {
            routesPresenter.release()
            waypointsPresenter.release()
        }
    }

    private fun mapReady( m:GoogleMap ) {
        waypointsPresenter?.setMap( m )
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            ROUTEID -> { focusCurrentRoute() }
        }
    }

    private fun focusRoutes() {
        showcurrentroute.set( false )
        showroutes.set( false )
    }

    private fun focusCurrentRoute() {
        showcurrentroute.set( true )
        showroutes.set( false )
    }
}
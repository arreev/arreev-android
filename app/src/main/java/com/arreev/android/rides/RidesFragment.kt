
package com.arreev.android.rides

import javax.inject.*

import android.os.*
import android.view.*

import io.reactivex.disposables.*

import com.arreev.android.*
import com.arreev.android.databinding.RidesfragmentBinding

/**
 * Created by jeffschulz on 5/15/18.
 */
class RidesFragment : android.support.v4.app.Fragment()
{
    @Inject lateinit var ridePresenter : RidePresenter
    @Inject lateinit var transportersPresenter : TransportersPresenter
    @Inject lateinit var fleetsPresenter : FleetsPresenter
    @Inject lateinit var state : State

    private var ridefragmentview : View? = null
    private var disposable : Disposable? = null

    var showcurrentride = android.databinding.ObservableBoolean(false )
    var showstables = android.databinding.ObservableBoolean(false )
    var showrides = android.databinding.ObservableBoolean(false )

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        ArreevApplication.appComponent.inject(this )
    }

    override fun onCreateView(inflater:LayoutInflater?, container:ViewGroup?, savedInstanceState:Bundle? ) : View? {
        ridefragmentview = inflater?.inflate( R.layout.ridesfragment,container,false );

        val binding = android.databinding.DataBindingUtil.bind<RidesfragmentBinding>( ridefragmentview  )
        binding.ridefragment = this

        ridePresenter.bind( RideAdapter(ridefragmentview ?: View( context ),ridePresenter ) )
        fleetsPresenter.bind( FleetsAdapter(ridefragmentview ?: View( context ),fleetsPresenter ) )
        transportersPresenter.bind( TransportersAdapter(ridefragmentview ?: View( context ),transportersPresenter ) )

        disposable = state.observeClient().subscribe( { client:Client -> stateChange( client ) } )

        return ridefragmentview
    }

    override fun onStart() {
        super.onStart()

        if ( state.getTransporter() != null ) {
            showcurrentride.set( true )
            showstables.set( false )
            showrides.set( false )
        }
    }

    fun toggleCurrentRide() {
        showcurrentride.set( !showcurrentride.get() )
        showstables.set( false )
        showrides.set( false )
    }

    fun toggleStables() {
        showcurrentride.set( false )
        showstables.set( !showstables.get() )
        showrides.set( false )
    }

    fun toggleRides() {
        showcurrentride.set( false )
        showstables.set( false )
        showrides.set( !showrides.get() )
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable?.dispose()

        ridePresenter.unbind()
        fleetsPresenter.unbind()
        transportersPresenter.unbind()

    }

    override fun onDestroy() {
        super.onDestroy()
        val changingConfigurations = activity?.isChangingConfigurations ?: false
        if ( !changingConfigurations ) {
            ridePresenter.release()
            fleetsPresenter.release()
            transportersPresenter.release()
        }
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            FLEETID -> {
                focusRides()
            }
            TRANSPORTERID -> {
                focusCurrentRide()
            }
        }
    }

    private fun focusRides() {
        showcurrentride.set( false )
        showstables.set( false )
        showrides.set( true )
    }

    private fun focusCurrentRide() {
        showcurrentride.set( true )
        showstables.set( false )
        showrides.set( false )
    }
}
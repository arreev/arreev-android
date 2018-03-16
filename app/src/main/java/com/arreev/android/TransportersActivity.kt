
package com.arreev.android

import android.content.Intent
import javax.inject.*

import android.os.*
import android.view.*
import android.widget.*
import android.support.v7.app.*
import io.reactivex.disposables.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class TransportersActivity : AppCompatActivity()
{
    @Inject lateinit var eventBus:EventBus
    @Inject lateinit var transportersPresenter: TransportersPresenter

    private var eventDisposable: Disposable? = null
    private var fleetid: String? = null

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("  TransportersActivity.onCreate" )

        ArreevApplication.appComponent.inject(this )
        fleetid = intent?.getStringExtra("fleetid" )

        setContentView( R.layout.transporters)
        setSupportActionBar( findViewById( R.id.maintoolbar ) )
    }

    override fun onStart() {
        super.onStart()
        dbg("  TransportersActivity.onStart" )

        val transporters = findViewById<View>( R.id.transporters )
        transportersPresenter.forFleet(fleetid ?: "" ).bind( TransportersAdapter( transporters,transportersPresenter ) )

        eventDisposable = eventBus.observable( Any::class.java ).subscribe( { e -> event( e ) } )
    }

    override fun onStop() {
        super.onStop()

        eventDisposable?.dispose()
        eventDisposable = null

        transportersPresenter.unbind()

        dbg("  TransportersActivity.onStop" )
    }

    override fun onDestroy() {
        super.onDestroy()

        eventDisposable?.dispose()
        eventDisposable = null

        transportersPresenter.release()

        dbg("  TransportersActivity.onDestroy" )
    }

    private fun event( e:Any ) {
        when ( e ) {
            is DatabaseClientConnectionEvent -> {
                val connectedtextview = findViewById<TextView>( R.id.connected )
                connectedtextview.text = if ( e.connected ) "connected" else "dis-connected"
            }
            is GoTransporterEvent -> {
                val intent = Intent( this, TransportingActivity::class.java )
                intent.putExtra( "transporterid",e.transporterid )
                startActivity( intent )
            }
        }
    }
}
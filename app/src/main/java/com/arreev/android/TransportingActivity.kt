
package com.arreev.android

import javax.inject.*

import android.os.*
import android.net.*
import android.view.*
import android.widget.*
import android.content.pm.*
import com.squareup.picasso.*
import android.support.v7.app.*
import android.support.v4.app.*
import com.arreev.android.model.*
import io.reactivex.disposables.*
import android.support.v4.content.*

import android.support.v7.widget.SwitchCompat
import kotlinx.android.synthetic.main.transporters.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class TransportingActivity : AppCompatActivity()
{
    @Inject lateinit var transporting: Transporting
    @Inject lateinit var eventBus: EventBus

    private val REQUEST_LOCATION_PERMISSONS = 100;

    private var fetchSubscription: Disposable? = null
    private var eventSubscription: Disposable? = null
    private var transporterid: String? = null

    private var transportingswitch: SwitchCompat? = null
    private var latitudetextview: TextView? = null
    private var longitudetextview: TextView? = null

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("    TransportingActivity.onCreate" )

        ArreevApplication.appComponent.inject(this )
        transporterid = intent?.getStringExtra("transporterid" )

        setContentView( R.layout.transporting )
        setSupportActionBar( findViewById( R.id.maintoolbar ) )

        transportingswitch = findViewById<SwitchCompat>( R.id.transportingswitch )
        latitudetextview = findViewById<TextView>( R.id.latitude )
        longitudetextview = findViewById<TextView>( R.id.longitude )
    }

    override fun onStart() {
        super.onStart()
        dbg("    TransportingActivity.onStart" )

        eventSubscription = eventBus.observable( Any::class.java ).subscribe( { a -> onEvent( a ) } )

        val switch = findViewById<SwitchCompat>( R.id.transportingswitch )
        switch.isChecked = transporting.isTransporting( transporterid )

        fetchSubscription = FetchTransporter().fetch(transporterid ?: "" ).subscribe( { t -> assume( t ) } )
    }

    override fun onRequestPermissionsResult( requestCode:Int, permissions:Array<out String>, grantResults:IntArray ) {
        when ( requestCode ) {
            REQUEST_LOCATION_PERMISSONS -> {
                val granted = ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                when ( granted ) {
                    true -> activate()
                    else -> deactivate()
                }
            }
        }
    }

    fun onToggleTransporting( view:View ) {
        val switch = findViewById<SwitchCompat>( R.id.transportingswitch )
        when ( switch.isChecked ) {
            true -> { preactivate() }
            false -> { transporting.cancel() }
        }
    }

    override fun onStop() {
        super.onStop()

        fetchSubscription?.dispose()
        fetchSubscription = null

        eventSubscription?.dispose()
        eventSubscription = null

        dbg("    TransportingActivity.onStop" )
    }

    override fun onDestroy() {
        super.onDestroy()

        dbg("    TransportingActivity.onDestroy" )
    }

    private fun assume( transporter:Transporter ) {
        val transporterImageView = findViewById<ImageView>( R.id.transportingimageview )
        val uri = Uri.parse( transporter.imageURL )
        Picasso.with(this )
                .load( uri )
                .into( transporterImageView )

        val transporterIDView = findViewById<TextView>( R.id.transportingidview )
        transporterIDView.text = transporter.id

        val transporterNameView = findViewById<TextView>( R.id.transportingnameview )
        transporterNameView.text = transporter.name
    }

    private fun onEvent( a:Any ) {
        when ( a ) {
            is DatabaseClientConnectionEvent -> {
                val connectedtextview = findViewById<TextView>( R.id.connected )
                connectedtextview.text = if ( a.connected ) "connected" else "dis-connected"
            }
            is UpdateTransporterLocationEvent -> {
                if ( a.transporterid == transporterid ) {
                    latitudetextview?.text = a.lat?.toString()
                    longitudetextview?.text = a.lng?.toString()
                }
            }
        }
    }

    private fun preactivate() {
        transportingswitch?.isEnabled = false

        val permission = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION )
        when ( permission ) {
            PackageManager.PERMISSION_GRANTED -> { activate() }
            else -> {
                val permissions = arrayOf( android.Manifest.permission.ACCESS_COARSE_LOCATION )
                ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSONS )
            }
        }
    }

    private fun activate() {
        transportingswitch?.isEnabled = true

        val id = transporterid
        if ( id != null ) {
            transporting.activate(this,id )
        }
    }

    private fun deactivate() {
        transportingswitch?.isEnabled = true
        transportingswitch?.isChecked = false

        transporting.cancel()
    }
}

package com.arreev.android

import javax.inject.*

import com.arreev.android.model.*
import io.reactivex.disposables.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class TransportersPresenter : Presenter<TransportersDisplay>,DataSource<Transporter>,TransportersDisplay.Listener
{
    @Inject lateinit var account:Account
    @Inject lateinit var eventBus:EventBus
    @Inject lateinit var transporting:Transporting

    private var fetchDisposable: Disposable? = null
    private var firstVisiblePosition : Int = 0
    private var display: TransportersDisplay? = null
    private var fleetid: String? = null
    private var total: Int = 0

    private val transporters = mutableListOf<Transporter>()

    init {
        ArreevApplication.appComponent.inject(this )
    }

    fun forFleet( fleetid:String ) : TransportersPresenter {
        this.fleetid = fleetid
        return this
    }

    override fun bind( d:TransportersDisplay ) {
        display = d

        display?.listener = this
        display?.setItemCount( transporters.size )
        display?.setPosition( firstVisiblePosition )

        if ( transporters.isEmpty() ) {
            fetch(0,100 )
        } else {
            assess()
        }
    }

    override fun unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null
    }

    override fun release() {
        fetchDisposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        transporters.clear()
    }

    override fun getDataFor( position:Int ):Transporter {
        try {
            return transporters[ position ]
        } catch ( x:Exception ) {
            err( x )
        }
        return Transporter()
    }

    override fun getReadyFor( start:Int, count:Int ) {
        val s = Math.max( start,0 );
        val c = Math.max( count,0 );
        val reach = ( s + c );
        if ( (reach < total) && (reach >= transporters.size) ) {
            fetch( s,c );
        }
    }

    override fun requestRefresh() {}

    override fun onTransporter( transporterid:String ) {
        eventBus.send( GoTransporterEvent( transporterid ) )
    }

    fun update( transporterid:String, lat:Double,lng:Double ) {

    }

    private fun fetch(start:Int, count:Int ) {
        val ownerid = account.ownerid ?: ""
        val _transporters = mutableListOf<Transporter>()

        fetchDisposable = FetchTransporters().fetch( ownerid,fleetid ?: "", start,count ).subscribe(
                { transporter -> _transporters.add( transporter ) },
                { error -> {} },
                { run{
                        total = start + count;
                        if ( total > transporters.size ) {
                            total = transporters.size;
                        }
                        update( _transporters )
                    }
                }
        )
    }

    private fun update( _transporters:List<Transporter> ) {
        transporters.clear()
        transporters.addAll( _transporters )
        display?.setItemCount( transporters.size )
        assess()
    }

    private fun assess() {
        val transporting = transporters.filter { transporting.isTransporting( it.id ) }
        display?.transportingid = if ( transporting?.size > 0 ) transporting[0].id else ""
    }
}

package com.arreev.android

import javax.inject.*

import com.arreev.android.model.*
import io.reactivex.disposables.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class FleetsPresenter : Presenter<FleetsDisplay>,DataSource<Fleet>,FleetsDisplay.Listener
{
    @Inject lateinit var account:Account
    @Inject lateinit var eventBus:EventBus

    private var fetchDisposable: Disposable? = null
    private var firstVisiblePosition : Int = 0
    private var display: FleetsDisplay? = null
    private var total: Int = 0

    private val fleets = mutableListOf<Fleet>()

    init {
        ArreevApplication.appComponent.inject(this )
    }

    override fun bind( d:FleetsDisplay) {
        display = d

        display?.listener = this
        display?.setItemCount( fleets.size )
        display?.setPosition( firstVisiblePosition )

        if ( fleets.isEmpty() ) {
            fetch(0, 100)
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

        fleets.clear()
    }

    override fun getDataFor( position:Int ):Fleet {
        try {
            return fleets[ position ]
        } catch ( x:Exception ) {
            err( x )
        }
        return Fleet()
    }

    override fun getReadyFor( start:Int,count:Int ) {
        val s = Math.max( start,0 );
        val c = Math.max( count,0 );
        val reach = ( s + c );
        if ( (reach < total) && (reach >= fleets.size) ) {
            fetch( s,c );
        }
    }
    override fun requestRefresh() {}

    override fun onFleet( fleetid:String ) {
        eventBus.send( GoFleetEvent( fleetid ) )
    }

    private fun fetch(start:Int, count:Int ) {
        val ownerid = account.ownerid ?: ""
        val _fleets = mutableListOf<Fleet>()

        fetchDisposable = FetchFleets().fetch( ownerid,start,count ).subscribe(
                { fleet -> _fleets.add( fleet ) },
                { error -> {} },
                { run{
                        total = start + count;
                        if ( total > fleets.size ) {
                            total = fleets.size;
                        }
                        update( _fleets )
                    }
                }
        )
    }

    private fun update( _fleets:List<Fleet> ) {
        fleets.clear()
        fleets.addAll( _fleets )
        display?.setItemCount( fleets.size )
    }
}

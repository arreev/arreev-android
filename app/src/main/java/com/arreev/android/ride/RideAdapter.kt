
package com.arreev.android.ride

import java.util.concurrent.*

import android.view.*
import com.arreev.android.*
import com.arreev.android.databinding.RideBinding

import io.reactivex.*
import io.reactivex.android.schedulers.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class RideAdapter(val view:View, val dataSource:DataSource<Ride>) : RideDisplay
{
    private var linearLayoutManager:android.support.v7.widget.LinearLayoutManager? = null
    private var recyclerView : android.support.v7.widget.RecyclerView? = null
    private var _listener: RideDisplay.Listener? = null
    private var _pageDelta = 25
    private var _pageSize = 100

    private val rideAdapter = RideAdapter()

    init { _init() }

    override var pageDelta:Int
        get() = _pageDelta
        set(value) { _pageDelta = value }

    override var pageSize:Int
        get() = _pageSize
        set(value) { _pageSize = value }

    override var listener:RideDisplay.Listener?
        get() = _listener
        set(value) { _listener = value }

    override var firstVisiblePosition:Int
        get() = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
        set(value) {}

    override fun release() { _release() }
    override fun setItemCount( count:Int ) { _setItemCount( count ) }
    override fun setPosition( position:Int ) { _setPosition( position ) }

    /**********************************************************************************************/

    private fun _init() {
        linearLayoutManager = android.support.v7.widget.LinearLayoutManager( view.context,android.support.v7.widget.LinearLayoutManager.VERTICAL,false )

        rideAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<android.support.v7.widget.RecyclerView>( R.id.riderecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = rideAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    private fun _release() {}

    private fun _setItemCount( count:Int ) {
        val positionStart = rideAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        rideAdapter?.itemCount = count
        rideAdapter?.notifyItemRangeInserted( positionStart,itemCount )

        Observable.just("" ).delay(100, TimeUnit.MILLISECONDS )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe(
                        {
                            view.parent.requestLayout()
                            view.requestLayout()
                            view.requestFocus()
                            recyclerView?.requestLayout()
                            recyclerView?.requestFocus()
                        }
                )
    }

    private fun _setPosition( position:Int ) {
        recyclerView?.scrollToPosition( position )
    }

    private fun setupScrolling() {
        val listener = object : android.support.v7.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled( recyclerView:android.support.v7.widget.RecyclerView?, dx:Int,dy:Int ) {
                val childCount = linearLayoutManager?.childCount ?: 0
                val itemCount = linearLayoutManager?.itemCount ?: 0
                val firstVisibleItem = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
                val reach = ( firstVisibleItem + childCount )
                val delta = Math.abs( reach - itemCount )
                if ( delta < _pageDelta ) {
                    dataSource?.getReadyFor( itemCount,_pageSize )
                }
            }
        }
        recyclerView?.addOnScrollListener( listener )
    }

    /*
     *
     */
    private inner class RideHolder( view:View ) : android.support.v7.widget.RecyclerView.ViewHolder( view ) {
        fun bind( ride:Ride ) {
            val binding:RideBinding = android.databinding.DataBindingUtil.bind( itemView )
            binding.ride = ride

            val rideImageView = itemView.findViewById<android.widget.ImageView>( R.id.rideimageview )
            rideImageView.setOnClickListener { }

            rideImageView.setImageDrawable( null )
            if ( ride.imageURL != null ) {
                val uri = android.net.Uri.parse( ride.imageURL )
                com.squareup.picasso.Picasso.get()
                        .load( uri )
                        .into( rideImageView )
            }

            val rideturntrackingon = itemView.findViewById<View>( R.id.rideturntrackingon )
            val rideturntrackingoff = itemView.findViewById<View>( R.id.rideturntrackingoff )
            rideturntrackingon.setOnClickListener { _listener?.setTrackingEnabled( ride )  }
            rideturntrackingoff.setOnClickListener { _listener?.setTrackingDisabled( ride )  }
        }
    }

    /*
     *
     */
    private inner class RideAdapter : android.support.v7.widget.RecyclerView.Adapter<RideHolder>()
    {
        var dataSource : DataSource<Ride>? = null

        private var itemCount = 0

        override fun getItemCount(): Int {
            return itemCount
        }

        fun setItemCount( itemCount:Int ) {
            this.itemCount = itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : RideHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.ride,parent,false )
            return RideHolder( view )
        }

        override fun onBindViewHolder( holder:RideHolder?,position:Int ) {
            val ride = dataSource?.getDataFor( position ) ?: Ride( id="0" )
            holder?.bind( ride )
        }
    }
}

package com.arreev.android.ride

import android.view.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.databinding.FleetBinding

/**
 * Created by jeffschulz on 5/15/18.
 */
class FleetsAdapter(val view:View, val dataSource:DataSource<Fleet>) : FleetsDisplay
{
    private var linearLayoutManager:android.support.v7.widget.LinearLayoutManager? = null
    private var recyclerView : android.support.v7.widget.RecyclerView? = null
    private var _listener: FleetsDisplay.Listener? = null
    private var _pageDelta = 25
    private var _pageSize = 100

    private val fleetAdapter = FleetAdapter()

    init { _init() }

    override var pageDelta:Int
        get() = _pageDelta
        set(value) { _pageDelta = value }

    override var pageSize:Int
        get() = _pageSize
        set(value) { _pageSize = value }

    override var listener:FleetsDisplay.Listener?
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
        linearLayoutManager = android.support.v7.widget.GridLayoutManager( view.context,2 )

        fleetAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<android.support.v7.widget.RecyclerView>( R.id.fleetsrecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = fleetAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    private fun _release() {}

    private fun _setItemCount( count:Int ) {
        val positionStart = fleetAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        fleetAdapter?.itemCount = count
        fleetAdapter?.notifyItemRangeInserted( positionStart,itemCount )
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
    private inner class FleetHolder( view:View ) : android.support.v7.widget.RecyclerView.ViewHolder( view ) {
        fun bind( fleet:Fleet ) {
            val binding:FleetBinding = android.databinding.DataBindingUtil.bind( itemView )
            binding.fleet = fleet

            val fleetImageView = itemView.findViewById<android.widget.ImageView>( R.id.fleetimageview )
            fleetImageView.setOnClickListener { _listener?.onFleet( fleet ) }

            fleetImageView.setImageDrawable( null )
            if ( fleet.imageURL != null ) {
                val uri = android.net.Uri.parse( fleet.imageURL )
                com.squareup.picasso.Picasso.get()
                        .load( uri )
                        .into( fleetImageView )
            }
        }
    }

    /*
     *
     */
    private inner class FleetAdapter : android.support.v7.widget.RecyclerView.Adapter<FleetHolder>()
    {
        var dataSource : DataSource<Fleet>? = null

        private var itemCount = 0

        override fun getItemCount(): Int {
            return itemCount
        }

        fun setItemCount( itemCount:Int ) {
            this.itemCount = itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : FleetHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.fleet,parent,false )
            return FleetHolder( view )
        }

        override fun onBindViewHolder( holder:FleetHolder?,position:Int ) {
            val fleet = dataSource?.getDataFor( position ) ?: Fleet( id="0" )
            holder?.bind( fleet )
        }
    }
}
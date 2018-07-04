
package com.arreev.android.rides

import java.util.concurrent.*

import android.view.*
import com.arreev.android.*
import com.arreev.android.databinding.TransporterBinding

import io.reactivex.*
import io.reactivex.android.schedulers.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
class TransportersAdapter(val view:View, val dataSource:DataSource<Transporter>) : TransportersDisplay
{
    private var linearLayoutManager:android.support.v7.widget.LinearLayoutManager? = null
    private var recyclerView : android.support.v7.widget.RecyclerView? = null
    private var _listener: TransportersDisplay.Listener? = null
    private var _pageDelta = 25
    private var _pageSize = 100

    private val transporterAdapter = TransporterAdapter()

    init { _init() }

    override var pageDelta:Int
        get() = _pageDelta
        set(value) { _pageDelta = value }

    override var pageSize:Int
        get() = _pageSize
        set(value) { _pageSize = value }

    override var listener:TransportersDisplay.Listener?
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

        transporterAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<android.support.v7.widget.RecyclerView>( R.id.transportersrecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = transporterAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    private fun _release() {}

    private fun _setItemCount( count:Int ) {
        val positionStart = transporterAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        transporterAdapter?.itemCount = count
        transporterAdapter?.notifyItemRangeInserted( positionStart,itemCount )

        Observable.just("" ).delay( 100, TimeUnit.MILLISECONDS )
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
    private inner class TransporterHolder( view:View ) : android.support.v7.widget.RecyclerView.ViewHolder( view ) {
        fun bind( transporter:Transporter ) {
            val binding:TransporterBinding = android.databinding.DataBindingUtil.bind( itemView )
            binding.transporter = transporter

            val transporterImageView = itemView.findViewById<android.widget.ImageView>( R.id.transporterimageview )
            transporterImageView.setOnClickListener { _listener?.onTransporter( transporter ) }

            transporterImageView.setImageDrawable( null )
            if ( transporter.imageURL != null ) {
                val uri = android.net.Uri.parse( transporter.imageURL )
                com.squareup.picasso.Picasso.get()
                        .load( uri )
                        .into( transporterImageView )
            }
        }
    }

    /*
     *
     */
    private inner class TransporterAdapter : android.support.v7.widget.RecyclerView.Adapter<TransporterHolder>()
    {
        var dataSource : DataSource<Transporter>? = null

        private var itemCount = 0

        override fun getItemCount(): Int {
            return itemCount
        }

        fun setItemCount( itemCount:Int ) {
            this.itemCount = itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : TransporterHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.transporter,parent,false )
            return TransporterHolder( view )
        }

        override fun onBindViewHolder( holder:TransporterHolder?,position:Int ) {
            val transporter = dataSource?.getDataFor( position ) ?: Transporter( id="0" )
            holder?.bind( transporter )
        }
    }
}
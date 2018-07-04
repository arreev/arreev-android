
package com.arreev.android.routes

import android.view.*
import android.widget.*
import java.util.concurrent.*

import io.reactivex.*
import io.reactivex.android.schedulers.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.databinding.RouteBinding

/**
 * Created by jeffschulz on 5/15/18.
 */
class RoutesAdapter( val view:View, val dataSource:DataSource<Route> ) : RoutesDisplay
{
    private var linearLayoutManager:android.support.v7.widget.LinearLayoutManager? = null
    private var recyclerView : android.support.v7.widget.RecyclerView? = null
    private var _listener: RoutesDisplay.Listener? = null
    private var routeactivate: CheckBox? = null
    private var _pageDelta = 25
    private var _pageSize = 100
    private var _busy = false

    private val routeAdapter = RouteAdapter()

    init { _init() }

    override var pageDelta:Int
        get() = _pageDelta
        set(value) { _pageDelta = value }

    override var pageSize:Int
        get() = _pageSize
        set(value) { _pageSize = value }

    override var listener:RoutesDisplay.Listener?
        get() = _listener
        set(value) { _listener = value }

    override var firstVisiblePosition:Int
        get() = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
        set(value) {}

    override var busy:Boolean
        get() = _busy
        set( value ) { _busy = value; _setBusy( _busy ) }

    override fun release() { _release() }
    override fun setItemCount( count:Int ) { _setItemCount( count ) }
    override fun setPosition( position:Int ) { _setPosition( position ) }

    override fun showEnRoute( active:Boolean ) { _showEnRoute( active ) }

    /**********************************************************************************************/

    private fun _init() {
        linearLayoutManager = android.support.v7.widget.LinearLayoutManager( view.context,android.support.v7.widget.LinearLayoutManager.VERTICAL,false )

        routeAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<android.support.v7.widget.RecyclerView>( R.id.routesrecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = routeAdapter
        recyclerView?.setHasFixedSize( true )

        routeactivate = view?.findViewById<CheckBox>( R.id.routeactivatecheckbox )
        routeactivate?.setOnClickListener { routeToggle() }

        setupScrolling()
    }

    private fun routeToggle() {
        val active = routeactivate?.isChecked ?: false
        listener?.setEnRoute( active )
    }

    private fun _release() {}

    private fun _setItemCount( count:Int ) {
        val positionStart = routeAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        routeAdapter?.itemCount = count
        routeAdapter?.notifyItemRangeInserted( positionStart,itemCount )

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

    private fun _setBusy( b:Boolean ) {
        view?.findViewById<View>( R.id.routesworkingimageview ).visibility = if ( b ) View.VISIBLE else View.INVISIBLE
    }

    private fun _showEnRoute( active:Boolean ) {
        routeactivate?.isChecked = active
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
    private inner class RouteHolder( view:View ) : android.support.v7.widget.RecyclerView.ViewHolder( view ) {
        fun bind( route:Route ) {
            val binding:RouteBinding = android.databinding.DataBindingUtil.bind( itemView )
            binding.route = route

            val routeNameView = itemView.findViewById<android.widget.TextView>( R.id.routenametextview )
            routeNameView.setOnClickListener { _listener?.onRoute( route ) }
            routeNameView.text = route.name

            val routeImageView = itemView.findViewById<android.widget.ImageView>( R.id.routeimageview )
            routeImageView.setOnClickListener { }

            routeImageView.setImageDrawable( null )
            if ( route.imageURL != null ) {
                val uri = android.net.Uri.parse( route.imageURL )
                com.squareup.picasso.Picasso.get()
                        .load( uri )
                        .into( routeImageView )
            }
        }
    }

    /*
     *
     */
    private inner class RouteAdapter : android.support.v7.widget.RecyclerView.Adapter<RouteHolder>()
    {
        var dataSource : DataSource<Route>? = null

        private var itemCount = 0

        override fun getItemCount(): Int {
            return itemCount
        }

        fun setItemCount( itemCount:Int ) {
            this.itemCount = itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : RouteHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.route,parent,false )
            return RouteHolder( view )
        }

        override fun onBindViewHolder( holder:RouteHolder?,position:Int ) {
            val route = dataSource?.getDataFor( position ) ?: Route( id="0" )
            holder?.bind( route )
        }
    }
}
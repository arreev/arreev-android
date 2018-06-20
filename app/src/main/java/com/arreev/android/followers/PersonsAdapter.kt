
package com.arreev.android.followers

import android.view.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.databinding.PersonBinding

/**
 * Created by jeffschulz on 5/15/18.
 */
class PersonsAdapter(val view:View, val dataSource:DataSource<Person>) : PersonsDisplay
{
    private var linearLayoutManager:android.support.v7.widget.LinearLayoutManager? = null
    private var recyclerView : android.support.v7.widget.RecyclerView? = null
    private var _listener: PersonsDisplay.Listener? = null
    private var _pageDelta = 25
    private var _pageSize = 100

    private val personAdapter = PersonAdapter()

    init { _init() }

    override var pageDelta:Int
        get() = _pageDelta
        set(value) { _pageDelta = value }

    override var pageSize:Int
        get() = _pageSize
        set(value) { _pageSize = value }

    override var listener:PersonsDisplay.Listener?
        get() = _listener
        set(value) { _listener = value }

    override var firstVisiblePosition:Int
        get() = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
        set(value) {}

    override fun release() { _release() }
    override fun setItemCount( count:Int ) { _setItemCount( count ) }
    override fun setPosition( position:Int ) { _setPosition( position ) }

    override fun assumeTransporter( transporter:Transporter? ) { _assumeTransporter( transporter ) }

    /**********************************************************************************************/

    private fun _init() {
        linearLayoutManager = android.support.v7.widget.GridLayoutManager( view.context,2 )

        personAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<android.support.v7.widget.RecyclerView>( R.id.personsrecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = personAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    private fun _release() {}

    private fun _setItemCount( count:Int ) {
        val positionStart = personAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        personAdapter?.itemCount = count
        personAdapter?.notifyItemRangeInserted( positionStart,itemCount )
    }

    private fun _setPosition( position:Int ) {
        recyclerView?.scrollToPosition( position )
    }

    private fun _assumeTransporter( transporter:Transporter? ) {
        val followersfragmenttransporterimageview = view.findViewById<android.widget.ImageView>( R.id.followersfragmenttransporterimageview )
        followersfragmenttransporterimageview.setImageBitmap( null )

        transporter ?: return

        if ( transporter.imageURL != null ) {
            val uri = android.net.Uri.parse( transporter.imageURL )
            com.squareup.picasso.Picasso.get()
                    .load( uri )
                    .into( followersfragmenttransporterimageview )
        }
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
    private inner class PersonHolder( view:View ) : android.support.v7.widget.RecyclerView.ViewHolder( view ) {
        fun bind( person:Person ) {
            val binding:PersonBinding = android.databinding.DataBindingUtil.bind( itemView )
            binding.person = person

            val personImageView = itemView.findViewById<android.widget.ImageView>( R.id.personimageview )
            personImageView.setOnClickListener { _listener?.onPerson( person ) }

            personImageView.setImageDrawable( null )
            if ( person.imageURL != null ) {
                val uri = android.net.Uri.parse( person.imageURL )
                com.squareup.picasso.Picasso.get()
                        .load( uri )
                        .into( personImageView )
            }
        }
    }

    /*
     *
     */
    private inner class PersonAdapter : android.support.v7.widget.RecyclerView.Adapter<PersonHolder>()
    {
        var dataSource : DataSource<Person>? = null

        private var itemCount = 0

        override fun getItemCount(): Int {
            return itemCount
        }

        fun setItemCount( itemCount:Int ) {
            this.itemCount = itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : PersonHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.person,parent,false )
            return PersonHolder( view )
        }

        override fun onBindViewHolder( holder:PersonHolder?,position:Int ) {
            val person = dataSource?.getDataFor( position ) ?: Person( id="0" )
            holder?.bind( person )
        }
    }
}
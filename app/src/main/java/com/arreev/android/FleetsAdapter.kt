
package com.arreev.android

import android.support.v7.widget.*
import com.arreev.android.model.*
import com.squareup.picasso.*
import android.databinding.*
import android.widget.*
import android.view.*
import android.net.*

import com.arreev.android.databinding.FleetBinding

/**
 * Created by jeffschulz on 3/8/18.
 */
class FleetsAdapter( val view:View,val dataSource:DataSource<Fleet> ) : FleetsDisplay
{
    private var linearLayoutManager:LinearLayoutManager? = null
    private var _listener: FleetsDisplay.Listener? = null
    private var recyclerView: RecyclerView? = null

    private val fleetAdapter:FleetAdapter = FleetAdapter()

    init {
        linearLayoutManager = LinearLayoutManager( view.context,LinearLayoutManager.VERTICAL,false )

        fleetAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<RecyclerView>( R.id.fleetrecyclerview )
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = fleetAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    override fun release() {}

    override fun setItemCount( count:Int ) {
        val positionStart = fleetAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        fleetAdapter?.setItemCount( count )
        fleetAdapter?.notifyItemRangeInserted( positionStart,itemCount )
    }

    override fun setPosition( position:Int ) {
        recyclerView?.scrollToPosition( position )
    }

    override var listener:FleetsDisplay.Listener?
        get() = _listener
        set(value) { _listener = value }

    override var firstVisiblePosition:Int
        get() = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
        set(value) {}

    private fun setupScrolling() {
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled( recyclerView:RecyclerView?, dx:Int,dy:Int ) {
                val childCount = linearLayoutManager?.childCount ?: 0
                val itemCount = linearLayoutManager?.itemCount ?: 0
                val firstVisibleItem = linearLayoutManager?.findFirstVisibleItemPosition() ?: 0
                val reach = ( firstVisibleItem + childCount )
                if ( reach >= itemCount ) {
                    dataSource?.getReadyFor( itemCount,30 )
                }
            }
        }
        recyclerView?.addOnScrollListener( listener )
    }

    private inner class FleetHolder( view:View ) : RecyclerView.ViewHolder( view )
    {
        private var fleetImageView :ImageView
        private var fleetNameView : TextView

        init {
            fleetImageView = itemView.findViewById<ImageView>( R.id.fleetimageview )
            fleetNameView = itemView.findViewById<TextView>( R.id.fleetnameview )
        }

        fun bind( fleet:Fleet ) {
            val binding:FleetBinding = DataBindingUtil.bind( itemView )
            binding.binding = FleetBinder()

            val fleetCardView = itemView.findViewById<View>( R.id.fleetcardview )
            val fleetid = fleet.id
            fleetCardView.setOnClickListener { onFleet( fleetid ) }

            fleetImageView.setImageDrawable( null )
            val uri = Uri.parse( fleet.imageURL )
            Picasso.with( itemView.context )
                    .load( uri )
                    .into( fleetImageView )

            fleetNameView.text = fleet.name
        }

        private fun onFleet( fleetid:String? ) {
            if ( fleetid != null ) {
                listener?.onFleet( fleetid )
            }
        }
    }

    private inner class FleetAdapter : RecyclerView.Adapter<FleetHolder>()
    {
        private var itemCount = 0

        var dataSource : DataSource<Fleet>? = null

        fun setItemCount( count:Int ) {
            this.itemCount = count
        }

        override fun getItemCount(): Int {
            return itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : FleetHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.fleet,parent,false )
            return FleetHolder( view )
        }

        override fun onBindViewHolder( holder:FleetHolder?,position:Int ) {
            val fleet = dataSource?.getDataFor( position ) ?: Fleet()
            holder?.bind( fleet )
        }
    }
}
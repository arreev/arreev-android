
package com.arreev.android

import android.support.v7.widget.*
import com.arreev.android.model.*
import com.squareup.picasso.*
import android.databinding.*
import android.widget.*
import android.view.*
import android.net.*

import com.arreev.android.databinding.TransporterBinding

/**
 * Created by jeffschulz on 3/8/18.
 */
class TransportersAdapter( val view:View,val dataSource:DataSource<Transporter> ) : TransportersDisplay
{
    private var _listener: TransportersDisplay.Listener? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var _transportingid: String? = null

    private val transporterAdapter:TransporterAdapter = TransporterAdapter()

    init {
        gridLayoutManager = GridLayoutManager( view.context,2 )

        transporterAdapter.dataSource = dataSource

        recyclerView = view?.findViewById<RecyclerView>( R.id.transporterrecyclerview )
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.adapter = transporterAdapter
        recyclerView?.setHasFixedSize( true )

        setupScrolling()
    }

    override fun release() {}

    override fun setItemCount( count:Int ) {
        val positionStart = transporterAdapter?.itemCount ?: 0
        val itemCount = ( count - positionStart )
        transporterAdapter?.setItemCount( count )
        transporterAdapter?.notifyItemRangeInserted( positionStart,itemCount )
    }

    override fun setPosition( position:Int ) {
        recyclerView?.scrollToPosition( position )
    }

    override var listener:TransportersDisplay.Listener?
        get() = _listener
        set(value) { _listener = value }

    override var firstVisiblePosition:Int
        get() = gridLayoutManager?.findFirstVisibleItemPosition() ?: 0
        set(value) {}

    override var transportingid:String?
        get() = _transportingid
        set(value) { _transportingid = value }

    private fun setupScrolling() {
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled( recyclerView:RecyclerView?, dx:Int,dy:Int ) {
                val childCount = gridLayoutManager?.childCount ?: 0
                val itemCount = gridLayoutManager?.itemCount ?: 0
                val firstVisibleItem = gridLayoutManager?.findFirstVisibleItemPosition() ?: 0
                val reach = ( firstVisibleItem + childCount )
                if ( reach >= itemCount ) {
                    dataSource?.getReadyFor( itemCount,30 )
                }
            }
        }
        recyclerView?.addOnScrollListener( listener )
    }

    private inner class TransporterHolder( view:View ) : RecyclerView.ViewHolder( view )
    {
        private var transporterImageView: ImageView
        private var transportingImageView: ImageView
        private var transporterNameView: TextView

        init {
            transporterImageView = itemView.findViewById<ImageView>( R.id.transporterimageview )
            transportingImageView = itemView.findViewById<ImageView>( R.id.transportingimageview )
            transporterNameView = itemView.findViewById<TextView>( R.id.transporternameview )
        }

        fun bind( transporter:Transporter ) {
            val binding:TransporterBinding = DataBindingUtil.bind( itemView )
            binding.binding = TransporterBinder()

            val transporterCardView = itemView.findViewById<View>( R.id.transportercardview )
            val transporterid = transporter.id
            transporterCardView.setOnClickListener { onTransporter( transporterid ) }

            transporterImageView.setImageDrawable( null )
            val uri = Uri.parse( transporter.imageURL )
            Picasso.with( itemView.context )
                    .load( uri )
                    .into( transporterImageView )

            val transportingdrawable = if ( transporter.id === _transportingid ) R.drawable.blinker else R.drawable.lednul
            transportingImageView.setImageResource( transportingdrawable )
            transportingImageView.animation?.start()

            transporterNameView.text = transporter.name
        }

        private fun onTransporter( transporterid:String? ) {
            if ( transporterid != null ) {
                listener?.onTransporter( transporterid )
            }
        }
    }

    private inner class TransporterAdapter : RecyclerView.Adapter<TransporterHolder>()
    {
        private var itemCount = 0

        var dataSource : DataSource<Transporter>? = null

        fun setItemCount( count:Int ) {
            this.itemCount = count
        }

        override fun getItemCount(): Int {
            return itemCount
        }

        override fun onCreateViewHolder( parent:ViewGroup?,viewType:Int ) : TransporterHolder {
            val view = LayoutInflater.from( parent?.getContext() ).inflate( R.layout.transporter,parent,false )
            return TransporterHolder( view )
        }

        override fun onBindViewHolder( holder:TransporterHolder?,position:Int ) {
            val transporter = dataSource?.getDataFor( position ) ?: Transporter()
            holder?.bind( transporter )
        }
    }
}
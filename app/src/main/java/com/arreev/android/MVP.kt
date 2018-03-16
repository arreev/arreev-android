
package com.arreev.android

/**
 * Created by jeffschulz on 3/8/18.
 */

interface Display
{
    fun release()
}

interface Presenter<D : Display>
{
    fun bind( d:D )
    fun unbind()
    fun release()
}

interface DataSource<D>
{
    fun getDataFor( position:Int ) : D
    fun getReadyFor( start:Int,count:Int )
    fun requestRefresh()
}

interface DataSink<D>
{
    fun setItemCount( count:Int )
    fun setPosition( position:Int )
}
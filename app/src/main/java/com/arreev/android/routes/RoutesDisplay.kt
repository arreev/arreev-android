
package com.arreev.android.routes

import com.arreev.android.*
import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
interface RoutesDisplay : Display,DataSink<Route>
{
    interface Listener
    {
        fun onRoute( route:Route )
        fun setEnRoute( active:Boolean )
    }

    var listener: RoutesDisplay.Listener?
    var firstVisiblePosition: Int
    var busy: Boolean

    fun showEnRoute( active:Boolean )
}

package com.arreev.android.rides

import com.arreev.android.*
import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
interface FleetsDisplay : Display,DataSink<Fleet>
{
    interface Listener
    {
        fun onFleet( fleet:Fleet )
    }

    var listener: FleetsDisplay.Listener?
    var firstVisiblePosition: Int
}

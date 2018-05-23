
package com.arreev.android.ride

import com.arreev.android.*
import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
interface RideDisplay : Display,DataSink<Ride>
{
    interface Listener
    {
        fun setTrackingEnabled( ride:Ride )
        fun setTrackingDisabled( ride:Ride )
    }

    var listener: RideDisplay.Listener?
    var firstVisiblePosition: Int
}

package com.arreev.android

import com.arreev.android.model.*

/**
 * Created by jeffschulz on 3/8/18.
 */
interface FleetsDisplay : Display,DataSink<Fleet>
{
    interface Listener
    {
        fun onFleet( fleetid:String )
    }

    var listener: FleetsDisplay.Listener?
    var firstVisiblePosition: Int
}
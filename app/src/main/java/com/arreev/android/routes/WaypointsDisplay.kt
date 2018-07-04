
package com.arreev.android.routes

import com.arreev.android.*
import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
interface WaypointsDisplay : Display,DataSink<Waypoint>
{
    interface Listener
    {
        fun onWaypoint( route:Waypoint )
    }

    var listener: WaypointsDisplay.Listener?

    fun setMap( map:com.google.android.gms.maps.GoogleMap? )
}
package com.arreev.android

/**
 * Created by jeffschulz on 3/8/18.
 */

data class GoFleetEvent( val fleetid: String )
data class GoTransporterEvent( val transporterid:String )
data class DatabaseClientConnectionEvent( val connected:Boolean )
data class UpdateTransporterLocationEvent( val transporterid:String, val lat:Double,val lng:Double )

package com.arreev.android

import com.arreev.android.model.*

/**
 * Created by jeffschulz on 3/8/18.
 */
interface TransportersDisplay : Display,DataSink<Transporter>
{
    interface Listener
    {
        fun onTransporter( transporterid:String )
    }

    var listener: TransportersDisplay.Listener?
    var firstVisiblePosition: Int
    var transportingid: String?
}
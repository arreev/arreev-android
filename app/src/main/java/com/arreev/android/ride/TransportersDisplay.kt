
package com.arreev.android.ride

import com.arreev.android.*
import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/15/18.
 */
interface TransportersDisplay : Display,DataSink<Transporter>
{
    interface Listener
    {
        fun onTransporter( transporter:Transporter )
    }

    var listener: TransportersDisplay.Listener?
    var firstVisiblePosition: Int
}

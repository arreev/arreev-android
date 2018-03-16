
package com.arreev.android

import android.databinding.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class FleetBinder
{
    var expandComment = ObservableBoolean()

    fun toggleComment() {
        expandComment.set( !expandComment.get() )
    }
}
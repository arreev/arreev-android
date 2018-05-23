
package com.arreev.android.followers

import gubo.slipwire.*
import com.arreev.android.*

/**
 * Created by jeffschulz on 5/17/18.
 */
interface PersonsDisplay : Display,DataSink<Person>
{
    interface Listener
    {
        fun onPerson( person:Person)
    }

    var listener: PersonsDisplay.Listener?
    var firstVisiblePosition: Int
}
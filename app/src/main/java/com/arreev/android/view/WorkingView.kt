
package com.arreev.android.view

import android.util.*
import android.widget.*
import android.content.*
import android.graphics.drawable.*

import com.arreev.android.R

/**
 * Created by jeffschulz on 5/14/18.
 */
class WorkingView : ImageView
{
    constructor( context:Context) : super( context,null )
    constructor( context:Context,attributes:AttributeSet? ) : super( context,attributes )
    constructor( context:Context,attributes:AttributeSet?,style:Int ) : super( context,attributes,style )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.setBackgroundResource( R.drawable.working )
        (this.background as AnimationDrawable).start()
    }
}
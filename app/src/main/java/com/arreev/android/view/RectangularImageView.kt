package com.arreev.android.view

import android.view.*
import android.util.*
import android.content.*

/**
 * Created by jeffschulz on 5/4/18.
 */
class RectangularImageView : android.support.v7.widget.AppCompatImageView
{
    constructor(context:Context) : super(context) {}
    constructor(context:Context, attrs:AttributeSet) : super(context, attrs) {}
    constructor(context:Context, attrs:AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure( widthMeasureSpec:Int,heightMeasureSpec:Int ) {
        var mw = View.MeasureSpec.getSize( widthMeasureSpec )
        var mh = View.MeasureSpec.getSize( heightMeasureSpec )

        if ( mw >= mh ) {
            val fw = mw.toFloat()
            val fh = fw * 3F / 4F
            mh = fh.toInt()
        } else {
            val fh = mh.toFloat()
            val fw = fh * 3F / 4F
            mw = fw.toInt()
        }

        mw = View.MeasureSpec.makeMeasureSpec( mw,View.MeasureSpec.EXACTLY )
        mh = View.MeasureSpec.makeMeasureSpec( mh,View.MeasureSpec.EXACTLY )

        super.onMeasure( mw,mh )
    }
}
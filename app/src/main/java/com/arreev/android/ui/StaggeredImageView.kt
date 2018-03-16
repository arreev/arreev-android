
package com.arreev.android.ui

import android.view.*
import android.util.*
import android.content.*
import android.support.v7.widget.*

/**
 * Created by JEFF on 7/17/2016.
 */
class StaggeredImageView : AppCompatImageView
{
    var imageWidth: Int = 0
    var imageHeight: Int = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mw = View.MeasureSpec.getSize(widthMeasureSpec)
        var mh = View.MeasureSpec.getSize(heightMeasureSpec)

        if (imageWidth > 0 && imageHeight > 0) {
            val iw = imageWidth.toFloat()
            val ih = imageHeight.toFloat()
            val iratio = iw / ih
            if (iratio > 0) {
                mh = (mw / iratio).toInt()
            }
        } else {
            mh = mw
        }

        mw = View.MeasureSpec.makeMeasureSpec(mw, View.MeasureSpec.EXACTLY)
        mh = View.MeasureSpec.makeMeasureSpec(mh, View.MeasureSpec.EXACTLY)

        super.onMeasure(mw, mh)
    }
}

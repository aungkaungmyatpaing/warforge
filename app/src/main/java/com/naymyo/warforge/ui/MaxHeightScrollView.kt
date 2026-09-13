package com.naymyo.warforge.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * A scroll view that grows to fit its content and then stops.
 *
 * Component notes run from one line to a short paragraph. A fixed height wastes space on
 * the short ones; an unbounded one lets a long note cover the model it is describing.
 */
class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0,
) : ScrollView(context, attrs, defStyle) {

    /** Pixels. Zero means no limit. */
    var maxHeightPx: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val spec = if (maxHeightPx > 0) {
            MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        } else {
            heightSpec
        }
        super.onMeasure(widthSpec, spec)
    }
}

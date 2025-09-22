package kr.open.library.simple_ui.presenter.extensions.display

import android.content.Context
import android.util.TypedValue

/****************
 * DP To PX, SP *
 ****************/

/**
 * Converts dp value to pixels
 */
public fun Number.dpToPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)

public fun Number.dpToSp(context: Context): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics,
    ) / (context.resources.displayMetrics.density * context.resources.configuration.fontScale)

/****************
 * PX To DP, SP *
 ****************/

/**
 * Converts pixel value to dp
 */
public fun Number.pxToDp(context: Context): Float = this.toFloat() / context.resources.displayMetrics.density

public fun Number.pxToSp(context: Context): Float =
    (this.toFloat() / context.resources.displayMetrics.density / context.resources.configuration.fontScale)

/****************
 * SP To DP, PX *
 ****************/

/**
 * Converts sp value to pixels
 */
public fun Number.spToPx(context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)

public fun Number.spToDp(context: Context): Float = (this.toFloat() * context.resources.configuration.fontScale)

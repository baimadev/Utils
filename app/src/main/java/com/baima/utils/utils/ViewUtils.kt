package com.baima.utils.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics


fun dp2px(dp:Float,ctx: Context):Int{
    val density = ctx.resources.displayMetrics.density
    return (density*dp+0.5f).toInt()
}


fun px2dp(px:Int,ctx: Context):Float{
    val density = ctx.resources.displayMetrics.density
    return px/density
}

fun getScreenHeight(ctx: Context):Int{
    val metric: DisplayMetrics = ctx.resources.displayMetrics
    return metric.heightPixels
}

fun getScreenWidth(ctx: Context):Int{
    val metric: DisplayMetrics = ctx.resources.displayMetrics
    return metric.widthPixels
}
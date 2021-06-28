package com.justsoft.redditshareinterceptor.utils

fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)

fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
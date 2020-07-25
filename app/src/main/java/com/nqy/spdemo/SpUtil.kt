package com.nqy.spdemo

import android.content.Context

object SpUtil {

    val sp = MyApp.context.getSharedPreferences("nqy", Context.MODE_PRIVATE)

}
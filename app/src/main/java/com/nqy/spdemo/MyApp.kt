package com.nqy.spdemo

import android.app.Application
import android.content.Context

class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {

        lateinit var context: Context

    }
}
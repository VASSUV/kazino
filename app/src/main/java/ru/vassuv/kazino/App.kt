package ru.vassuv.kazino

import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        private lateinit var app: App

        val context: Context
            get() = app.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}

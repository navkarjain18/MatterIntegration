package com.example.matterdemosampleapp

import android.app.Application
import com.example.matterdemosampleapp.local.DataStorePreference
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MatterApplication : Application() {

    val TAG = ">>//"

    override fun onCreate() {
        super.onCreate()
        initPreferences()
    }

    private fun initPreferences(){
        DataStorePreference.init(this)
    }
}
package com.example.markmoussa.meshchatapplication

import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.util.Log

class LifecycleObserverActivity(context: Context) : LifecycleObserver {

//    override fun onCreate() {
//        super.onCreate()
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//    }

    val mContext = context.applicationContext

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.i("DEBUG ", "ON APP FOREGROUNDED CALLED")
        val hypeFramework = mContext as HypeLifeCycle
        hypeFramework.requestHypeToStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.i("DEBUG ", "ON APP BACKGROUNDED CALLED")
        val hypeFramework = mContext as HypeLifeCycle
        hypeFramework.requestHypeToStop()

    }



}
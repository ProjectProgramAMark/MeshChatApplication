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

        // Commenting this out because if Hype stops when app backgrounded, messages would only get sent
        // when the user has the app open
         val hypeFramework = mContext as HypeLifeCycle
         hypeFramework.requestHypeToStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed() {
        // Commenting this part out for demo tomorrow
//        val hypeFramework = mContext as HypeLifeCycle
//        hypeFramework.requestHypeToStop()
    }



}
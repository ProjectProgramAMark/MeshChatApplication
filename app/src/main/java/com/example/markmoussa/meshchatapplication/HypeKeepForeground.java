package com.example.markmoussa.meshchatapplication;

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

// This class keeps track of whether an activity is running in the foreground.
// The Android API does not provide a means to query that, which we need in
// order to know whether the app is actively running. This is used to stop the
// Hype framework when the app is sent to the background, something that may or
// may not be desirable. In this case, we are running the framework only when the
// app is on the foreground. This is motivated by the fact that background support,
// although already existent, is not yet officially supported by the framework.
// This code was written here to prevent distracting from the ChatApplication's
// main purpose, which is to demonstrate how to use the Hype framework.
public class HypeKeepForeground extends Application {

    public interface LifecycleDelegate {

        void onApplicationStart(Application app);
        void onApplicationStop(Application app);
    }

    private boolean isRunningForeground = false;
    private WeakReference<LifecycleDelegate> lifecycleDelegate;

    @Override
    public void onCreate() {

        super.onCreate();

        final Application thisApp = this;

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityResumed(Activity activity) {

                boolean wasRunningForeground = getRunningForeground();

                setRunningForeground(true);

                if (!wasRunningForeground) {
                    if (getLifecycleDelegate() != null) {
                        getLifecycleDelegate().onApplicationStart(thisApp);
                    }
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {

                boolean wasRunningForeground = getRunningForeground();

                setRunningForeground(false);

                if (wasRunningForeground) {
                    if (getLifecycleDelegate() != null) {
                        getLifecycleDelegate().onApplicationStop(thisApp);
                    }
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void setRunningForeground(boolean isRunningForeground) {
        this.isRunningForeground = isRunningForeground;
    }

    private boolean getRunningForeground() {
        return this.isRunningForeground;
    }

    public synchronized void setLifecyleDelegate(LifecycleDelegate lifecycleDelegate) {
        this.lifecycleDelegate = new WeakReference<>(lifecycleDelegate);
    }

    private synchronized LifecycleDelegate getLifecycleDelegate() {
        return this.lifecycleDelegate != null ? this.lifecycleDelegate.get() : null;
    }
}

package com.arreev.android

import android.app.*

/**
 * Created by jeffschulz on 3/8/18.
 */
class ArreevApplication : Application()
{
    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule( AppModule( this ) )
                .build()
    }
}
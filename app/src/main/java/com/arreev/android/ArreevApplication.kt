
package com.arreev.android

import android.os.*
import android.app.*

import com.google.firebase.database.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/14/18.
 */
class ArreevApplication : Application()
{
    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        val versionCode = packageManager.getPackageInfo( packageName,0 ).versionCode
        val versionName = packageManager.getPackageInfo( packageName,0 ).versionName

        if ( com.arreev.android.BuildConfig.DEBUG ) {
            log("ARREEV V$versionName($versionCode) DEBUG" )
            StrictMode.setThreadPolicy( StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build() )
            StrictMode.setVmPolicy( StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build() )
        } else {
            log("ARREEV V$versionName($versionCode) RELEASE" )
        }

        if ( com.squareup.leakcanary.LeakCanary.isInAnalyzerProcess(this ) ) {
            return
        }
        com.squareup.leakcanary.LeakCanary.install( this )

        /*
         * must set this up before any subsequent database geInstance calls
         */
        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled( true );
        val rootreference = database.getReference()
        rootreference.keepSynced(true );

        appComponent = DaggerAppComponent.builder()
                .appModule( AppModule( this ) )
                .build()

    }
}
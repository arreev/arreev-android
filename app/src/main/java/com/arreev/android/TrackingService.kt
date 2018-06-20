
package com.arreev.android

import javax.inject.*

import android.os.*
import android.app.*
import android.content.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/16/18.
 *
 * https://developer.android.com/guide/components/services
 * https://developer.android.com/guide/components/services#Foreground
 * https://developer.android.com/about/versions/oreo/background-location-limits
 */
class TrackingService : Service()
{
    @Inject lateinit var tracking: Tracking
    @Inject lateinit var updating: Updating

    private val CHANNELID                   = "ARREEV_LOCATION_UPATES_CHANNEL"
    private val ONGOING_NOTIFICATION_ID     = 100

    override fun onCreate() {
        dbg("TrackingService.onCreate " )

        ArreevApplication.appComponent.inject(this )

        /*
         * https://developer.android.com/training/notify-user/channels
         */

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            val channel = NotificationChannel( CHANNELID,"Arreev Location",NotificationManager.IMPORTANCE_DEFAULT )
            channel.description = "Arreev Ride Location Updates"
            val notificationManager = getSystemService( NotificationManager::class.java )
            notificationManager!!.createNotificationChannel( channel )
        }

        val notificationIntent = Intent(this,HomeActivity::class.java )
        val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent,0 )

        val notification = android.support.v4.app.NotificationCompat.Builder(this,CHANNELID )
                .setPriority( android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT )
                .setContentText( getText( R.string.notificationmessage ) )
                .setContentTitle( getText (R.string.notificationtitle ) )
                .setTicker( getText( R.string.notificationticker ) )
                .setSmallIcon( R.drawable.ic_notification )
                .setContentIntent( pendingIntent )
                .build()

        startForeground( ONGOING_NOTIFICATION_ID,notification )
    }

    override fun onStartCommand( intent:Intent,flags:Int,startId:Int ):Int {
        dbg("TrackingService.onStartCommand" )

        val ownerid = intent.getStringExtra("ownerid" )
        val transporterid = intent.getStringExtra("transporterid" )
        tracking.open( ownerid,transporterid )
        updating.open( ownerid,transporterid )

        return Service.START_NOT_STICKY
    }

    override fun onBind( intent:Intent ):IBinder? { return null }
    override fun onUnbind( intent:Intent ):Boolean { return false }
    override fun onRebind( intent:Intent ) {}

    override fun onDestroy() {
        updating.close()
        tracking.close()

        stopForeground(true )

        dbg("TrackingService.onDestroy" )
    }
}

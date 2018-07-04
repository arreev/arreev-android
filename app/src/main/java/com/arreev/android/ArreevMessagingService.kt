
package com.arreev.android

import com.google.firebase.auth.*
import com.google.firebase.messaging.*
import com.google.firebase.iid.FirebaseInstanceId

import gubo.slipwire.*

/**
 * https://console.firebase.google.com/u/1/project/arreev-fireplace/notification
 *
 * Created by jeffschulz on 6/27/18.
 */
class ArreevMessagingService : FirebaseMessagingService()
{
    /*
     * For security, we use notification tag ... it must match this instnaces token:currentUser.uid
     */
    override fun onMessageReceived( remoteMessage:RemoteMessage? ) {
        val message = remoteMessage?.notification?.body

        dbg( message );
//        val token = FirebaseInstanceId.getInstance().token
//        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
//        val currentusertag = "${token}:${ownerid}"
//        val notificationtag = remoteMessage?.notification?.tag
//
//        if ( currentusertag.equals( notificationtag ) ) {
//            dbg("<WARNING: message not meant for me>" )
//            sendFCMTokenToServer()
//        } else {
//            dbg("<message> ${message}" )
//        }
    }
}
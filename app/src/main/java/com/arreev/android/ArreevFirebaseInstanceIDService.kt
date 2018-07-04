
package com.arreev.android

import com.google.firebase.iid.*
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

import gubo.slipwire.*

/**
 * Created by jeffschulz on 6/27/18.
 */
class ArreevFirebaseInstanceIDService : FirebaseInstanceIdService()
{
    override fun onTokenRefresh() {
        sendFCMTokenToServer()
    }
}

fun sendFCMTokenToServer() {
    try {
        val token = FirebaseInstanceId.getInstance().token
        val device = "${android.os.Build.MANUFACTURER}.${android.os.Build.MODEL}"
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid

        val reference = FirebaseDatabase.getInstance().getReference("fcm" )
                .child("tokens" )
                .child( token )

        reference.child("device" ).setValue( device ).addOnFailureListener { e -> dbg( e ) }
        reference.child("ownerid" ).setValue( ownerid ).addOnFailureListener { e -> dbg( e ) }
    } catch ( x:Throwable ) {
        dbg( x )
    }
}
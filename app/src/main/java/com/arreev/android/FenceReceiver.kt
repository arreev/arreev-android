
package com.arreev.android

import javax.inject.*
import android.content.*

import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

import com.google.android.gms.awareness.fence.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 6/29/18.
 */
class FenceReceiver : BroadcastReceiver()
{
    @Inject lateinit var state: State

    init {
        ArreevApplication.appComponent.inject(this )
    }

    override fun onReceive( context:Context?,intent:Intent? ) {
        try {
            val fencestate = FenceState.extract( intent )
            when ( fencestate.currentState ) {
                FenceState.TRUE -> { handle( fencestate ) }
                FenceState.FALSE -> {}
                FenceState.UNKNOWN -> {}
            }
        } catch ( x:Throwable ) {
            dbg( x )
        }
    }

    private fun handle( fencestate:FenceState? ) {
        fencestate ?: return

        val slice = fencestate.fenceKey.split(":" )
        val waypointid = slice[0]
        val waypointname = slice[1]
        val action = slice[2]

        state.setFencing( "$waypointid $waypointname $action" )

        val ownerid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val transporterid = state.getTransporter();

        FirebaseDatabase.getInstance().getReference("events" )
                .child( ownerid )
                .child( waypointid )
                .setValue( transporterid + ':' + action )
                .addOnFailureListener { dbg("<database FAILED set event>" ) }
    }
}

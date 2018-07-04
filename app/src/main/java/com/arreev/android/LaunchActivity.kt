
package com.arreev.android

import android.os.*
import android.content.*
import android.support.v7.app.*

import com.google.firebase.auth.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/14/18.
 */
class LaunchActivity : AppCompatActivity()
{
    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("LaunchActivity.onCreate" )

        sendFCMTokenToServer() // associates this fcm token with currentuser, which could be null if signed-out

        val intent = Intent( this,
                if ( isLoggedIn() ) HomeActivity::class.java else SignInActivity::class.java )
        startActivity( intent )
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        dbg("LaunchActivity.onDestroy" )
    }

    private fun isLoggedIn() : Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null;
    }
}
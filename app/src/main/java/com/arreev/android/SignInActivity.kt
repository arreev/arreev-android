
package com.arreev.android

import android.os.*
import android.view.*
import android.content.*
import android.databinding.*
import android.support.v7.app.*
import com.google.firebase.auth.*
import com.google.android.gms.tasks.*

import gubo.slipwire.*
import android.content.Context.INPUT_METHOD_SERVICE



/*
 * https://developer.android.com/topic/libraries/data-binding/
 * https://github.com/googlesamples/android-databinding
 * https://developer.android.com/reference/android/databinding/ObservableField
 */
class MutableSignIn
{
    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val error = ObservableField<String>()
}

/**
 * Created by jeffschulz on 5/14/18.
 */
class SignInActivity : AppCompatActivity(),OnCompleteListener<AuthResult>
{
    private val signin = MutableSignIn()

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("SignInActivity.onCreate" )

        val binding = android.databinding.DataBindingUtil.setContentView<com.arreev.android.databinding.SigninBinding>(this,R.layout.signin )
        binding.signin = signin

        signin.email.set( "guest@arreev.com" )
    }

    override fun onResume() {
        super.onResume()
        sendFCMTokenToServer() // associates this fcm token with currentuser, which could be null if signed-out
    }

    @Suppress( "UNUSED_PARAMETER" )
    public fun onSignIn( view:View ) {
        val imm = getSystemService( Context.INPUT_METHOD_SERVICE ) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow( currentFocus.windowToken,0 )

        findViewById<View>( R.id.workingview ).visibility = View.VISIBLE
        try {
            val email = signin.email.get() ?: ""
            val password = signin.password.get() ?: ""
            FirebaseAuth.getInstance().signInWithEmailAndPassword( email,password ).addOnCompleteListener(this,this )
        } catch ( e:Throwable ) {
            findViewById<View>( R.id.workingview ).visibility = View.INVISIBLE
            signin.error.set( "invalid sign in data" )
        }
    }

    override fun onComplete( result:Task<AuthResult> ) {
        findViewById<View>( R.id.workingview ).visibility = View.INVISIBLE
        signin.password.set( "" )
        if ( result.isSuccessful ) {
            val intent = Intent(this,HomeActivity::class.java )
            startActivity( intent )
            finish()
        } else {
            signin.error.set( "failed to sign in" )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dbg("SignInActivity.onDestroy" )
    }
}
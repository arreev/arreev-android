
package com.arreev.android

import javax.inject.*

import android.os.*
import android.view.*
import android.widget.*
import android.content.*
import com.google.gson.*
import android.util.Base64
import android.support.v7.app.*
import io.reactivex.disposables.*

import com.amazonaws.*
import com.amazonaws.regions.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.*

import kotlinx.android.synthetic.main.main.*

/**
 *
 */
class MainActivity : AppCompatActivity()
{
    @Inject lateinit var account:Account
    @Inject lateinit var eventBus:EventBus
    @Inject lateinit var transporting:Transporting
    @Inject lateinit var fleetsPresenter: FleetsPresenter

    private var eventDisposable: Disposable? = null

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("MainActivity.onCreate" )

        ArreevApplication.appComponent.inject(this )
        setContentView( R.layout.main)
        setSupportActionBar( findViewById( R.id.maintoolbar ) )

        _signin.visibility = View.GONE
        _signoutbutton?.visibility = View.GONE

        transporting.startup()
    }

    override fun onStart() {
        super.onStart()
        dbg("MainActivity.onStart" )

        login(null,null )

        eventDisposable = eventBus.observable( Any::class.java ).subscribe( { e -> event( e ) } )
    }

    fun onSignIn( view:View ) {
        _busy.visibility = View.VISIBLE
        _signin.visibility = View.GONE
        login( _email.text?.toString(),_password.text?.toString() )
    }

    fun onSignOut( view:View ) {
        logout();
    }

    override fun onStop() {
        super.onStop()

        eventDisposable?.dispose()
        eventDisposable = null

        fleetsPresenter.unbind()

        dbg("MainActivity.onStop" )
    }

    override fun onDestroy() {
        super.onDestroy()

        eventDisposable?.dispose()
        eventDisposable = null

        fleetsPresenter.release()

        transporting.shutdown()

        dbg("MainActivity.onDestroy" )
    }

    private fun login( email:String?,password:String? ) {
        try {
            val configuration = ClientConfiguration().withUserAgent("arreev-android" )
            val userpool = CognitoUserPool(this,"us-west-2_q3pK7mNmr","3b17pag51qqcmndcql0079hap5",null, configuration, Regions.US_WEST_2 )
            val user = userpool.getCurrentUser()

            account.ownerid = ""
            dbg("current user: $user" )

            val authenticationHandler = object : AuthenticationHandler {
                override fun getAuthenticationDetails(continuation:AuthenticationContinuation?, userId:String? ) {
                    dbg("getAuthenticationDetails $userId" )
                    val authenticationDetails = AuthenticationDetails( email,password, mapOf() )
                    continuation?.setAuthenticationDetails( authenticationDetails )
                    continuation?.continueTask()
                }

                override fun getMFACode( continuation:MultiFactorAuthenticationContinuation? ) {
                    val destination = continuation?.parameters?.destination
                    dbg("getMFACode $destination" )
                    continuation?.setMfaCode( "" ) // have to get code from user input
                    continuation?.continueTask()
                }

                override fun authenticationChallenge( continuation:ChallengeContinuation? ) {
                    dbg("authenticationChallenge" )
                    continuation?.continueTask()
                }

                override fun onSuccess( userSession:CognitoUserSession?, newDevice:CognitoDevice? ) {
                    dbg("onSuccess $userSession" )

                    getAccount( asJWT(userSession?.idToken?.jwtToken ?: "" ).sub )
                }

                override fun onFailure( x:Exception? ) {
                    dbg( x )

                    _busy.visibility = View.GONE
                    _signin.visibility = View.VISIBLE
                    _signoutbutton.visibility = View.GONE
                }
            }

            user.getSessionInBackground( authenticationHandler ) // async, will call back on this thread
        } catch ( x:Throwable ) {
            dbg( x )
        }
    }

    private fun getAccount( sub:String? ) {
        FetchAccount().fetch(sub ?: "" ).subscribe(
            { a -> loggedin( a.id ) },
            { e -> logout() }
        )
    }

    private fun loggedin( ownerid:String? ) {
        dbg("loggedin: ${ownerid}" )

        if ( ownerid.isNullOrEmpty() ) {
            logout()
        } else {
            account.ownerid = ownerid

            _busy.visibility = View.GONE
            _signin.visibility = View.GONE
            _signoutbutton.visibility = View.VISIBLE

            _fleets.visibility = View.VISIBLE
            fleetsPresenter.bind( FleetsAdapter( _fleets,fleetsPresenter ) )
        }
    }

    private fun logout() {
        try {
            val configuration = ClientConfiguration().withUserAgent("arreev-android")
            val userpool = CognitoUserPool(this, "us-west-2_q3pK7mNmr", "3b17pag51qqcmndcql0079hap5", null, configuration, Regions.US_WEST_2)
            val user = userpool.getCurrentUser()

            user.signOut()
            _fleets.visibility = View.INVISIBLE
            fleetsPresenter.unbind()
            fleetsPresenter.release()

            _busy.visibility = View.GONE
            _signin.visibility = View.VISIBLE
            _signoutbutton.visibility = View.GONE
        } catch ( x:Throwable ) {
            dbg( x )
        }
    }


    private fun event( e:Any ) {
        when ( e ) {
            is DatabaseClientConnectionEvent -> {
                val connectedtextview = findViewById<TextView>( R.id.connected )
                connectedtextview.text = if ( e.connected ) "connected" else "dis-connected"
            }
            is GoFleetEvent -> {
                val intent = Intent( this, TransportersActivity::class.java )
                intent.putExtra( "fleetid",e.fleetid )
                startActivity( intent )
            }
        }
    }
}

data class JWT( val sub:String?,val email:String? )

fun asJWT( token:String? ) : JWT {
    var jwt = JWT(null,null )

    try {
        val encoded = token?.split("." )?.get( 1 ) ?: ""
        val decoded = String( Base64.decode( encoded,Base64.DEFAULT ) )
        jwt = GsonBuilder().setPrettyPrinting().create().fromJson<JWT>( decoded,JWT::class.java )
    } catch ( x:Throwable ) {
        dbg( x )
    }

    return jwt
}

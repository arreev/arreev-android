
package gubo.slipwire

import android.util.*

/**
 * Created by jeffschulz on 3/8/18.
 */

var logger:Logger = AndroidLogger()

interface Logger
{
    fun log( a:Any? )
    fun dbg( a:Any? )
    fun err( e:Throwable? )
}

fun log( a:Any? ) { logger.log( a ); }
fun dbg( a:Any? ) { logger.dbg( a ); }
fun err( e:Throwable? ) { logger.err( e ); }

class AndroidLogger : Logger
{
    private val DBG = "DBG"

    override fun log( a:Any? ) {
        Log.v( DBG,a?.toString() ?: "null" )
    }

    override fun dbg( a:Any? ) {
        Log.d( DBG,a?.toString() ?: "null" )
    }

    override fun err( e:Throwable? ) {
        Log.e( DBG,"XXX",e )
    }
}


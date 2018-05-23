
package gubo.slipwire

import io.reactivex.*
import io.reactivex.subjects.*
import io.reactivex.schedulers.*
import io.reactivex.android.schedulers.*

/**
 * Created by jeffschulz on 3/8/18.
 */
open class Bus
{
    /*
     * https://android.jlelse.eu/super-simple-event-bus-with-rxjava-and-kotlin-f1f969b21003
     */

    private val publisher: PublishSubject<Any> = PublishSubject.create<Any>()

    fun send( a:Any ) {
        publisher.onNext( a )
    }

    fun<T> observable( t:Class<T> ) : Observable<T> = publisher.ofType( t )
            .subscribeOn( Schedulers.io() )
            .observeOn( AndroidSchedulers.mainThread() )
}

class EventBus : Bus()
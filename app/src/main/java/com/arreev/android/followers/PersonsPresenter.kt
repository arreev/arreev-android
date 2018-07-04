
package com.arreev.android.followers

import javax.inject.*

import io.reactivex.*
import io.reactivex.disposables.*

import com.google.firebase.auth.*
import com.google.firebase.database.*

import gubo.slipwire.*
import com.arreev.android.*
import com.arreev.android.api.*


/**
 * Created by jeffschulz on 5/15/18.
 *
 * TODO: All presenters using Range, getReadyFor are broken unless the server returns valid total ...
 *       so redo presenters with this in mind and/or change servlets
 */
class PersonsPresenter : Presenter<PersonsDisplay>,DataSource<Person>,PersonsDisplay.Listener
{
    private val persons = mutableListOf<Person>()

    private var database: FirebaseDatabase? = null
    private var disposable: Disposable? = null
    private var display: PersonsDisplay? = null
    private var firstVisiblePosition = 0

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    init { _init() }

    override fun bind( d:PersonsDisplay ) { _bind( d ) }
    override fun unbind() { _unbind() }
    override fun release() { _release() }

    override fun getDataFor( position:Int ):Person { return _getDataFor( position ) }
    override fun getReadyFor( start:Int,count:Int ) { _getReadyFor( start,count ) }
    override fun requestRefresh() { _requestRefresh() }

    override fun onPerson( person:Person ) { _onPerson( person ) }

    /**********************************************************************************************/

    private fun  _init() {
        ArreevApplication.appComponent.inject(this )

        database = FirebaseDatabase.getInstance()

        state.observeClient().subscribe( { c:Client -> getTransporterInfo() } )
    }

    private fun _bind(d:PersonsDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 15
        display?.pageSize = 50
        display?.setItemCount( persons.size )
        display?.setPosition( firstVisiblePosition )

        if ( persons.isEmpty() ) fetch(0,500 )
        getTransporterInfo()
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null

        persons.clear()
    }

    private fun _release() {
        disposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        persons.clear()

        dbg("PersonsPresenter.release" )
    }

    private fun _getDataFor( position:Int ):Person {
        try {
            return persons[ position ]
        } catch ( x:Throwable ) {
            dbg( x )
        }
        return Person( id = "0" )
    }

    private fun _getReadyFor( start:Int,count:Int ) {}

    private fun _requestRefresh() {}

    private fun _onPerson( person:Person ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        val transporterid = this.state.getTransporter();
        transporterid ?: return

        val path = "followers/${ownerid}/${person.id}/${transporterid}"
        val reference = database?.getReference( path )

        reference?.addValueEventListener( object : ValueEventListener {
            override fun onDataChange( snapshot:DataSnapshot ) {
                reference?.removeEventListener(this )
                val on = !( snapshot.getValue( Boolean::class.java ) ?: false )
                reference.setValue( on ).addOnSuccessListener { person.isFollowing.set( on ) }
            }
            override fun onCancelled( error:DatabaseError ) {
                reference?.removeEventListener(this )
                dbg( error )
            }
        } )
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        disposable?.dispose()

        display?.busy = true
        FetchPersons().fetch(ownerid ?: "", start,count )
                .flatMap { (id,name,imageURL) -> Observable.just( Person( id,name,imageURL ) ) }
                .subscribe(
                { f:Person -> onNext( f ) },
                { x:Throwable -> onError() },
                { onComplete() }
        )
    }

    private fun onNext( p:Person ) {
        val indexof = persons.indexOf( p )
        when ( indexof ) {
            -1 -> { persons.add( p ) }
            else -> { persons[ indexof ] = p }
        }
    }

    private fun onComplete() {
        display?.setItemCount( persons.size )
        display?.busy = false
        sync()
    }

    private fun getTransporterInfo() {
        val transporterid = state.getTransporter()
        display?.assumeTransporter(null )

        transporterid ?: return

        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        FetchTransporter().fetch( ownerid,transporterid ).subscribe(
                { t:Transporter -> display?.assumeTransporter( t ) },
                { e:Throwable -> onError() },
                { sync() }
        )
    }

    private fun sync() {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        val transporterid = this.state.getTransporter();
        transporterid ?: return

        persons.forEach { person -> sync( ownerid,transporterid,person ) }
    }

    private fun sync( ownerid:String,transporterid:String,person:Person ) {
        person.isFollowing.set( false )

        val path = "followers/${ownerid}/${person.id}/${transporterid}"
        val reference = database?.getReference( path )

        reference?.addValueEventListener( object : ValueEventListener {
            override fun onDataChange( snapshot:DataSnapshot ) {
                reference?.removeEventListener(this )
                val on = ( snapshot.getValue( Boolean::class.java ) ?: false )
                person.isFollowing.set( on )
            }
            override fun onCancelled( error:DatabaseError ) {
                reference?.removeEventListener(this )
                dbg( error )
            }
        } )
    }

    private fun onError() {
        eventbus.send( NetworkErrorEvent() )
        display?.busy = false
    }
}
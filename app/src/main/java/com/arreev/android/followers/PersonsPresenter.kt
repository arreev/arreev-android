
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
    private val referencelisteners = mutableListOf<PersonChildEventListener>()
    private val persons = mutableListOf<Person>()

    private var database: FirebaseDatabase? = null
    private var disposable: Disposable? = null
    private var display: PersonsDisplay? = null
    private var firstVisiblePosition = 0

    @Inject lateinit var eventbus : EventBus
    @Inject lateinit var state : State

    inner class PersonChildEventListener( val person:Person,val reference:DatabaseReference ) : ChildEventListener
    {
        override fun onChildMoved( snapshot:DataSnapshot?,prevchildkey:String? ) {}
        override fun onChildChanged( snapshot:DataSnapshot?,prevchildkey:String? ) {
            when ( snapshot?.key ) {
                "isFollowing" -> {
                    val isFollowing = snapshot.value as Boolean
                    person.isFollowing?.set( isFollowing )
                }
            }
        }
        override fun onChildAdded( snapshot:DataSnapshot?,prevchildkey:String? ) {}
        override fun onChildRemoved( snapshot:DataSnapshot? ) {}
        override fun onCancelled( error:DatabaseError? ) {}
    }

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
    }

    private fun _bind(d:PersonsDisplay) {
        display?.release()

        display = d
        display?.listener = this
        display?.pageDelta = 15
        display?.pageSize = 50
        display?.setItemCount( persons.size )
        display?.setPosition( firstVisiblePosition )

        if ( persons.isEmpty() ) fetch(0,500 ) else resync()
    }

    private fun _unbind() {
        firstVisiblePosition = display?.firstVisiblePosition ?: 0

        display?.listener = null
        display?.release()
        display = null
    }

    private fun _release() {
        disposable?.dispose()

        display?.listener = null
        display?.release()
        display = null

        persons.clear()
        clearReferenceListeners()

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

        val reference = database?.getReference("followers/${ownerid}/${person.id}" )
        reference ?: return

        val isFollowing = !( person.isFollowing?.get() ?: false )
        reference.updateChildren( mapOf("isFollowing" to isFollowing ) )
    }

    private fun fetch( start:Int,count:Int ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        disposable?.dispose()

        FetchPersons().fetch( ownerid ?: "", start,count )
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

    private fun onError() {
        eventbus.send( NetworkErrorEvent() )
    }

    private fun onComplete() {
        display?.setItemCount( persons.size )
        resync()
    }

    private fun resync() {
        clearReferenceListeners()
        for ( p in persons ) sync( p )
    }

    private fun sync( person:Person ) {
        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        ownerid ?: return

        val reference = database?.getReference("followers/${ownerid}/${person.id}" )
        reference ?: return

        /*
         * get current value
         */
        val valuelistener = object : ValueEventListener {
            override fun onDataChange( snapshot:DataSnapshot ) {
                value( snapshot )
                reference.removeEventListener(this )
            }
            override fun onCancelled( error:DatabaseError ) { dbg( error ) }
        }
        reference.addValueEventListener( valuelistener )

        /*
         * add child change listener
         */
        val childlistener = PersonChildEventListener( person,reference )
        referencelisteners.add( childlistener )
        reference.addChildEventListener( childlistener )
    }

    private fun value( snapshot:DataSnapshot? ) {
        snapshot ?: return
        try {
            val personid = snapshot.key
            val isFollowing = snapshot.child("isFollowing" ).getValue( Boolean::class.java )
            val person = persons.find { p -> p.id == personid }
            person?.isFollowing?.set( isFollowing ?: false )
        } catch ( x:Throwable ) {
            dbg( x )
        }
    }

    private fun clearReferenceListeners() {
        for ( listener in referencelisteners ) {
            listener?.reference?.removeEventListener( listener )
        }
        referencelisteners.clear()
    }
}
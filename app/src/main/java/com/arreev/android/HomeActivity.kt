
package com.arreev.android

import java.util.concurrent.*

import android.os.*
import android.view.*
import javax.inject.*
import android.content.*
import android.content.pm.*

import com.google.firebase.auth.*

import gubo.slipwire.*

/**
 * Created by jeffschulz on 5/4/18.
 */
class HomeActivity : android.support.v7.app.AppCompatActivity(),
        android.support.design.widget.NavigationView.OnNavigationItemSelectedListener,
        android.support.v4.view.ViewPager.OnPageChangeListener
{
    @Inject lateinit var eventbus:EventBus
    @Inject lateinit var network: Network
    @Inject lateinit var state: State

    private val REQUEST_LOCATION_PERMISSONS = 200

    private var appBarLayout: android.support.design.widget.AppBarLayout? = null
    private var ridesButton: android.support.v7.widget.AppCompatButton? = null
    private var routesButton: android.support.v7.widget.AppCompatButton? = null
    private var followersButton: android.support.v7.widget.AppCompatButton? = null
    private var viewPager: android.support.v4.view.ViewPager? = null
    private var appBarLayoutIsExpanded = false
    private var eventbusDisposable: io.reactivex.disposables.Disposable? = null
    private var stateDisposable: io.reactivex.disposables.Disposable? = null

    private var trackingservice: Intent? = null

    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
        dbg("HomeActivity.onCreate" )

        /*
         * should never happen
         */
        if ( FirebaseAuth.getInstance().currentUser == null ) {
            val intent = Intent( this,SignInActivity::class.java )
            startActivity( intent )
            finish()
            return
        }

        ArreevApplication.appComponent.inject(this )

        trackingservice = Intent(this,TrackingService::class.java )

        stateDisposable = state.observeClient().subscribe( { client:Client -> stateChange( client ) } )

        setContentView( R.layout.home )
        configure()
    }

    override fun onCreateOptionsMenu( menu: Menu? ): Boolean {
        menuInflater.inflate( R.menu.home,menu )
        return true
    }

    override fun onOptionsItemSelected( item: MenuItem? ): Boolean {
        val id = item?.itemId ?: -1

        when ( id ) {
            R.id.action_search -> {
                appBarLayout?.setExpanded( !appBarLayoutIsExpanded )
                return true
            }
            R.id.action_settings -> {
                return true
            }
        }

        return super.onOptionsItemSelected( item )
    }

    override fun onStart() {
        super.onStart()

        eventbusDisposable = eventbus.observable( ArreevEvent::class.java ).subscribe( { event:ArreevEvent -> onEvent( event ) } )

        if ( !network.isAvailable() ) eventbus.send( NetworkErrorEvent() )

        if ( state.getTransporter() != null ) {
            appBarLayoutIsExpanded = false
            appBarLayout?.setExpanded( appBarLayoutIsExpanded )
        }
    }

    override fun onRequestPermissionsResult( requestCode:Int, permissions:Array<out String>, grantResults:IntArray ) {
        when ( requestCode ) {
            REQUEST_LOCATION_PERMISSONS -> {
                val granted = ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                when ( granted ) {
                    true -> {
                        android.support.v4.content.ContextCompat.startForegroundService(this,trackingservice )
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onNavigationItemSelected( item:MenuItem ) : Boolean {
        val signout = getString( R.string.signout )
        when ( item.title ) {
            signout -> { signout() }
        }

        val drawer = findViewById<android.support.v4.widget.DrawerLayout>( R.id.homedrawerlayout )
        drawer.closeDrawer( android.support.v4.view.GravityCompat.START )

        return true;
    }

    override fun onPageScrollStateChanged( state:Int ) {}
    override fun onPageScrolled( position:Int, positionOffset:Float, positionOffsetPixels:Int ) {}

    override fun onPageSelected( position:Int ) {
        ridesButton?.isSelected = false
        routesButton?.isSelected = false
        followersButton?.isSelected = false
        when ( position ) {
            0 -> ridesButton?.isSelected = true
            1 -> routesButton?.isSelected = true
            2 -> followersButton?.isSelected = true
        }
    }

    override fun onStop() {
        super.onStop()

        eventbusDisposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()

        stateDisposable?.dispose()
        eventbusDisposable?.dispose()

        dbg("HomeActivity.onDestroy" )
    }

    private fun onEvent( event:ArreevEvent? ) {
        when ( event ) {
            is NetworkErrorEvent -> { showNetworkError() }
            is StartTrackingServiceEvent -> {
                startTrackingService( event.ride )
            }
            is StopTrackingServiceEvent -> {
                stopTrackingService( event.ride )
            }
        }
    }

    private fun stateChange( client:Client ) {
        when ( client.modification ) {
            TRANSPORTERID -> {
                stopTrackingService(null )
            }
            TRACKINGLOCATION -> {
                val homecontentgpsimageview = findViewById<View>( R.id.homecontentgpsimageview )
                homecontentgpsimageview.alpha = 1F
                io.reactivex.Observable.just("" ).delay(500, TimeUnit.MILLISECONDS ).subscribe(
                        { homecontentgpsimageview.alpha = 0F }
                );
            }
        }
    }

    private fun startTrackingService( ride:Ride ) {
        stopService( trackingservice )

        val ownerid = FirebaseAuth.getInstance().currentUser?.uid
        val transporterid = ride.id

        trackingservice?.putExtra("ownerid",ownerid )
        trackingservice?.putExtra("transporterid",transporterid )

        val permission = android.support.v4.content.ContextCompat.checkSelfPermission( applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION )
        when ( permission ) {
            PackageManager.PERMISSION_GRANTED -> {
                android.support.v4.content.ContextCompat.startForegroundService(this,trackingservice )
            }
            else -> {
                val permissions = arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION )
                android.support.v4.app.ActivityCompat.requestPermissions(this, permissions,REQUEST_LOCATION_PERMISSONS )
            }
        }
    }

    private fun stopTrackingService( ride:Ride? ) {
        findViewById<View>( R.id.homecontentgpsimageview ).alpha = 0F

        stopService( trackingservice )
    }

    private fun showNetworkError() {
        val textview = findViewById<android.widget.TextView>( R.id.homecontentnetworkerrortextview )

        textview.visibility = android.view.View.VISIBLE
        textview.alpha = 0F
        textview.animate().alpha(1F ).setDuration( 500 ).withEndAction {
            textview.animate().alpha( 0F ).setDuration( 500 ).setStartDelay( 2500 ).withEndAction { textview.visibility = android.view.View.GONE }
        }
    }

    private fun signout() {
        state.clearAll()

        FirebaseAuth.getInstance().signOut()
        val intent = Intent( this,LaunchActivity::class.java )
        startActivity( intent )
        finish()
    }

    private fun configure() {
        val toolbar = findViewById<android.support.v7.widget.Toolbar>( R.id.hometoolbar )
        toolbar.title = resources.getString( R.string.app_name )
        setSupportActionBar( toolbar )

        val drawer = findViewById<android.support.v4.widget.DrawerLayout>( R.id.homedrawerlayout )
        val toggle = android.support.v7.app.ActionBarDrawerToggle( this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close )
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<android.support.design.widget.NavigationView>( R.id.homenavview )
        val navheaderemailtextview = navigationView.getHeaderView(0 ).findViewById<android.widget.TextView>( R.id.navheaderemailtextview )
        navheaderemailtextview?.text = FirebaseAuth.getInstance().currentUser?.email
        navigationView.setNavigationItemSelectedListener( this )

        appBarLayout = findViewById<android.support.design.widget.AppBarLayout>( R.id.homeappbar )
        appBarLayout?.addOnOffsetChangedListener { appBarLayout, verticalOffset -> appBarLayoutIsExpanded = (verticalOffset == 0) }

        ridesButton = findViewById<android.support.v7.widget.AppCompatButton>( R.id.homeridesbutton )
        routesButton = findViewById<android.support.v7.widget.AppCompatButton>( R.id.homeroutesbutton )
        followersButton = findViewById<android.support.v7.widget.AppCompatButton>( R.id.homefollowersbutton )

        ridesButton?.setOnClickListener( { viewPager?.setCurrentItem( 0 ) } )
        routesButton?.setOnClickListener( { viewPager?.setCurrentItem( 1 ) } )
        followersButton?.setOnClickListener( { viewPager?.setCurrentItem( 2 ) } )

        val homeFragmentStatePagerAdapter = HomeFragmentStatePagerAdapter( supportFragmentManager )
        viewPager = findViewById<android.support.v4.view.ViewPager>( R.id.homeviewpager )
        viewPager?.setAdapter( homeFragmentStatePagerAdapter )
        viewPager?.addOnPageChangeListener(this )
        viewPager?.setOffscreenPageLimit( 4 )
        viewPager?.setCurrentItem( 0 )

        ridesButton?.isSelected = true
    }

    private class HomeFragmentStatePagerAdapter( fragmentManager:android.support.v4.app.FragmentManager ) :
            android.support.v4.app.FragmentStatePagerAdapter( fragmentManager )
    {
        override fun getCount(): Int {
            return 3
        }

        override fun getItem( position : Int ) : android.support.v4.app.Fragment? {
            var fragment: android.support.v4.app.Fragment? = null
            when ( position ) {
                0 -> fragment = com.arreev.android.ride.RideFragment()
                1 -> fragment = EmptyFragment()
                2 -> fragment = com.arreev.android.followers.FollowersFragment()
            }
            return fragment
        }
    }
}

class EmptyFragment : android.support.v4.app.Fragment()
{
    override fun onCreate( savedInstanceState:Bundle? ) {
        super.onCreate( savedInstanceState )
    }

    override fun onCreateView(inflater:LayoutInflater?, container:ViewGroup?, savedInstanceState:Bundle? ) : View? {
        val view = inflater?.inflate( R.layout.emptyfragment,container,false );
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
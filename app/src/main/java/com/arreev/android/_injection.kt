
package com.arreev.android

import javax.inject.*
import android.content.*

import dagger.*

import gubo.slipwire.*
import com.arreev.android.api.*
import com.arreev.android.ride.*
import com.arreev.android.followers.*

@Module
class AppModule( val application:ArreevApplication )
{
    @Provides
    @Singleton
    fun provideApplicationContext() : Context = application.applicationContext

    @Provides
    @Singleton
    fun provideState() : State = State()

    @Provides
    @Singleton
    fun provideNetwork() : Network = Network()

    @Provides
    @Singleton
    fun provideEventBus() : EventBus = EventBus()

    @Provides
    @Singleton
    fun provideRetrofit() : retrofit2.Retrofit = retrofit( "https://api.prayclient.com" )

    @Provides
    @Singleton
    fun provideRidePresenter() : RidePresenter = RidePresenter()

    @Provides
    @Singleton
    fun provideFleetsPresenter() : FleetsPresenter = FleetsPresenter()

    @Provides
    @Singleton
    fun provideTransportersPresenter() : TransportersPresenter = TransportersPresenter()

    @Provides
    @Singleton
    fun providePersonsPresenter() : PersonsPresenter = PersonsPresenter()

    @Provides
    @Singleton
    fun provideTracking() : Tracking = Tracking()

    @Provides
    @Singleton
    fun provideUpdateing(): Updating = Updating()
}

/**
 * Created by jeffschulz on 5/4/18.
 */
@Singleton
@Component( modules = arrayOf( AppModule::class ) )
interface AppComponent
{
    fun inject( homeActivity:HomeActivity )
    fun inject( rideFragment:RideFragment )
    fun inject( ridePresenter:RidePresenter )
    fun inject( fleetsPresenter:FleetsPresenter )
    fun inject( transportersPresenter:TransportersPresenter )
    fun inject( fetchFleets:FetchFleets )
    fun inject( fetchTransporters:FetchTransporters )
    fun inject( fetchTransporter:FetchTransporter )
    fun inject( followersFragment:FollowersFragment )
    fun inject( personsPresenter:PersonsPresenter )
    fun inject( network:Network )
    fun inject( tracking:Tracking )
    fun inject( updating:Updating )
    fun inject( trackingService:TrackingService )
    fun inject( fetchPersons:FetchPersons )
    fun inject( state:State )
}
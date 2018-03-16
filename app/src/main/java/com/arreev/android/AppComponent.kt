
package com.arreev.android

import dagger.*
import javax.inject.*

/**
 * Created by GUBO on 7/26/2017.
 */
@Singleton
@Component( modules = arrayOf( AppModule::class ) )
interface AppComponent
{
    fun inject( mainActivity: MainActivity )
    fun inject( fleetingActivity:TransportersActivity)
    fun inject( transportingActivity: TransportingActivity )

    fun inject( fleetsPresenter: FleetsPresenter )
    fun inject( transportersPresenter: TransportersPresenter )

    fun inject( transporting: Transporting )

    fun inject( fetchAccount: FetchAccount )
    fun inject( fetchFleets: FetchFleets )
    fun inject( fetchTransporter: FetchTransporter )
    fun inject( fetchTransporters: FetchTransporters )
}

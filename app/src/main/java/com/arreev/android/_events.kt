
package com.arreev.android

interface ArreevEvent

class NetworkErrorEvent : ArreevEvent

class StartTrackingServiceEvent( val ride:Ride ) : ArreevEvent
class StopTrackingServiceEvent( val ride:Ride ) : ArreevEvent
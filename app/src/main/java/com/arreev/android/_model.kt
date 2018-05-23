
package com.arreev.android

data class Fleet(
        val id: String,
        val name: String? = null,
        val imageURL: String? = null
)

data class Transporter(
        val id: String,
        val name: String? = null,
        val imageURL: String? = null
)

data class Ride(
        val id: String,
        val name: String? = null,
        val imageURL: String? = null,
        val isTrackingEnabled:android.databinding.ObservableBoolean = android.databinding.ObservableBoolean(false ),
        val isFollowingEnabled:android.databinding.ObservableBoolean = android.databinding.ObservableBoolean(false )
)

data class Person(
        val id: String,
        val name: String? = null,
        val imageURL: String? = null,
        val isFollowing:android.databinding.ObservableBoolean = android.databinding.ObservableBoolean(false )
)
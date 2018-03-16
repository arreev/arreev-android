package com.arreev.android

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by jeffschulz on 3/9/18.
 */
class Test internal constructor() {
    internal var ref:DatabaseReference? = null

    init {
        val client:FusedLocationProviderClient? = null

        //        class CB extends LocationCallback {};
        //
        //        ref.addValueEventListener(new ValueEventListener() {
        //            @Override
        //            public void onDataChange(DataSnapshot dataSnapshot) {
        //                String value = dataSnapshot.getValue(String.class);
        //            }
        //
        //            @Override
        //            public void onCancelled(DatabaseError error) {
        //            }
        //        });

        try {
            client!!.requestLocationUpdates(null, object : LocationCallback() {}, null)
        } catch (x:SecurityException) {
            x.printStackTrace()
        }

    }
}

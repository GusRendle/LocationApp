package com.rendle.locationapp.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rendle.locationapp.R
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityMapBinding
import com.rendle.locationapp.models.PoIModel

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityMapBinding
//Refers to nav drawer toggle in the activity's action bar
private lateinit var toggle: ActionBarDrawerToggle
//The map data and view
private lateinit var mMap: GoogleMap
//This app's Firebase Authentication
private lateinit var auth: FirebaseAuth
//This app's Firebase Database & Database ref
private lateinit var firebaseDb: FirebaseDatabase
private lateinit var dbRef: DatabaseReference

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityMapBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        //Sets the Tool Bar as a Support Action Bar
        setSupportActionBar(b.toolbarMain)
        //Adds support for back button in search and hamburger menu button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Creates hamburger menu button, links it to nav drawer
        toggle = ActionBarDrawerToggle(this, b.drawerLayout, R.string.open_text, R.string.close_text)
        b.drawerLayout.addDrawerListener(toggle)
        //Toggle is ready
        toggle.syncState()

        auth = FirebaseAuth.getInstance()

        //Intent for the login activity
        val loginIntent = Intent(this, LoginActivity::class.java)

        //Handles clicks on nav drawer icons
        b.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_item_main -> startActivity(Intent(this, MainActivity::class.java))
                R.id.item_sign_out -> {
                    auth.signOut()
                    startActivity(loginIntent)
                    finish()
                }
            }
            //If user signed out, redirect to login page
            if(auth.currentUser == null){
                startActivity(loginIntent)
                finish()
            }
            true
        }

        //Redirects user to login activity if not logged in
        if(auth.currentUser == null){
            startActivity(loginIntent)
            finish()
        }

        //Links Firebase db to the db's URL
        firebaseDb = FirebaseDatabase.getInstance("https://locationapp-3c40b-default-rtdb.europe-west1.firebasedatabase.app/")
        dbRef = firebaseDb.reference
        //Reference to the PoI sub-section of the db
        val poiRef: DatabaseReference = dbRef.child("POIs")

        //Gets the id of the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //Acquires the GoogleMap object
        mapFragment.getMapAsync { mMap ->
            //Runs at start and whenever database info changes
            poiRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Wipe previous data
                        mMap.clear()
                        for (poiSnapshot in dataSnapshot.children) {
                            //Lat and lng are not stored as a LatLng in the Firebase db
                            val lat = poiSnapshot.child("location/latitude/").value.toString().toDouble()
                            val lng = poiSnapshot.child("location/longitude/").value.toString().toDouble()
                            //Add poi to map
                            mMap.addMarker(MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(poiSnapshot.child("name").value as String?))!!.tag = poiSnapshot.key
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
            })
        }
    }

    //Adds on click functionality to nav drawer items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}

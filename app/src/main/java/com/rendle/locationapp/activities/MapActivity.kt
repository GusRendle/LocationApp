package com.rendle.locationapp.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rendle.locationapp.R
import com.rendle.locationapp.databinding.ActivityMapBinding
import org.json.JSONObject
import java.net.URL




//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityMapBinding
//Refers to nav drawer toggle in the activity's action bar
private lateinit var toggle: ActionBarDrawerToggle
//This app's Firebase Authentication
private lateinit var auth: FirebaseAuth
//This app's Firebase Database & Database ref
private lateinit var firebaseDb: FirebaseDatabase
private lateinit var dbRef: DatabaseReference
//Toggle for showing only favourites
private var favToggle: Boolean = false
//List of favourite uuid values
private  var favList = mutableListOf<String>()

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
        //Gets the id of the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //Acquires the GoogleMap object from onMapReady
        mapFragment.getMapAsync(::onMapReady)

        //Links Firebase db to the db's URL
        dbRef = FirebaseDatabase.getInstance("https://locationapp-3c40b-default-rtdb.europe-west1.firebasedatabase.app/").reference
        //Goes to the child with the user's uid in the admins sub category
        dbRef.child("Admins").child(auth.currentUser!!.uid).get().addOnSuccessListener {
            //If the value exists, user is an admin
            if (it.value != null) {
                b.fabAddLocation.visibility = View.VISIBLE
            }
        }.addOnFailureListener{
            Log.e("firebase", "User is not an Admin", it)
        }

        //Open AddLocationActivity when fab is pressed
        b.fabAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        //Toggles favourites when button is pressed
        b.btnFav.setOnClickListener {
            favToggle = !favToggle
            if (favToggle) {
                b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
            } else {
                b.btnFav.setImageResource(R.drawable.ic_fav_24)
            }
            mapFragment.getMapAsync(::onMapReady)
        }

        //If the map was left in fav mode, set the icon to reflect this
        if (favToggle) {
            b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
        }

        //Reference to the Favourites sub-section of the db
        val favRef: DatabaseReference = dbRef.child("Favourites/${auth.currentUser!!.uid}")
        favRef.addValueEventListener(object : ValueEventListener {
            //Runs every time data is updated
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Wipe previous data
                favList.clear()
                if (dataSnapshot.exists()) {
                    //For each favourite, adds the uuid to favList
                    for (favSnapshot in dataSnapshot.children) {
                        favList.add(favSnapshot.key!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        //Starts an Asynchronous thread to get and update the weather
        Thread {
            val response:String?
            //URL for the weather API
            response = URL("https://api.openweathermap.org/data/2.5/weather?lat=52.1&lon=-2.2&appid=d6f716616a77bbef0be8f6d850fe47b5&units=metric").readText(Charsets.UTF_8)
            val jsonObj = JSONObject(response)
            val main = jsonObj.getJSONObject("main")
            val clouds = jsonObj.getJSONObject("clouds")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val temp = main.getString("temp")+"°C"
            val cloudCoverage = clouds.getString("all")
            val weatherDescription = weather.getString("description")
            runOnUiThread {
                //Gets layout for the header
                val headerLayout: View = b.navView.getHeaderView(0)
                val tvTemp = headerLayout.findViewById<TextView>(R.id.tv_temp)
                val tvWeather = headerLayout.findViewById<TextView>(R.id.tv_weather)
                val navHeader = headerLayout.findViewById<ConstraintLayout>(R.id.nav_header)
                //Adjust background depending on weather
                if (cloudCoverage.toInt() > 30) {
                    navHeader.setBackgroundResource(R.drawable.clouds)
                }
                //Update values
                tvTemp.text = "it is currently ${temp},"
                tvWeather.text = "with ${weatherDescription}."
            }
        }.start()

    }

    //Adds on click functionality to nav drawer items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //NOT an override function
    private fun onMapReady(mMap: GoogleMap) {
        //Reference to the PoI sub-section of the db
        val poiRef: DatabaseReference = dbRef.child("POIs")
        //Sets the map to Worcester
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(52.18288929882368, -2.225693857359504), 13.5F))
        //Runs at start and whenever database info changes
        poiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Wipe previous data
                    mMap.clear()

                    for (poiSnapshot in dataSnapshot.children) {
                        //Lat and lng are not stored as a LatLng in the Firebase db
                        val lat = poiSnapshot.child("location/latitude/").value as Double
                        val lng = poiSnapshot.child("location/longitude/").value as Double
                        //Normal poi id it's iin the favourites list, else null
                        val favItem = favList.find { item -> item == poiSnapshot.key}
                        //If favourites not toggled, or are toggled and the poi matches
                        if ((!favToggle) || (favToggle && favItem != null) ) {
                            mMap.addMarker(MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(poiSnapshot.child("name").value as String?))!!.tag = poiSnapshot.key
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        //When marker clicked, got to PoiDetailActivity
        mMap.setOnMarkerClickListener { marker ->
            val intent = Intent(this, PoiDetailActivity::class.java)
            //Pass the uuid of the marker clicked
            intent.putExtra("uuid", marker.tag.toString())
            startActivity(intent)
            return@setOnMarkerClickListener true
        }
    }

    override fun onResume() {
        super.onResume()
        //Gets the id of the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //Acquires the GoogleMap object from onMapReady
        mapFragment.getMapAsync(::onMapReady)
    }
}

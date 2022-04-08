package com.rendle.locationapp.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rendle.locationapp.R
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityMainBinding
import com.rendle.locationapp.models.PoIModel
import java.util.*


//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityMainBinding
//Refers to nav drawer toggle in the activity's action bar
private lateinit var toggle: ActionBarDrawerToggle
//List of all PoIs
private var fullPoiList = mutableListOf<PoIModel>()
//List of all favourite PoIs
private var favPoiList = mutableListOf<PoIModel>()
//Mutable list to search PoIs
private var tempPoiList = mutableListOf<PoIModel>()
//RecyclerView adapter
private lateinit var rvAdapter: LocationAdapter
//Recycler view
private lateinit var rvLocations: RecyclerView
//Toggle for showing only favourites
private var favToggle: Boolean = false

//This app's Firebase Authentication
private lateinit var auth: FirebaseAuth
//This app's Firebase Database ref
private lateinit var dbRef: DatabaseReference

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityMainBinding.inflate(layoutInflater)
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
                R.id.nav_item_map -> startActivity(Intent(this, MapActivity::class.java))
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

        //Gets recycler view from XML
        rvLocations = b.rvLocationsList
        //Sets the RecyclerView's layoutManager to the created layout
        rvLocations.layoutManager = LinearLayoutManager(this@MainActivity)

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

        //Toggles favourites when button is pressed
        b.btnFav.setOnClickListener {
            favToggle = !favToggle
            if (favToggle) {
                b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
            } else {
                b.btnFav.setImageResource(R.drawable.ic_fav_24)
            }
            updateFavs()
        }

        //If the list was left in fav mode, set the icon to reflect this
        if (favToggle) {
            b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
        }

        //Reference to the PoI sub-section of the db
        val poiRef: DatabaseReference = dbRef.child("POIs")
        //Runs at start and whenever database info changes
        poiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Wipe previous data
                    fullPoiList.clear()
                    for (poiSnapshot in dataSnapshot.children) {
                        //Lat and lng are not stored as a LatLng in the Firebase db
                        val lat = poiSnapshot.child("location/latitude/").value as Double
                        val lng = poiSnapshot.child("location/longitude/").value as Double
                        //Create a new PoIModel using db info, fav set to false by default
                        val poi = PoIModel(poiSnapshot.key, poiSnapshot.child("name").value as String?, LatLng(lat, lng),poiSnapshot.child("description").value as String?, false)
                        //Add poi to poi list
                        fullPoiList.add(poi)
                    }
                    //UpdateFavs also handles the recycler view
                    updateFavs()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        //In case Firebase database fails, load empty list into rv
        rvAdapter = LocationAdapter(currentList())

        //Open AddLocationActivity when fab is pressed
        b.fabAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }
    }

    //Returns either the full list, or the filtered fav list
    private fun currentList(): MutableList<PoIModel> {
        return if (favToggle) {
            favPoiList
        } else {
            fullPoiList
        }
    }

    fun updateFavs() {
        favPoiList.clear()
        //Reference to the PoI sub-section of the db
        val favRef: DatabaseReference = dbRef.child("Favourites/${auth.currentUser!!.uid}")

        //One time db listener
        favRef.get().addOnSuccessListener {
            if (it != null) {
                for (favSnapshot in it.children) {
                    //Gets fav poi from the db, searches for it in list of pois
                    val poi: PoIModel? = fullPoiList.find { it.uuid == favSnapshot.key }
                    //Sets the fav param to true in the poi list
                    poi!!.fav = true
                    favPoiList.add(poi)
                }
            }
            //Initialises adapter with new list
            rvAdapter = LocationAdapter(currentList())
            //Sends initialised adapter to recycler view
            rvLocations.adapter = rvAdapter
        }.addOnFailureListener{
            Log.e("firebase", "No favourites found", it)
        }
    }

    //Adds on click functionality to nav drawer items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //Inflates the menu search XML
        menuInflater.inflate(R.menu.menu_search, menu)
        val item = menu?.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView
        //When any text is changed
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //Submit does nothing, as we update whenever the text changes
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                tempPoiList.clear()

                //Ignore case for searching
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    currentList().forEach {
                        //Return items matching entered text
                        if (it.name!!.lowercase(Locale.getDefault()).contains(searchText)) {
                            tempPoiList.add(it)
                        }
                    }
                } else {
                    //If it's empty, return all items
                    tempPoiList.addAll(currentList())
                }
                //Update the Recycler View
                rvAdapter.addList(tempPoiList)

                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        updateFavs()
    }
}
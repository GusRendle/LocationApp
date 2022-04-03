package com.rendle.locationapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rendle.locationapp.R
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityMainBinding
import com.rendle.locationapp.models.PoIModel
import java.util.*


//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityMainBinding
//Refers to nav drawer toggle in the activity's action bar
lateinit var toggle: ActionBarDrawerToggle
//List of all PoIs
private var fullPoiList = listOf<PoIModel>()
//Mutable list to search PoIs
private var tempPoiList = mutableListOf<PoIModel>()
//RecyclerView adapter
private lateinit var rvAdapter: LocationAdapter

private lateinit var auth: FirebaseAuth

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

        //Handles clicks on nav drawer icons
        b.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.item1 -> Toast.makeText(applicationContext, "item 1", Toast.LENGTH_LONG).show()
            }
            true
        }

        auth = FirebaseAuth.getInstance()

        //Redirects user to login activity if not logged in
        if(auth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Generates 20 PoIs
        fullPoiList = PoIModel.createLocationList2()
        tempPoiList.addAll(fullPoiList)

        //Initialises adapter with new list
        rvAdapter = LocationAdapter(fullPoiList)
        //Gets Recycler view from XML
        val rvLocations = b.rvLocationsList
        //Sends initialised adapter to rv
        rvLocations.adapter = rvAdapter
        //Sets the RecyclerView's layoutManager to the created layout
        rvLocations.layoutManager = LinearLayoutManager(this)



        //Open AddLocationActivity when fab is pressed
        b.fabAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
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
                    fullPoiList.forEach {
                        //Return items matching entered text
                        if (it.name.lowercase(Locale.getDefault()).contains(searchText)) {
                            tempPoiList.add(it)
                        }
                    }
                } else {
                    //If it's empty, return all items
                    tempPoiList.addAll(fullPoiList)
                }
                //Update the Recycler View
                rvAdapter.addList(tempPoiList)

                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}
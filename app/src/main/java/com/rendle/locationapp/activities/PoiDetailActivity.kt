package com.rendle.locationapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rendle.locationapp.R
import com.rendle.locationapp.databinding.ActivityPoiDetailBinding
import java.util.*

//Uses a Data Binding to refer to objects by their XML IDs
private lateinit var b: ActivityPoiDetailBinding
//This app's Firebase Authentication
private lateinit var auth: FirebaseAuth
//This PoI's uuid
private lateinit var targetUUID: String

//This app's Firebase Database ref
private lateinit var dbRef: DatabaseReference
//This app's Firebase Database ref
private lateinit var poiRef: DatabaseReference
//This app's Firebase Database ref
private lateinit var favRef: DatabaseReference
//This app's Firebase Storage ref
private lateinit var storageRef: StorageReference

//Toggle for showing only favourites
private var isFav: Boolean = false

class PoiDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityPoiDetailBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        //Get the extra from the intent, which is a UUID of the POI
        targetUUID = if (intent.getStringExtra("uuid") != null) {
            intent.getStringExtra("uuid")!!
        } else {
            Toast.makeText(this, "POI not found", Toast.LENGTH_LONG).show()
            UUID.randomUUID().toString()
        }

        //Links Firebase db to the db's URL
        dbRef = FirebaseDatabase.getInstance("https://locationapp-3c40b-default-rtdb.europe-west1.firebasedatabase.app/").reference
        //Reference for Firebase storage
        storageRef = FirebaseStorage.getInstance("gs://locationapp-3c40b.appspot.com").reference

        //Goes to the child with the user's uid in the admins sub category
        poiRef = dbRef.child("POIs").child(targetUUID)
        poiUpdate(poiRef, storageRef)

        //Goes to the child with the user's uid in the favourites sub category
        auth = FirebaseAuth.getInstance()
        favRef = dbRef.child("Favourites")

        favRef.child(auth.currentUser!!.uid).child(targetUUID).get().addOnSuccessListener {
            //If the value exists, poi is a favourite
            isFav = if (it.value != null) {
                b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
                true
            } else {
                false
            }
        }.addOnFailureListener{
            isFav = false
        }

        //Lets user add / remove favourites with a single button
        b.btnFav.setOnClickListener {
            if (isFav) {
                favRef.removeValue()
                b.btnFav.setImageResource(R.drawable.ic_fav_24)
            } else {
                favRef.setValue("true")
                b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
            }
            isFav = !isFav
        }

        //Goes to the child with the user's uid in the admins sub category
        dbRef.child("Admins").child(auth.currentUser!!.uid).get().addOnSuccessListener {
            //If the value exists, user is an admin
            if (it.value != null) {
                //Set admin buttons to visible
                b.btnEdit.visibility = View.VISIBLE
                b.btnRemove.visibility = View.VISIBLE
            }
        }.addOnFailureListener{
            Log.e("firebase", "User is not an Admin", it)
        }

        //Edits the PoI
        b.btnEdit.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            //Pass the uuid of the marker clicked
            intent.putExtra("uuid", targetUUID)
            startActivity(intent)
        }

        //Removes the poi
        b.btnRemove.setOnClickListener {
            poiRef.removeValue()
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        //Goes to the child with the user's uid in the admins sub category
        poiUpdate(poiRef, storageRef)
    }

    private fun poiUpdate(poiRef: DatabaseReference, storageRef: StorageReference) {
        poiRef.get().addOnSuccessListener { poiSnapshot ->
            //If the value exists, user is an admin
            if (poiSnapshot.value != null) {
                //Updates values
                b.toolbarPoi.title = poiSnapshot.child("name").value as String
                b.tvName.text = poiSnapshot.child("name").value as String
                b.tvDescription.text = poiSnapshot.child("description").value as String
                //The location of this poi's image
                val poiImageRef = storageRef.child("images/$targetUUID/main.jpg")
                //Gets image URL from firebase storage
                poiImageRef.downloadUrl.addOnSuccessListener {
                    //Uses the coil library to load the image from the URL
                    b.ivMainImage.load(it)
                }.addOnFailureListener {
                    b.tvImageStatus.text = getString(R.string.image_downloading_failed)
                }
            }
        }.addOnFailureListener{
            Log.e("firebase", "POI not found", it)
        }
    }
}

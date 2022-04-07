package com.rendle.locationapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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
        val storageRef = FirebaseStorage.getInstance("gs://locationapp-3c40b.appspot.com").reference

        //Goes to the child with the user's uid in the admins sub category
        poiRef = dbRef.child("POIs").child(targetUUID)
        poiRef.get().addOnSuccessListener { poiRef ->
            //If the value exists, user is an admin
            if (poiRef.value != null) {
                //Updates values
                b.toolbarPoi.title = poiRef.child("name").value as String
                b.tvName.text = poiRef.child("name").value as String
                b.tvDescription.text = poiRef.child("description").value as String
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

        //Goes to the child with the user's uid in the favourites sub category
        favRef = dbRef.child("Favourites")
        auth = FirebaseAuth.getInstance()
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
                favRef.child(auth.currentUser!!.uid).child(targetUUID).removeValue()
                b.btnFav.setImageResource(R.drawable.ic_fav_24)
            } else {
                favRef.child(auth.currentUser!!.uid).child(targetUUID).setValue("true")
                b.btnFav.setImageResource(R.drawable.ic_fav_filled_24)
            }
            isFav = !isFav
        }

    }
}

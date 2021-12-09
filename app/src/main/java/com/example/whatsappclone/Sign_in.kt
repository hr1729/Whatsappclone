package com.example.whatsappclone

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.*

class Sign_in : AppCompatActivity() {
    var stor1 = Firebase.storage
    var auth=FirebaseAuth.getInstance()
    var currfile:Uri?=null
    var database=FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        userImgView.setOnClickListener {
            uploading_image()
            nextBtn.isEnabled=false
        }
        nextBtn.setOnClickListener {
            val name=nameEt.text.toString()
            if(name.isNullOrBlank()){
                Toast.makeText(this,"please enter your Name",Toast.LENGTH_LONG).show()
            }
            else{
                val user = User(name, currfile.toString(), currfile.toString()/*Needs to thumbnai url*/, auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun uploading_image() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode==1000){
            data?.data?.let {
                Glide.with(this).load(it).transform(CenterCrop(),RoundedCorners(20)).into(userImgView)
                uploading_image_to_firestore("profile")
                currfile=it
            }
        }
    }

    private fun uploading_image_to_firestore(s: String)=GlobalScope.launch(Dispatchers.IO) {

        try{
            currfile?.let{
              val t1=  async {
                    stor1.reference.child("$s/${auth.uid.toString()}").putFile(it)
                }
                awaitAll(t1)
                withContext(Dispatchers.Main){
                    nextBtn.isEnabled=true
                    Toast.makeText(this@Sign_in,"Successfully uploaded",Toast.LENGTH_SHORT).show()
                }

            }
        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@Sign_in,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }
}
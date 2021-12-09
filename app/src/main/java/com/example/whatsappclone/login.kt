package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class login : AppCompatActivity() {
     private lateinit var  no:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        phoneNumberEt.addTextChangedListener {
            var istrue=false
            if (it != null) {
                if(it.length>= 10){
                    istrue=true
                }
                nextBtn.isEnabled=istrue
            }
        }
        nextBtn.setOnClickListener {
            val code=ccp.selectedCountryCodeWithPlus
             no=code+phoneNumberEt.text.toString()
            notifyUser()
        }
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("will will be verifying the phone number is it ok?")
            setPositiveButton("Ok"){_,_->
               startActivity(Intent(this@login,Otp::class.java).putExtra("phone",no))
                finish()
            }
            setNegativeButton("Cancel"){it,it2->
                it.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }
}
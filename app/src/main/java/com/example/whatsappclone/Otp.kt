package com.example.whatsappclone

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

class Otp : AppCompatActivity(), View.OnClickListener {
    private   var phone1:String?=null
    var veirfyid:String?=null

    lateinit var callback:PhoneAuthProvider.OnVerificationStateChangedCallbacks

     var resendToken1:PhoneAuthProvider.ForceResendingToken?=null

    private lateinit var progressdiolog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
         phone1= intent.getStringExtra("phone")
        verifyTv.text="Verify the $phone1"
        spnnable()
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                if(::progressdiolog.isInitialized){
                    progressdiolog.dismiss()
                }
                val smscode=credential.smsCode
                if(!smscode.isNullOrBlank()){
                    sentcodeEt.setText(smscode)
                }
               signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                veirfyid= verificationId
                resendToken1 = token
            }
        }
        val auth=Firebase.auth
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone1!!)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callback)          // OnVerificationStateChangedCallbacks
            .build()


        PhoneAuthProvider.verifyPhoneNumber(options)
        showtimmer(60000)
        progressdiolog=ProgressDialog(this)
        progressdiolog.setMessage("We are sending a verification code")
        progressdiolog.show()

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mauth=FirebaseAuth.getInstance()
        mauth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                startActivity(Intent(this@Otp,Sign_in::class.java).putExtra("phone",phone1))
                finish()
            }
            else{

            }
        }
    }

    private fun showtimmer(i: Long) {
        object:CountDownTimer(i,1000){
            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible=true
                counterTv.text=getString(R.string.timer,millisUntilFinished/1000)
            }
            override fun onFinish() {
                resendBtn.isEnabled=true
                counterTv.isVisible=false
            }
        }.start()
    }

    @SuppressLint("StringFormatMatches")
    private fun spnnable() {
        var span=SpannableString(getString(R.string.String_1,phone1))
        val click=object:ClickableSpan(){
            override fun onClick(widget: View) {
                showActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.bgColor
                ds.isUnderlineText=false
            }
        }
        span.setSpan(click,span.length-13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text=span
    }
    private fun showActivity() {
        startActivity(Intent(this,login::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    override fun onBackPressed() {
    }
    override fun onClick(v: View) {
        when (v) {
            verificationBtn -> {
                // try to enter the code by yourself to handle the case
                // if user enter another sim card used in another phone ...
                var code = sentcodeEt.text.toString()
                if (code.isNotEmpty() && !veirfyid.isNullOrEmpty()) {

                    progressdiolog = createProgressDialog("Please wait...", false)
                    progressdiolog.show()
                    val credential =
                        PhoneAuthProvider.getCredential(veirfyid!!, code.toString())
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if (resendToken1 != null) {
                    resendVerificationCode(phone1.toString(), resendToken1)
                    showtimmer(60000)
                    progressdiolog = createProgressDialog("Sending a verification code", false)
                    progressdiolog.show()
                } else {
                    Toast.makeText(this,"Sorry, You Can't request new code now, Please wait ...",Toast.LENGTH_LONG).show()
                }
            }

        }
    }
    private fun resendVerificationCode(phoneNumber: String, mResendToken: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callback, // OnVerificationStateChangedCallbacks
            mResendToken
        ) // ForceResendingToken from callbacks
    }

}
fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
    return ProgressDialog(this).apply {
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(false)
        setMessage(message)
    }
}
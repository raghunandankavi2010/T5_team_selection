package com.example.raghu.tiger5regulars.ui


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.raghu.tiger5regulars.R
import com.example.raghu.tiger5regulars.models.User
import com.example.raghu.tiger5regulars.models.UserProfile
import com.example.raghu.tiger5regulars.utilities.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.login.*
import timber.log.Timber
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Login to your google Account"
        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val signInButton: SignInButton = findViewById(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        signInButton.setOnClickListener {
            signIn()
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {

                Timber.tag("LoginActivity").w(e, "Google sign in failed")

                updateUI(null)

            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
       Timber.d("firebaseAuthWithGoogle:%s", acct.id!!)
        showProgress()

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.d("signInWithCredential:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.tag("LoginActivity").w(task.exception, "signInWithCredential:failure")
                        Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    hideProgress()
                }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            database = FirebaseDatabase.getInstance().reference
            postUserDetails(user)
            val date = getCurrentDateTime()
            val dateInString = date.toStringFormat("dd/MM/yyyy")
            writeNewUser(it.uid, it.displayName, false, dateInString)
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            sharedPref.edit {
                putString(PREF_NAME, it.displayName)
                putString("id", it.uid)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun postUserDetails(user: FirebaseUser) {

        val userName = user.displayName
        val userPhoto = user.photoUrl.toString()
        val userPhoneNumber = user.phoneNumber
        val userEmail = user.email
        val userID = user.uid
        writeUserProfileDetails(userID, userName, userPhoneNumber, userEmail, userPhoto)
    }

    private fun writeUserProfileDetails(userID: String, userName: String?, userPhoneNumber: String?, userEmail: String?, userPhoto: String?) {
        val userProfile = UserProfile(userName, userEmail, userPhoneNumber, userPhoto)
        val profile = userProfile.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/PlayersProfile/$userID"] = profile

        database.updateChildren(childUpdates)
    }


    private fun writeNewUser(uid: String, name: String?, playing: Boolean, today: String) {
        val user = User(name, playing, today)

        val users = user.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/Players/$uid"] = users

        database.updateChildren(childUpdates)
    }


    fun showProgress() {
        progressBar.visibility = View.VISIBLE
        sign_in_button.isEnabled = false
    }

    fun hideProgress() {
        progressBar.visibility = View.INVISIBLE
        sign_in_button.isEnabled = true
    }
}

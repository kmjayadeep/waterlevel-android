package online.cloud16.waterlevelindicator

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var TAG = "water";

    private lateinit var progressView: View
    private lateinit var progressText: TextView
    private lateinit var progressLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        progressView = findViewById(R.id.progress_view)
        progressLayout = findViewById(R.id.progress_layout)
        progressText = findViewById(R.id.progress_text)
    }

    private fun load() {
        val db = Firebase.database
        db.getReference("/waterlevel").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                Log.i("val", "Value is: $value")
                setPercentage(90)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("val", "cancelled")
            }
        });
    }

    private fun setPercentage(n : Int) {
        progressText.text = "$n%"
        var maxHeight = progressLayout.height
        var height = n * maxHeight / 100
        var layoutParams = progressView.layoutParams
        layoutParams.height = height
        progressView.layoutParams = layoutParams
        if(n<=25) {
            progressView.setBackgroundColor(Color.parseColor("#ff3333"))
        }else if (n<=75) {
            progressView.setBackgroundColor(Color.parseColor("#0080ff"))
        }else {
            progressView.setBackgroundColor(Color.parseColor("#4d4dff"))
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth = Firebase.auth
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    load();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}
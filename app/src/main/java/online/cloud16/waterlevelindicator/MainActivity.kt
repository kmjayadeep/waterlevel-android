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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var TAG = "water";

    private lateinit var progressView: View
    private lateinit var progressText: TextView
    private lateinit var timeView: TextView
    private lateinit var progressLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        progressView = findViewById(R.id.progress_view)
        timeView = findViewById(R.id.timestamp)
        progressLayout = findViewById(R.id.progress_layout)
        progressText = findViewById(R.id.progress_text)
    }

    private fun load() {
        val db = Firebase.database
        db.getReference("/waterlevel").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                Log.i("val", "Value is: $value")

                val timestamp = snapshot.child("timestamp").getValue<Long>()
                val distance = snapshot.child("measurement/distance").getValue<Int>()
                if (timestamp != null) {
                    val calendar = Calendar.getInstance()
                    val df = SimpleDateFormat(
                        "MMM dd HH:mm:ss a", Locale.ENGLISH
                    )
                    calendar.timeInMillis = timestamp
                    timeView.text = "Updated: " + df.format(calendar.time) + "\nValue: " + distance
                }
                val min = snapshot.child("min").getValue<Int>()
                val max = snapshot.child("max").getValue<Int>()
                var percentage = 0
                if (min != null && max != null) {
                    if (min >= max) {
                        Log.i(TAG, "wrong min and max values")
                        return;
                    }
                    if (distance == null) {
                        Log.i(TAG, "No measurement available")
                        return
                    }
                    val height = max - min
                    if (distance > max || distance < min) {
                        Log.i(TAG, "measurement out of range, skipping")
                        return
                    }
                    percentage = (max - distance) * 100 / height
                }
                setPercentage(percentage)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("val", "cancelled")
            }
        });
    }

    private fun setPercentage(n: Int) {
        progressText.text = "$n%"
        var maxHeight = progressLayout.height
        var height = n * maxHeight / 100
        var layoutParams = progressView.layoutParams
        layoutParams.height = height
        progressView.layoutParams = layoutParams
        if (n <= 25) {
            progressView.setBackgroundColor(Color.parseColor("#ff3333"))
        } else if (n <= 75) {
            progressView.setBackgroundColor(Color.parseColor("#0080ff"))
        } else {
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
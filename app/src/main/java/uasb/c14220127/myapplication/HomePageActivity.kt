package uasb.c14220127.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageActivity : AppCompatActivity() {

    private lateinit var workerRecyclerView: RecyclerView
    private lateinit var workerAdapter: WorkerAdapter
    private val workerList = mutableListOf<Worker>()
    private lateinit var welcomeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        welcomeTextView = findViewById(R.id.textView2)
        workerRecyclerView = findViewById(R.id.viewCategory)
        workerRecyclerView.layoutManager = LinearLayoutManager(this)
        workerAdapter = WorkerAdapter(workerList)
        workerRecyclerView.adapter = workerAdapter

        fetchWorkersFromFirebase()
        fetchUserNameFromFirebase()
    }

    private fun fetchWorkersFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("workers")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val worker = document.toObject(Worker::class.java)
                    workerList.add(worker)
                }
                workerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("HomepageActivity", "Error getting documents: ", exception)
            }
    }

    private fun fetchUserNameFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name")
                        welcomeTextView.text = "Hi, $userName"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("HomepageActivity", "Error getting user name: ", exception)
                }
        }
    }
}

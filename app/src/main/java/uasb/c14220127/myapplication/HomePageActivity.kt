package uasb.c14220127.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
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

        // Initialize views
        setupViews()
        // Fetch data
        fetchWorkersFromFirebase()
        fetchUserNameFromFirebase()
    }

    private fun setupViews() {
        try {
            welcomeTextView = findViewById(R.id.textView2)
            workerRecyclerView = findViewById(R.id.viewCategory)  // Pastikan ID ini sama dengan di layout
            workerRecyclerView.layoutManager = LinearLayoutManager(this)
            workerAdapter = WorkerAdapter(workerList)
            workerRecyclerView.adapter = workerAdapter
        } catch (e: Exception) {
            Log.e("HomePageActivity", "Error setting up views: ${e.message}")
            Toast.makeText(this, "Error setting up app", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchWorkersFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("workers")
            .get()
            .addOnSuccessListener { result ->
                workerList.clear()
                Log.d("FirebaseData", "Number of documents: ${result.size()}")

                for (document in result) {
                    try {
                        val worker = document.toObject(Worker::class.java)
                        Log.d("FirebaseData", "Worker data: Name=${worker.name}, Degree=${worker.degree}, Spec=${worker.specialization}, Image=${worker.imageUrl}")
                        workerList.add(worker)
                    } catch (e: Exception) {
                        Log.e("FirebaseData", "Error parsing worker document: ${document.id}", e)
                    }
                }
                workerAdapter.notifyDataSetChanged()
                Log.d("FirebaseData", "Final workerList size: ${workerList.size}")

                if (workerList.isEmpty()) {
                    Toast.makeText(this, "No workers found in database", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseData", "Error getting documents", exception)
                Toast.makeText(this, "Failed to load workers: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchUserNameFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            welcomeTextView.text = "Welcome!"
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userName = document.getString("name")
                    welcomeTextView.text = "Hi, ${userName ?: "User"}"
                } else {
                    welcomeTextView.text = "Welcome!"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomepageActivity", "Error getting user name: ", exception)
                welcomeTextView.text = "Welcome!"
            }
    }
}
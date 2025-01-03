package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
    private val filteredWorkerList = mutableListOf<Worker>()
    private lateinit var welcomeTextView: TextView
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        setupViews()
        setupSearch()
        setupBottomNavigation()
        fetchWorkersFromFirebase()
        fetchUserNameFromFirebase()
    }

    private fun setupViews() {
        welcomeTextView = findViewById(R.id.textview2)
        searchEditText = findViewById(R.id.searchEt)
        workerRecyclerView = findViewById(R.id.viewCategory)
        workerRecyclerView.layoutManager = LinearLayoutManager(this)

        //adapter rec view untuk worker
        workerAdapter = WorkerAdapter(filteredWorkerList, this) { workerId ->
            openDetailActivity(workerId)
            openEditActivity(workerId)
        }
        workerRecyclerView.adapter = workerAdapter
    }

    private fun setupBottomNavigation() {
        val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)
        profileLayout.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val transactionLayout = findViewById<LinearLayout>(R.id.transactionLayout)
        transactionLayout.setOnClickListener {
            startActivity(Intent(this, InvoiceActivity::class.java))
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterWorkers(s.toString())
            }
        })
    }

    private fun filterWorkers(query: String) {
        filteredWorkerList.clear()
        if (query.isEmpty()) {
            filteredWorkerList.addAll(workerList)
        } else {
            val searchQuery = query.lowercase()
            filteredWorkerList.addAll(workerList.filter { worker ->
                worker.name?.lowercase()?.contains(searchQuery) == true ||
                        worker.specialization?.lowercase()?.contains(searchQuery) == true
            })
        }
        workerAdapter.notifyDataSetChanged()
    }


    private fun fetchWorkersFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("workers")
            .get()
            .addOnSuccessListener { result ->
                workerList.clear()
                filteredWorkerList.clear()

                for (document in result) {
                    val worker = document.toObject(Worker::class.java)
                    worker.workerId = document.id // Assign the document ID to workerId
                    workerList.add(worker)
                    filteredWorkerList.add(worker)
                }

                workerAdapter.notifyDataSetChanged()
                if (workerList.isEmpty()) {
                    Toast.makeText(this, "No workers found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseData", "Error fetching workers: ${exception.message}")
                Toast.makeText(this, "Failed to load workers", Toast.LENGTH_SHORT).show()
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
                val userName = document.getString("name")
                welcomeTextView.text = "Hi, ${userName ?: "User"}"
            }
            .addOnFailureListener {
                welcomeTextView.text = "Welcome!"
            }
    }

    private fun openDetailActivity(workerId: String) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("worker_id", workerId)
        }
        startActivity(intent)
    }
    private fun openEditActivity(workerId: String) {
        val intent = Intent(this, EditWorker::class.java).apply {
            putExtra("worker_id", workerId)
        }
        startActivity(intent)
    }
}

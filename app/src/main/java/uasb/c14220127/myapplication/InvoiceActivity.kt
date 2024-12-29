package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InvoiceActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var db: FirebaseFirestore
    private val invoices = mutableListOf<InvoiceData>()
    private lateinit var adapter: InvoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_list)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup SwipeRefresh
        setupSwipeRefresh()

        // Initial data load
        fetchUserInvoices()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.invoiceRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        Log.d("InvoiceActivity", "Views initialized: RecyclerView=$recyclerView, EmptyState=$emptyStateLayout, Loading=$loadingProgressBar")
    }


    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = InvoiceAdapter(invoices) { invoice ->
            val intent = Intent(this, InvoiceDetailActivity::class.java).apply {
                putExtra("bookingId", invoice.bookingId)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        Log.d("InvoiceActivity", "RecyclerView adapter set")
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            fetchUserInvoices()
        }
    }

    private fun setupBottomNavigation() {
        val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)
        profileLayout.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val explorerLayout = findViewById<LinearLayout>(R.id.explorerLayout)
        explorerLayout.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }
    }

    private fun fetchUserInvoices() {
        showLoading()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            hideLoading()
            showEmptyState()
            return
        }

        db.collection("invoices")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                invoices.clear()
                for (document in documents) {
                    val invoice = document.toObject(InvoiceData::class.java)
                    invoices.add(invoice)
                }

                if (invoices.isEmpty()) {
                    showEmptyState()
                } else {
                    showInvoices()
                }

                recyclerView.adapter?.notifyDataSetChanged()
                hideLoading()
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { e ->
                hideLoading()
                swipeRefresh.isRefreshing = false
                Toast.makeText(this, "Error loading invoices: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showEmptyState() {
        emptyStateLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showInvoices() {
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }
}
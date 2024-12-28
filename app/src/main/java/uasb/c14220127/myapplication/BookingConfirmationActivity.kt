package uasb.c14220127.myapplication

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class BookingConfirmationActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout

    // Views for booking details
    private lateinit var bookingIdTv: TextView
    private lateinit var dateTv: TextView
    private lateinit var durationTv: TextView
    private lateinit var priceTv: TextView
    private lateinit var paymentMethodTv: TextView
    private lateinit var statusTv: TextView

    // Views for worker details
    private lateinit var workerNameTv: TextView
    private lateinit var workerPhoneTv: TextView
    private lateinit var workerAddressTv: TextView

    // Views for user details
    private lateinit var userNameTv: TextView
    private lateinit var userPhoneTv: TextView
    private lateinit var userAddressTv: TextView

    // RecyclerView for jobs
    private lateinit var jobsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booking_confirmation)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Get booking ID from intent
        val bookingId = intent.getStringExtra("booking_id")
        if (bookingId != null) {
            fetchBookingDetails(bookingId)
        } else {
            showError("Booking ID not found")
        }

        // Setup back button in toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Booking Confirmation"
        }
    }

    private fun initializeViews() {
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        contentLayout = findViewById(R.id.contentLayout)

        // Booking details
        bookingIdTv = findViewById(R.id.bookingIdTv)
        dateTv = findViewById(R.id.dateTv)
        durationTv = findViewById(R.id.durationTv)
        priceTv = findViewById(R.id.priceTv)
        paymentMethodTv = findViewById(R.id.paymentMethodTv)
        statusTv = findViewById(R.id.statusTv)

        // Worker details
        workerNameTv = findViewById(R.id.workerNameTv)
        workerPhoneTv = findViewById(R.id.workerPhoneTv)
        workerAddressTv = findViewById(R.id.workerAddressTv)

        // User details
        userNameTv = findViewById(R.id.userNameTv)
        userPhoneTv = findViewById(R.id.userPhoneTv)
        userAddressTv = findViewById(R.id.userAddressTv)

        // Jobs RecyclerView
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView)
        jobsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchBookingDetails(bookingId: String) {
        showLoading()

        db.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    displayBookingDetails(document.toObject(BookingData::class.java))
                } else {
                    showError("Booking not found")
                }
            }
            .addOnFailureListener { e ->
                showError("Error loading booking: ${e.message}")
            }
    }

    private fun displayBookingDetails(bookingData: BookingData?) {
        hideLoading()

        if (bookingData == null) {
            showError("Invalid booking data")
            return
        }

        // Display booking details
        bookingIdTv.text = "Booking ID: ${bookingData.bookingId}"
        dateTv.text = "Date: ${bookingData.date}"
        durationTv.text = "Duration: ${bookingData.duration}"
        priceTv.text = "Price: Rp ${bookingData.price}"
        paymentMethodTv.text = "Payment: ${bookingData.paymentMethod}"
        statusTv.text = "Status: ${bookingData.status.capitalize()}"

        // Display worker details
        workerNameTv.text = "Name: ${bookingData.workerName}"
        workerPhoneTv.text = "Phone: ${bookingData.workerPhone}"
        workerAddressTv.text = "Address: ${bookingData.workerAddress}"

        // Display user details
        userNameTv.text = "Name: ${bookingData.userName}"
        userPhoneTv.text = "Phone: ${bookingData.userPhone}"
        userAddressTv.text = "Address: ${bookingData.userAddress}"

        // Display jobs
        val adapter = JobAdapter(ArrayList(bookingData.jobs))
        jobsRecyclerView.adapter = adapter
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgressBar.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}